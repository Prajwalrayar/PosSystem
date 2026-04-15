from __future__ import annotations

import ast
import math
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import date, timedelta
from itertools import combinations
from pathlib import Path
from statistics import mean, pstdev
import os

import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

from fastapi.middleware.cors import CORSMiddleware

try:
    from schemas import (
        AIAnalysisMode,
        BasketRecommendation,
        BasketResponse,
        BasketSummary,
        DemandChartPoint,
        DemandResponse,
        DemandSummary,
        ForecastHorizon,
        SmartAIRequest,
    )
except ImportError:  # pragma: no cover - supports package execution
    from ..schemas import (
        AIAnalysisMode,
        BasketRecommendation,
        BasketResponse,
        BasketSummary,
        DemandChartPoint,
        DemandResponse,
        DemandSummary,
        ForecastHorizon,
        SmartAIRequest,
    )


@dataclass(slots=True)
class DemandTrainingBundle:
    model: Pipeline | None
    history_by_product: dict[str, dict[date, float]]
    baseline_by_product: dict[str, float]
    popularity: Counter[str]
    last_observed_date: date | None


@dataclass(slots=True)
class BasketTrainingBundle:
    item_counts: Counter[str]
    pair_counts: dict[str, dict[str, int]]
    transaction_count: int
    average_basket_size: float


