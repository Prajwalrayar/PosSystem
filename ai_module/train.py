# import pandas as pd
# import mysql.connector
# from sklearn.ensemble import RandomForestRegressor
# import joblib

# # 🔗 DB connection
# conn = mysql.connector.connect(
#     host="localhost",
#     user="root",
#     password="Prajwal@0103",
#     database="saas_-pos"
# )

# # 📥 Fetch data
# query = """
# SELECT 
#     product_id,
#     DATE(created_at) as day,
#     SUM(quantity) as total_sold
# FROM order_items
# GROUP BY product_id, DATE(created_at)
# ORDER BY day
# """

# df = pd.read_sql(query, conn)

# if df.empty:
#     print("❌ No data found")
#     exit()

# print("✅ Rows fetched:", len(df))

# # 🧠 Features
# df['day_of_week'] = pd.to_datetime(df['day']).dt.dayofweek
# df['month'] = pd.to_datetime(df['day']).dt.month

# df['prev_sales'] = df.groupby('product_id')['total_sold'].shift(1)

# df['avg_7'] = df.groupby('product_id')['total_sold'] \
#                 .rolling(7, min_periods=1).mean().reset_index(0, drop=True)

# df['avg_30'] = df.groupby('product_id')['total_sold'] \
#                  .rolling(30, min_periods=1).mean().reset_index(0, drop=True)

# df['is_weekend'] = df['day_of_week'].apply(lambda x: 1 if x >= 5 else 0)

# # 🔥 IMPORTANT FIX
# df = df.fillna(0)

# # 🎯 Model
# X = df[["day_of_week","month","prev_sales","avg_7","avg_30","is_weekend"]]
# y = df["total_sold"]

# model = RandomForestRegressor(n_estimators=100)
# model.fit(X, y)

# joblib.dump(model, "model.pkl")

# print("✅ Model trained successfully with REAL data")


# --------------------------------
# Dummy data for testing
# -------------------------------


import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
import joblib

np.random.seed(42)

# 📅 Generate 60 days
dates = pd.date_range(start="2024-01-01", periods=60)

data = []

# 🔥 Simulate order_items table
for product_id in range(1, 6):  # 5 products

    base = np.random.randint(20, 40)

    for i, date in enumerate(dates):

        day_of_week = date.weekday()

        # 🎯 Simulate realistic behavior
        trend = i * 0.2
        weekend_boost = 10 if day_of_week >= 5 else 0
        noise = np.random.randint(-3, 3)

        quantity = base + trend + weekend_boost + noise
        quantity = max(1, int(quantity))

        data.append({
            "product_id": product_id,
            "created_at": date,
            "quantity": quantity
        })

# 👉 This simulates your DB table
df_orders = pd.DataFrame(data)

# 💾 Save for graph + debugging
df_orders.to_csv("dummy_order_items.csv", index=False)

# =====================================
# 🧠 FEATURE ENGINEERING (LIKE BACKEND)
# =====================================

df_orders['day'] = pd.to_datetime(df_orders['created_at']).dt.date

daily_sales = df_orders.groupby(['product_id', 'day'])['quantity'].sum().reset_index()

# Features
daily_sales['day_of_week'] = pd.to_datetime(daily_sales['day']).dt.dayofweek
daily_sales['month'] = pd.to_datetime(daily_sales['day']).dt.month

daily_sales['prev_sales'] = daily_sales.groupby('product_id')['quantity'].shift(1)

daily_sales['avg_7'] = daily_sales.groupby('product_id')['quantity'] \
    .rolling(7, min_periods=1).mean().reset_index(0, drop=True)

daily_sales['avg_30'] = daily_sales.groupby('product_id')['quantity'] \
    .rolling(30, min_periods=1).mean().reset_index(0, drop=True)

daily_sales['is_weekend'] = daily_sales['day_of_week'].apply(lambda x: 1 if x >= 5 else 0)

daily_sales = daily_sales.fillna(0)

# =====================================
# 🎯 TRAIN MODEL
# =====================================

X = daily_sales[["day_of_week", "month", "prev_sales", "avg_7", "avg_30", "is_weekend"]]
y = daily_sales["quantity"]

model = RandomForestRegressor(n_estimators=100)
model.fit(X, y)

joblib.dump(model, "model.pkl")

print("✅ Model trained with repository-compatible dummy data")
print("📊 File created: dummy_order_items.csv")