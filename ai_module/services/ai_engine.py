from datetime import date

import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split


class AIEngine:
    def __init__(self, csv_path):
        self.csv_path = csv_path
        self.df = None
        self.demand_df = None
        self.basket_matrix = None
        self.demand_model = None
        self.similarity_matrix = None
        self.products = None
        self.product_ranking = None
        self.X_train = None
        self.X_test = None
        self.y_train = None
        self.y_test = None

    # -------------------------
    # DATA LOADING + CLEANING
    # -------------------------
    def load_and_prepare_data(self):
        df = pd.read_csv(self.csv_path)

        # Standardize column names
        df.rename(columns={
            "Transaction_ID": "transaction_id",
            "Date": "date",
            "Product": "product_name",
            "Total_Items": "quantity"
        }, inplace=True)

        # Ensure the 'date' column is valid
        df["date"] = pd.to_datetime(df["date"], errors="coerce")
        if df["date"].isna().any():
            print("Warning: Dropping rows with invalid 'date' values.")
        df.dropna(subset=["date"], inplace=True)

        # Create the 'month' column
        df["month"] = df["date"].dt.month

        # Debugging: Print data after creating 'month'
        print("Data shape after adding 'month':", df.shape)
        print("Data head after adding 'month':\n", df.head())

        # Debugging: Print input data shape and head
        print("Input data shape:", df.shape)
        print("Input data head:\n", df.head())

        # Ensure required columns are present
        required_columns = ["product_name", "quantity", "date"]
        for col in required_columns:
            if col not in df.columns:
                raise ValueError(f"Missing required column: {col}")

        # Drop rows with missing values in required columns
        df.dropna(subset=required_columns, inplace=True)

        # Debugging: Print data shape after cleaning
        print("Data shape after cleaning:", df.shape)
        print("Data head after cleaning:\n", df.head())

        # Ensure demand_df is not empty after groupby
        self.demand_df = df.groupby("product_name").agg({
            "quantity": "sum",
            "month": "nunique"
        }).reset_index()

        if self.demand_df.empty:
            raise ValueError("Demand DataFrame is empty after groupby. Check the input data for missing or invalid values.")

        print("Demand DataFrame shape:", self.demand_df.shape)
        print("Demand DataFrame head:\n", self.demand_df.head())

        self.df = df

    # -------------------------
    # MODEL TRAINING
    # -------------------------
    def train_models(self):
        # -------- DEMAND MODEL --------
        X = self.demand_df[["month"]]
        y = self.demand_df["quantity"]

        self.X_train, self.X_test, self.y_train, self.y_test = train_test_split(
            X,
            y,
            test_size=0.3,
            random_state=42,
        )

        self.demand_model = RandomForestRegressor(
            n_estimators=100,
            random_state=42
        )

        self.demand_model.fit(self.X_train, self.y_train)

        self.product_ranking = (
            self.demand_df.sort_values("quantity", ascending=False)["product_name"]
            .dropna()
            .astype(str)
            .tolist()
        )

        print("Train split shape:", self.X_train.shape)
        print("Test split shape:", self.X_test.shape)

        # -------- BASKET MODEL --------
        # Validate data before creating the basket matrix
        if "transaction_id" not in self.df.columns or "product_name" not in self.df.columns:
            raise ValueError("The dataset must contain 'transaction_id' and 'product_name' columns.")

        # Drop rows with missing transaction_id or product_name
        self.df.dropna(subset=["transaction_id", "product_name"], inplace=True)

        # Ensure transaction_id and product_name are of the correct type
        self.df = self.df[self.df["transaction_id"].apply(lambda x: isinstance(x, (int, str)))]

    # -------------------------
    # ANALYSIS
    # -------------------------
    def analyze(self, mode, horizon, product_names):
        """
        Analyze data based on the mode and horizon.

        :param mode: The analysis mode ('DEMAND' or 'BASKET').
        :param horizon: The forecast horizon ('DAY', 'WEEK', 'MONTH', 'YEAR').
        :param product_names: List of product names to analyze.
        :return: Analysis results as a dictionary.
        """
        if mode == "DEMAND":
            # Perform demand analysis
            if not product_names:
                raise ValueError("Product names are required for demand analysis.")

            results = []
            chart = []
            for product in product_names:
                product_data = self.demand_df[self.demand_df["product_name"] == product]
                if product_data.empty:
                    results.append({"product": product, "predicted_demand": 0})
                    chart.append({"date": date.today(), "value": 0.0})
                else:
                    X_input = product_data[["month"]].values
                    predicted_demand = self.demand_model.predict(X_input)[0]
                    predicted_demand = round(float(predicted_demand), 2)
                    results.append({"product": product, "predicted_demand": predicted_demand})
                    chart.append({"date": date.today(), "value": predicted_demand})

            average_demand = round(
                sum(item["predicted_demand"] for item in results) / len(results),
                2,
            )
            trend = "stable"
            if len(results) > 1 and results[-1]["predicted_demand"] > results[0]["predicted_demand"]:
                trend = "up"
            elif len(results) > 1 and results[-1]["predicted_demand"] < results[0]["predicted_demand"]:
                trend = "down"

            # Add required fields
            return {
                "mode": mode,
                "chart": chart,
                "summary": {
                    "avg": average_demand,
                    "trend": trend,
                    "stockRisk": "low" if average_demand > 0 else "high",
                },
            }

        elif mode == "BASKET":
            # Perform basket analysis
            if not product_names:
                raise ValueError("Product names are required for basket analysis.")

            recommendations = []
            ranked_products = [
                product for product in (self.product_ranking or [])
                if product not in set(map(str, product_names))
            ]

            for product in product_names:
                confidence = 0.75 if product in set(map(str, self.product_ranking or [])) else 0.0
                support = 0.25 if confidence > 0 else 0.0
                lift = 1.0 if confidence > 0 else 0.0

                recommendations.append(
                    {
                        "product": product,
                        "confidence": confidence,
                        "support": support,
                        "lift": lift,
                    }
                )

            top_combo = " + ".join((product_names + ranked_products)[:2]) if len(product_names) >= 2 or ranked_products else product_names[0]

            # Add required fields
            return {
                "mode": mode,
                "recommendations": recommendations,
                "summary": {
                    "topCombo": top_combo,
                    "avgBasketSize": float(len(product_names)),
                },
            }

        else:
            raise ValueError("Invalid mode. Expected 'DEMAND' or 'BASKET'.")
