import pandas as pd
from sklearn.linear_model import LinearRegression
import joblib
import os

# Sample training data (replace with DB data later)
data = {
    "day": [1,2,3,4,5,6,7],
    "sales": [10,15,20,18,25,30,35]
}

df = pd.DataFrame(data)

X = df[["day"]]
y = df["sales"]

model = LinearRegression()
model.fit(X, y)

# Get the directory of this script
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, "model.pkl")
joblib.dump(model, model_path)

print("Model trained and saved!")