class AIEngine:
    def __init__(self):
        print("DEBUG: Initializing AIEngine")

        # Define base directory
        self.base_dir = Path(__file__).resolve().parent.parent
        print("DEBUG: Base directory set to:", self.base_dir)

        # Initialize catalog_lookup as an empty dictionary
        self.catalog_lookup = {}

        # Step 1: Load raw transactions (no normalization yet)
        self.transactions = self._load_transactions(raw=True)
        print("DEBUG: Transactions loaded:", len(self.transactions))

        # Step 2: Build catalog and lookup
        print("DEBUG: Building catalog...")
        self.basket_catalog = self._build_catalog()
        self.catalog_lookup = self._create_lookup(self.basket_catalog)
        print("DEBUG: catalog_lookup initialized:", len(self.catalog_lookup))

        # Step 3: Normalize products after catalog is ready
        self.transactions["products"] = self.transactions["products"].apply(self._normalize_product_names)
        print("DEBUG: Transactions normalized.")

    def _create_lookup(self, catalog: list[str]) -> dict[str, str]:
        """Create a lookup dictionary from the catalog."""
        return {name.lower(): name for name in catalog}

    def clean_products(self, products: list[str]) -> list[str]:
        """Normalize product names using catalog_lookup."""
        if not self.catalog_lookup:
            print("WARNING: catalog_lookup not ready, returning original products.")
            return products

        return [self.catalog_lookup.get(p.lower(), p) for p in products]

    def analyze(self, payload: SmartAIRequest) -> DemandResponse | BasketResponse:
        print("DEBUG: Starting analysis with payload:", payload)
        try:
            if payload.mode == AIAnalysisMode.DEMAND:
                print("DEBUG: Mode is DEMAND")
                response = self._build_demand_response(payload)
                print("DEBUG: Demand response built successfully:", response)
                return response
            if payload.mode == AIAnalysisMode.BASKET:
                print("DEBUG: Mode is BASKET")
                response = self._build_basket_response(payload)
                print("DEBUG: Basket response built successfully:", response)
                return response
            raise ValueError(f"Unsupported mode: {payload.mode}")
        except ValueError as ve:
            print("ERROR: ValueError occurred:", ve)
            raise HTTPException(status_code=400, detail=str(ve))
        except Exception as e:
            print("ERROR: Exception occurred during analysis:", e)
            raise HTTPException(status_code=500, detail="Internal Server Error")

    def _load_transactions(self, raw: bool = False) -> pd.DataFrame:
        print("DEBUG: Starting to load transactions")
        csv_path = self.base_dir / "Retail_store_100k.csv"
        if not csv_path.exists():
            print(f"ERROR: CSV file not found at {csv_path}")
            return pd.DataFrame(columns=["transaction_date", "products"])

        try:
            frame = pd.read_csv(csv_path)
        except Exception as e:
            print(f"ERROR: Failed to read CSV file at {csv_path}: {e}")
            return pd.DataFrame(columns=["transaction_date", "products"])

        if frame.empty or "Product" not in frame.columns or "Date" not in frame.columns:
            print("ERROR: CSV file is empty or missing required columns.")
            return pd.DataFrame(columns=["transaction_date", "products"])

        print("DEBUG: Initial frame head:\n", frame.head())
        cleaned = frame.copy()
        cleaned["transaction_date"] = pd.to_datetime(cleaned["Date"], errors="coerce").dt.date
        cleaned["products"] = cleaned["Product"].apply(self._parse_transaction_items)
        print("DEBUG: After parsing products:\n", cleaned.head())
        cleaned = cleaned[cleaned["transaction_date"].notna() & cleaned["products"].map(bool)]

        if cleaned.empty:
            print("ERROR: No valid transactions found after cleaning.")
            return pd.DataFrame(columns=["transaction_date", "products"])

        if raw:
            return cleaned.reset_index(drop=True)  # Return raw transactions without normalization

        print("DEBUG: After normalizing product names:\n", cleaned.head())
        cleaned["products"] = cleaned["products"].apply(self._normalize_product_names)
        cleaned = cleaned[cleaned["products"].map(bool)]
        cleaned = cleaned.sort_values("transaction_date")
        print("DEBUG: Final cleaned transactions:\n", cleaned.head())
        return cleaned.reset_index(drop=True)

    def _build_catalog(self) -> list[str]:
        print("DEBUG: Building catalog")
        if self.transactions.empty:
            print("DEBUG: Transactions are empty, using default catalog.")
            return self._default_catalog()

        popularity = Counter(item for basket in self.transactions["products"] for item in basket)
        print("DEBUG: Item popularity:\n", popularity)
        if not popularity:
            print("DEBUG: No items found in transactions, using default catalog.")
            return self._default_catalog()

        ranked = [name for name, _ in popularity.most_common()]
        print("DEBUG: Ranked catalog:\n", ranked)
        return ranked or self._default_catalog()

    def _train_demand_model(self) -> DemandTrainingBundle:
        if self.transactions.empty:
            return DemandTrainingBundle(None, {}, {}, Counter(), None)

        daily_counts: dict[date, Counter[str]] = defaultdict(Counter)
        popularity: Counter[str] = Counter()
        for row in self.transactions.itertuples(index=False):
            row_date: date = row.transaction_date
            products = list(dict.fromkeys(row.products))
            popularity.update(products)
            for product in products:
                daily_counts[row_date][product] += 1

        if not daily_counts:
            return DemandTrainingBundle(None, {}, {}, popularity, None)

        all_dates = sorted(daily_counts.keys())
        first_day = all_dates[0]
        last_day = all_dates[-1]
        calendar = [first_day + timedelta(days=offset) for offset in range((last_day - first_day).days + 1)]
        cutoff_index = max(int(len(calendar) * 0.8), 30)
        cutoff_index = min(cutoff_index, len(calendar) - 1)
        cutoff_date = calendar[cutoff_index]

        history_by_product: dict[str, dict[date, float]] = {}
        baseline_by_product: dict[str, float] = {}
        feature_rows: list[dict[str, object]] = []

        for product in self.basket_catalog:
            series = [float(daily_counts.get(day, Counter()).get(product, 0)) for day in calendar]
            if not any(series):
                continue

            history_by_product[product] = {day: series[index] for index, day in enumerate(calendar)}
            baseline_by_product[product] = round(mean(series), 4)

            for index in range(30, len(calendar)):
                target_date = calendar[index]
                if target_date > cutoff_date:
                    continue
                feature_rows.append(self._build_demand_feature_row(product, target_date, series, index))

        if not feature_rows:
            return DemandTrainingBundle(None, history_by_product, baseline_by_product, popularity, last_day)

        training_frame = pd.DataFrame(feature_rows)
        feature_columns = [
            "product",
            "day_of_week",
            "month",
            "quarter",
            "day_of_year",
            "is_weekend",
            "lag_1",
            "lag_7",
            "lag_14",
            "lag_30",
            "rolling_7",
            "rolling_30",
            "volatility_7",
            "volatility_30",
            "trend_7",
            "trend_30",
        ]

        preprocessor = ColumnTransformer(
            transformers=[
                (
                    "product",
                    OneHotEncoder(handle_unknown="ignore", sparse_output=False),
                    ["product"],
                )
            ],
            remainder="passthrough",
        )

        model = Pipeline(
            steps=[
                ("preprocessor", preprocessor),
                (
                    "regressor",
                    RandomForestRegressor(
                        n_estimators=140,
                        random_state=42,
                        min_samples_leaf=2,
                        n_jobs=-1,
                    ),
                ),
            ]
        )

        model.fit(training_frame[feature_columns], training_frame["target"])
        return DemandTrainingBundle(model, history_by_product, baseline_by_product, popularity, last_day)

    def _train_basket_model(self) -> BasketTrainingBundle:
        if self.transactions.empty:
            return BasketTrainingBundle(Counter(), {}, 0, 3.2)

        cutoff_index = max(int(len(self.transactions) * 0.8), 1)
        cutoff_index = min(cutoff_index, len(self.transactions))
        train_transactions = self.transactions.iloc[:cutoff_index]

        item_counts: Counter[str] = Counter()
        pair_counts: dict[str, dict[str, int]] = defaultdict(lambda: defaultdict(int))
        basket_sizes: list[int] = []

        for row in train_transactions.itertuples(index=False):
            basket = list(dict.fromkeys(row.products))
            if not basket:
                continue

            basket_sizes.append(len(basket))
            item_counts.update(basket)
            for left, right in combinations(sorted(set(basket)), 2):
                pair_counts[left][right] += 1
                pair_counts[right][left] += 1

        average_basket_size = float(mean(basket_sizes)) if basket_sizes else 3.2
        return BasketTrainingBundle(item_counts, pair_counts, len(train_transactions), average_basket_size)

    def _build_demand_feature_row(
        self,
        product: str,
        target_date: date,
        series: list[float],
        index: int,
    ) -> dict[str, object]:
        recent_7 = series[max(0, index - 7) : index]
        recent_30 = series[max(0, index - 30) : index]

        return {
            "product": product,
            "day_of_week": target_date.weekday(),
            "month": target_date.month,
            "quarter": ((target_date.month - 1) // 3) + 1,
            "day_of_year": target_date.timetuple().tm_yday,
            "is_weekend": 1 if target_date.weekday() >= 5 else 0,
            "lag_1": series[index - 1] if index >= 1 else 0.0,
            "lag_7": series[index - 7] if index >= 7 else 0.0,
            "lag_14": series[index - 14] if index >= 14 else 0.0,
            "lag_30": series[index - 30] if index >= 30 else 0.0,
            "rolling_7": round(mean(recent_7), 4) if recent_7 else 0.0,
            "rolling_30": round(mean(recent_30), 4) if recent_30 else 0.0,
            "volatility_7": round(pstdev(recent_7), 4) if len(recent_7) > 1 else 0.0,
            "volatility_30": round(pstdev(recent_30), 4) if len(recent_30) > 1 else 0.0,
            "trend_7": round(series[index - 1] - series[index - 7], 4) if index >= 7 else 0.0,
            "trend_30": round(series[index - 1] - series[index - 30], 4) if index >= 30 else 0.0,
            "target": series[index],
        }

    def _build_demand_response(self, payload: SmartAIRequest) -> DemandResponse:
        logger.debug(f"Building demand response for payload: {payload}")
        product_names = self._normalize_product_names(payload.productNames)
        logger.debug(f"Normalized product names: {product_names}")
        product_names = [name for name in product_names if name in self.demand_history_by_product]
        logger.debug(f"Filtered product names: {product_names}")
        if not product_names:
            logger.warning("No valid product names found. Falling back to default product names.")
            product_names = self._default_product_names()

        try:
            product_forecasts = [self._forecast_product(name, payload.horizon) for name in product_names]
            logger.debug(f"Product forecasts: {product_forecasts}")
        except Exception as e:
            logger.error(f"Error during product forecasting: {e}")
            raise

        if not product_forecasts:
            logger.warning("No product forecasts generated. Falling back to default demand response.")
            return self._fallback_demand_response(payload, product_names)

        chart: list[DemandChartPoint] = []
        for index in range(len(product_forecasts[0])):
            forecast_date = product_forecasts[0][index][0]
            values = [forecast[index][1] for forecast in product_forecasts]
            combined_value = max(mean(values), 0.0)
            chart.append(DemandChartPoint(date=forecast_date, value=round(combined_value, 2)))

        values = [point.value for point in chart]
        summary = DemandSummary(
            avg=round(mean(values), 2),
            trend=self._classify_trend(values),
            stockRisk=self._classify_stock_risk(values),
        )
        logger.debug(f"Demand response summary: {summary}")
        return DemandResponse(chart=chart, summary=summary)

    def _fallback_demand_response(self, payload: SmartAIRequest, product_names: list[str]) -> DemandResponse:
        horizon_steps, step_delta = self._horizon_profile(payload.horizon)
        branch_factor = 1 + ((payload.branchId % 9) - 4) * 0.02
        base_level = mean([self.demand_baselines.get(name, 20.0) for name in product_names]) if product_names else 20.0
        start_day = date.today() + step_delta

        chart: list[DemandChartPoint] = []
        for index in range(horizon_steps):
            current_day = start_day + (step_delta * index)
            phase = index / max(horizon_steps - 1, 1)
            seasonal = math.sin(phase * math.tau) * 0.12
            drift = index * 0.015
            value = max(base_level * (1 + seasonal + drift) * branch_factor, 0.0)
            chart.append(DemandChartPoint(date=current_day, value=round(value, 2)))

        values = [point.value for point in chart]
        return DemandResponse(
            chart=chart,
            summary=DemandSummary(
                avg=round(mean(values), 2),
                trend=self._classify_trend(values),
                stockRisk=self._classify_stock_risk(values),
            ),
        )

    def _forecast_product(self, product: str, horizon: ForecastHorizon) -> list[tuple[date, float]]:
        horizon_steps, step_delta = self._horizon_profile(horizon)
        history = dict(self.demand_history_by_product.get(product, {}))
        if not history:
            return []

        cursor_date = max(history.keys())
        forecast_start = date.today() + step_delta
        forecasts: list[tuple[date, float]] = []

        for step in range(horizon_steps):
            target_date = forecast_start + (step_delta * step)
            while cursor_date < target_date:
                cursor_date += timedelta(days=1)
                feature_row = pd.DataFrame([self._build_demand_feature_row_for_prediction(product, cursor_date, history)])
                if self.demand_model is None:
                    predicted_value = self._baseline_forecast(product, cursor_date, history)
                else:
                    predicted_value = float(self.demand_model.predict(feature_row)[0])
                history[cursor_date] = max(predicted_value, 0.0)

            forecasts.append((target_date, round(history[target_date], 2)))

        return forecasts

    def _build_demand_feature_row_for_prediction(
        self,
        product: str,
        target_date: date,
        history: dict[date, float],
    ) -> dict[str, object]:
        def _get_lag(days_back: int) -> float:
            return float(history.get(target_date - timedelta(days=days_back), 0.0))

        recent_7 = [_get_lag(step) for step in range(1, 8)]
        recent_30 = [_get_lag(step) for step in range(1, 31)]

        return {
            "product": product,
            "day_of_week": target_date.weekday(),
            "month": target_date.month,
            "quarter": ((target_date.month - 1) // 3) + 1,
            "day_of_year": target_date.timetuple().tm_yday,
            "is_weekend": 1 if target_date.weekday() >= 5 else 0,
            "lag_1": recent_7[0],
            "lag_7": _get_lag(7),
            "lag_14": _get_lag(14),
            "lag_30": _get_lag(30),
            "rolling_7": round(mean(recent_7), 4),
            "rolling_30": round(mean(recent_30), 4),
            "volatility_7": round(pstdev(recent_7), 4) if len(recent_7) > 1 else 0.0,
            "volatility_30": round(pstdev(recent_30), 4) if len(recent_30) > 1 else 0.0,
            "trend_7": round(recent_7[0] - _get_lag(7), 4),
            "trend_30": round(recent_7[0] - _get_lag(30), 4),
        }

    def _baseline_forecast(self, product: str, target_date: date, history: dict[date, float]) -> float:
        recent_7 = [history.get(target_date - timedelta(days=step), 0.0) for step in range(1, 8)]
        recent_30 = [history.get(target_date - timedelta(days=step), 0.0) for step in range(1, 31)]
        base_level = self.demand_baselines.get(product, 18.0)
        seasonal = math.sin((target_date.timetuple().tm_yday / 365.0) * math.tau) * (base_level * 0.08)
        rolling = 0.65 * mean(recent_7) + 0.35 * mean(recent_30)
        trend = recent_7[0] - recent_7[-1]
        value = (base_level * 0.4) + (rolling * 0.6) + (trend * 0.15) + seasonal
        return max(value, 0.0)

    def _build_basket_response(self, payload: SmartAIRequest) -> BasketResponse:
        logger.debug(f"Building basket response for payload: {payload}")
        seed_labels = self._normalize_product_names(payload.productNames)
        logger.debug(f"Normalized seed labels: {seed_labels}")
        seed_labels = [name for name in seed_labels if name in self.basket_catalog]
        logger.debug(f"Filtered seed labels: {seed_labels}")
        if not seed_labels:
            logger.warning("No valid seed labels found. Falling back to default product names.")
            seed_labels = self._default_product_names()
        seed_set = set(seed_labels)
        logger.debug(f"Seed set: {seed_set}")

        candidate_scores: dict[str, dict[str, float]] = {}
        for seed_label in seed_labels:
            co_occurrences = self.basket_pairs.get(seed_label, {})
            seed_count = max(self.basket_item_counts.get(seed_label, 1), 1)
            logger.debug(f"Processing seed label: {seed_label}, co-occurrences: {co_occurrences}")
            for partner_label, pair_count in co_occurrences.items():
                if partner_label in seed_set:
                    continue

                partner_count = max(self.basket_item_counts.get(partner_label, 1), 1)
                support = pair_count / max(self.basket_transaction_count, 1)
                confidence = pair_count / seed_count
                lift = confidence / max(partner_count / max(self.basket_transaction_count, 1), 1e-9)
                combo = " + ".join(sorted({seed_label, partner_label}))

                candidate = {
                    "confidence": round(min(confidence, 1.0), 4),
                    "support": round(min(support, 1.0), 4),
                    "lift": round(max(lift, 0.01), 4),
                }
                current = candidate_scores.get(combo)
                if current is None or candidate["lift"] > current["lift"]:
                    candidate_scores[combo] = candidate

        if not candidate_scores:
            logger.warning("No candidate scores generated. Falling back to default basket recommendations.")
            candidate_scores = self._fallback_basket_recommendations(seed_labels)

        recommendations = [
            BasketRecommendation(product=product, **scores)
            for product, scores in sorted(
                candidate_scores.items(),
                key=lambda item: (item[1]["lift"], item[1]["confidence"]),
                reverse=True,
            )
        ]
        logger.debug(f"Basket recommendations: {recommendations}")
        return BasketResponse(recommendations=recommendations, summary=BasketSummary(topCombo="", avgBasketSize=0.0))

    def _fallback_basket_recommendations(self, seed_labels: list[str]) -> dict[str, dict[str, float]]:
        recommendations: dict[str, dict[str, float]] = {}
        fallback_candidates = [label for label in self.basket_catalog if label not in seed_labels] or self._default_catalog()

        for index, seed_label in enumerate(seed_labels):
            partner = fallback_candidates[index % len(fallback_candidates)]
            combo = " + ".join(sorted({seed_label, partner}))
            base = 0.42 + (index * 0.07)
            recommendations[combo] = {
                "confidence": round(min(base + 0.12, 0.95), 4),
                "support": round(min(base / 2, 0.7), 4),
                "lift": round(1.1 + (index * 0.2), 4),
            }
        return recommendations

    def _parse_transaction_items(self, items: str) -> list[str]:
        """Parse the Product column into a list of items."""
        try:
            return ast.literal_eval(items)
        except (ValueError, SyntaxError):
            print(f"WARNING: Failed to parse items: {items}")
            return []

    def _normalize_product_names(self, products: list[str]) -> list[str]:
        """Normalize product names using catalog_lookup."""
        if not self.catalog_lookup:
            print("WARNING: catalog_lookup not ready, returning original products.")
            return products

        return [self.catalog_lookup.get(p.lower(), p) for p in products]

    def _default_catalog(self) -> list[str]:
        return [
            "Bread",
            "Milk",
            "Butter",
            "Eggs",
            "Coffee",
            "Tea",
            "Rice",
            "Chicken",
            "Apples",
            "Bananas",
            "Cheese",
            "Yogurt",
        ]

    def _default_product_names(self, branch_id: int, count: int = 3) -> list[str]:
        catalog = [name for name, _ in self.product_popularity.most_common()] or self.basket_catalog or self._default_catalog()
        if not catalog:
            return ["Bread", "Milk", "Butter"]

        start = branch_id % len(catalog)
        selected = [catalog[(start + offset) % len(catalog)] for offset in range(count)]
        return list(dict.fromkeys(selected))

    def _horizon_profile(self, horizon: ForecastHorizon) -> tuple[int, timedelta]:
        if horizon == ForecastHorizon.DAY:
            return 7, timedelta(days=1)
        if horizon == ForecastHorizon.WEEK:
            return 8, timedelta(weeks=1)
        if horizon == ForecastHorizon.MONTH:
            return 12, timedelta(days=30)
        if horizon == ForecastHorizon.YEAR:
            return 12, timedelta(days=30)
        return 7, timedelta(days=1)

    def _classify_trend(self, values: list[float]) -> str:
        if len(values) < 2:
            return "STABLE"

        midpoint = max(len(values) // 2, 1)
        first_half = mean(values[:midpoint])
        second_half = mean(values[midpoint:])
        delta = second_half - first_half

        if delta > 1.25:
            return "UP"
        if delta < -1.25:
            return "DOWN"
        return "STABLE"

    def _classify_stock_risk(self, values: list[float], branch_id: int) -> str:
        average_value = mean(values)
        volatility = pstdev(values) / max(average_value, 1.0) if len(values) > 1 else 0.0
        trend = self._classify_trend(values)

        if average_value >= 55 or volatility >= 0.18 or (trend == "UP" and average_value >= 35):
            return "HIGH"
        if average_value >= 30 or volatility >= 0.1 or trend == "UP":
            return "MEDIUM"
        if branch_id % 4 == 0:
            return "MEDIUM"
        return "LOW"
