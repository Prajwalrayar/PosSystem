from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)
model = joblib.load("model.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.json

    features = [[
        data["day_of_week"],
        data["month"],
        data["prev_sales"],
        data["avg_7"],
        data["avg_30"],
        data["is_weekend"]
    ]]

    prediction = model.predict(features)[0]

    return jsonify({
        "forecast": round(float(prediction), 2)
    })

app.run(port=5000)