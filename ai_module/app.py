from flask import Flask, request, jsonify
import joblib
import os

app = Flask(__name__)

# Get the directory of this script
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, "model.pkl")
model = joblib.load(model_path)

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    day = data.get("day")

    prediction = model.predict([[day]])[0]

    return jsonify({
        "predicted_demand": round(prediction, 2)
    })

if __name__ == '__main__':
    app.run(port=5001)