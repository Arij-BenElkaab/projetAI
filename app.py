from flask import Flask, request, jsonify
import joblib
import numpy as np
from flask_cors import CORS

app = Flask(__name__)
CORS(app)
gb_fc = joblib.load("models/gb_fuelconsump.pkl")   # Gradient Boosting for fuelConsumpCmb
dt_co2 = joblib.load("models/dt_co2.pkl")          # Decision Tree for co2Emissions

def validate_payload(payload, required_fields):
    missing = [f for f in required_fields if f not in payload]
    return missing

@app.route("/predict-fuel-consumption", methods=["POST"])
def predict_fuel_consumption():
    data = request.get_json(silent=True) or {}
    missing = validate_payload(data, ["engineSize", "cylinderNo"])
    if missing:
        return jsonify({"error": f"Missing fields: {missing}"}), 400
    try:
        engine_size = float(data["engineSize"])
        cylinder_no = float(data["cylinderNo"])
        X = np.array([[engine_size, cylinder_no]])
        pred = gb_fc.predict(X)[0]
        return jsonify({
            "input": {"engineSize": engine_size, "cylinderNo": cylinder_no},
            "prediction": {"fuelConsumpCmb": float(pred)}
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/predict-co2", methods=["POST"])
def predict_co2():
   
    data = request.get_json(silent=True) or {}
    missing = validate_payload(data, ["fuelConsumpCmb", "fuelType"])
    if missing:
        return jsonify({"error": f"Missing fields: {missing}"}), 400
    try:
        fuel_cons = float(data["fuelConsumpCmb"])
        fuel_type = float(data["fuelType"])
        X = np.array([[fuel_cons, fuel_type]])
        pred = dt_co2.predict(X)[0]
        return jsonify({
            "input": {"fuelConsumpCmb": fuel_cons, "fuelType": fuel_type},
            "prediction": {"co2Emissions": float(pred)}
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
