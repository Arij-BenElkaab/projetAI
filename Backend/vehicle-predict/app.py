from flask import Flask, request, jsonify
import joblib
import numpy as np

# ---- Load model bundle ----
# Expected keys in bundle: scaler, model, X_cols, y_col, threshold
bundle = joblib.load("model.pkl")
scaler    = bundle["scaler"]
model     = bundle["model"]
X_cols    = bundle["X_cols"]
y_col     = bundle.get("y_col", None)
threshold = float(bundle.get("threshold", 0.5))

# Pos class index (in case classes_ are not [0,1] in order)
# We'll use whatever index corresponds to the label "1"
classes = getattr(model, "classes_", None)
if classes is None:
    # Fallback: assume binary and positive class is index 1
    pos_index = 1
else:
    classes = list(classes)
    if 1 in classes:
        pos_index = classes.index(1)
    else:
        # If labels are not 0/1, assume positive is the last column
        pos_index = -1

# Optional alternate thresholds (from your earlier tuning)
THRESHOLDS = {
    "balanced": 0.707,       # tuned for F1 in your run
    "high_recall": 0.467,    # tuned for recall >= 0.80
    "default": threshold     # whatever was saved in model.pkl
}

app = Flask(__name__)
from flask_cors import CORS
CORS(app)  # allow http://localhost:4200 to call the API


# ---- Helpers ----
def choose_threshold(req):
    """Pick a threshold via query params:
       - ?mode=balanced or ?mode=high_recall
       - ?thr=0.42 to force a custom float threshold
       Falls back to bundle's saved 'threshold'.
    """
    # explicit numeric override
    thr_param = req.args.get("thr")
    if thr_param is not None:
        try:
            return float(thr_param)
        except ValueError:
            pass  # ignore bad value and continue

    # named mode
    mode = req.args.get("mode")
    if mode in THRESHOLDS:
        return THRESHOLDS[mode]

    return THRESHOLDS["default"]

# ---- Routes ----

@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "🚀 Vehicle failure prediction API is running."})

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "ok": True,
        "expected_features": X_cols,
        "saved_threshold": threshold,
        "available_modes": list(THRESHOLDS.keys()),
        "model_classes": classes,
        "positive_class_index": pos_index
    })

@app.route("/schema", methods=["GET"])
def schema():
    # Provide an example payload for quick copy-paste
    example = {
        "Air temperature [K]": 300,
        "Process temperature [K]": 310,
        "Rotational speed [rpm]": 1600,
        "Torque [Nm]": 35,
        "Tool wear [min]": 200
    }
    # Keep only keys present in X_cols (handles variants)
    example = {k: v for k, v in example.items() if k in X_cols}
    return jsonify({
        "required_json_keys": X_cols,
        "example": example
    })

@app.route("/predict", methods=["POST"])
def predict():
    # 1) Parse JSON
    try:
        data = request.get_json(force=True)
        X_input = np.array([[data[col] for col in X_cols]], dtype=float)
    except KeyError as e:
        return jsonify({
            "error": f"Missing feature: {str(e)}",
            "required": X_cols
        }), 400
    except Exception as e:
        return jsonify({
            "error": f"Invalid JSON or values: {e}",
            "required": X_cols
        }), 400

    # 2) Preprocess
    X_scaled = scaler.transform(X_input)

    # 3) Probability of the positive class
    proba_vec = model.predict_proba(X_scaled)[0]
    prob = float(proba_vec[pos_index])

    # 4) Pick threshold (mode or thr=)
    thr = choose_threshold(request)
    pred = int(prob >= thr)

    # 5) (Optional) z-scores for transparency
    z_scores = [float(x) for x in X_scaled[0]]

    # Simple server-side debug (shows in your console)
    print(f"[DEBUG] p(failure)={prob:.4f} thr={thr:.3f} pred={pred} input={data}")

    return jsonify({
        "prediction": "panne_probable" if pred == 1 else "aucune_panne",
        "probability": round(prob, 3),
        "threshold_used": thr,
        "mode_hint": request.args.get("mode", "default"),
        "features_used": X_cols,
        "z_scores": [round(z, 3) for z in z_scores]
    })

if __name__ == "__main__":
    # debug=True is fine for local development only
    app.run(host="127.0.0.1", port=5000, debug=True)
