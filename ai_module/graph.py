import streamlit as st
import plotly.graph_objects as go
import plotly.express as px
import pandas as pd
import mysql.connector
import joblib
import numpy as np
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from datetime import datetime

# =============================
# 🔗 DB CONNECTION
# =============================
conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="Prajwal@0103",
    database="cp_test"
)


# =============================
# 📂 STREAMLIT UI FOR SELECTIONS
# =============================
st.title("Sales Prediction Dashboard")

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="Prajwal@0103",
    database="cp_test"
)

# Fetch categories
cat_query = "SELECT id, name FROM categories"
categories = pd.read_sql(cat_query, conn)
cat_options = categories['name'].tolist()
cat_selected = st.selectbox("Select Category (required)", cat_options, index=None)

if cat_selected:
    category_id = categories[categories['name'] == cat_selected]['id'].values[0]
    # Fetch products for selected category
    prod_query = f"SELECT id, name FROM products WHERE category_id = {category_id}"
    products = pd.read_sql(prod_query, conn)
    prod_options = ["All Products"] + products['name'].tolist()
    prod_selected = st.selectbox("Select Product (optional)", prod_options, index=0)
else:
    st.warning("Please select a category.")
    st.stop()

# Date pickers (required)
col1, col2 = st.columns(2)
with col1:
    start_date = st.date_input("Start Date (required)", value=None)
with col2:
    end_date = st.date_input("End Date (required)", value=None)

if not start_date or not end_date:
    st.warning("Please select both start and end dates.")
    st.stop()

# Get product_id if a specific product is selected
if prod_selected and prod_selected != "All Products":
    product_id = products[products['name'] == prod_selected]['id'].values[0]
else:
    product_id = None

# =============================
# � FETCH SALES DATA (JOIN)
# =============================
if product_id:
    query = f"""
    SELECT 
        DATE(o.created_at) as day,
        SUM(oi.quantity) as total_sold
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.id
    WHERE oi.product_id = {product_id}
    AND DATE(o.created_at) BETWEEN '{start_date}' AND '{end_date}'
    GROUP BY DATE(o.created_at)
    ORDER BY day
    """
else:
    query = f"""
    SELECT 
        DATE(o.created_at) as day,
        SUM(oi.quantity) as total_sold
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.id
    JOIN products p ON oi.product_id = p.id
    WHERE p.category_id = {category_id}
    AND DATE(o.created_at) BETWEEN '{start_date}' AND '{end_date}'
    GROUP BY DATE(o.created_at)
    ORDER BY day
    """

df = pd.read_sql(query, conn)

if df.empty:
    st.error("No sales data found for the selected options.")
    st.stop()

# =============================
# 🔥 FILL MISSING DAYS
# =============================
df['day'] = pd.to_datetime(df['day'])
df = df.set_index('day').asfreq('D').fillna(0).reset_index()

# =============================
# 🧠 FEATURE ENGINEERING
# =============================
df['day_of_week'] = df['day'].dt.dayofweek
df['month'] = df['day'].dt.month
df['prev_sales'] = df['total_sold'].shift(1)
df['avg_7'] = df['total_sold'].rolling(7, min_periods=1).mean()
df['avg_30'] = df['total_sold'].rolling(30, min_periods=1).mean()
df['is_weekend'] = df['day_of_week'].apply(lambda x: 1 if x >= 5 else 0)

df = df.fillna(0)

# =============================
# 🤖 LOAD MODEL
# =============================

X = df[["day_of_week","month","prev_sales","avg_7","avg_30","is_weekend"]]
y_true = df["total_sold"].values
try:
    import os
    model_path = os.path.join(os.path.dirname(__file__), "model.pkl")
    model = joblib.load(model_path)
    y_pred = model.predict(X)
except Exception as e:
    st.error(f"Model loading/prediction error: {e}")
    st.stop()

# =============================
# 📊 METRICS
# =============================
mae = mean_absolute_error(y_true, y_pred)
rmse = np.sqrt(mean_squared_error(y_true, y_pred))
r2 = r2_score(y_true, y_pred)


# =============================
# 📊 GRAPHS: SALES PREDICTION & METRICS BAR CHART
# =============================

# GRAPH 1: SALES PREDICTION (Interactive with Plotly)
fig1 = go.Figure()

fig1.add_trace(go.Scatter(
    x=df['day'],
    y=y_true,
    mode='lines+markers',
    name='Actual',
    line=dict(color='blue'),
    hovertemplate='<b>Date:</b> %{x}<br><b>Actual Sales:</b> %{y}<extra></extra>'
))

fig1.add_trace(go.Scatter(
    x=df['day'],
    y=y_pred,
    mode='lines+markers',
    name='Predicted',
    line=dict(color='red'),
    marker=dict(symbol='x'),
    hovertemplate='<b>Date:</b> %{x}<br><b>Predicted Sales:</b> %{y}<extra></extra>'
))

fig1.update_layout(
    title='Demand Prediction',
    xaxis_title='Date',
    yaxis_title='Sales',
    hovermode='x unified',
    height=500
)

# GRAPH 2: METRICS BAR CHART (Interactive with Plotly)
metrics = ["MAE", "RMSE", "R2"]
values = [mae, rmse, r2]

fig2 = go.Figure(data=[
    go.Bar(
        x=metrics,
        y=values,
        marker_color=['#1f77b4', '#ff7f0e', '#2ca02c'],
        hovertemplate='<b>%{x}</b><br>Value: %{y:.4f}<extra></extra>'
    )
])

fig2.update_layout(
    title='Model Performance',
    yaxis_title='Value',
    hovermode='closest',
    height=500
)

# Display both charts side by side
col1, col2 = st.columns(2)
with col1:
    st.plotly_chart(fig1, use_container_width=True)
with col2:
    st.plotly_chart(fig2, use_container_width=True)