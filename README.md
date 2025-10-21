# Vehicle Fuel Consumption & CO₂ Emission Prediction API

A machine learning-powered Flask API that predicts vehicle fuel consumption and CO₂ emissions based on engine characteristics and fuel type.

## Overview

This project provides two predictive models:

1. **Fuel Consumption Prediction**: Estimates fuel consumption (L/100 km) from engine size and cylinder count
2. **CO₂ Emissions Prediction**: Calculates CO₂ emissions (g/km) from fuel consumption and fuel type

| Prediction Task | Features | Target | Model | R² Score |
|---|---|---|---|---|
| Fuel Consumption | Engine Size, Cylinder Count | Fuel Consumption | Gradient Boosting Regressor | 0.766 |
| CO₂ Emissions | Fuel Consumption, Fuel Type | CO₂ Emissions | Decision Tree Regressor | 0.981 |

## Tech Stack

- Python 3.10+
- Pandas, NumPy
- Scikit-learn
- Flask
- Joblib

## Project Structure

```
vehicle-emission-api/
├── app.py                      # Flask API application
├── models/
│   ├── gb_fuelconsump.pkl     # Gradient Boosting model (fuel consumption)
│   └── dt_co2.pkl             # Decision Tree model (CO₂ emissions)
├── model_training.ipynb
├── requirements.txt            # Dependencies
└── README.md                   # Documentation
```

## Installation

### Prerequisites
- Python 3.10 or higher
- pip package manager

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/ahmeddrid14/Transport-App.git
   cd Transport-App
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   source venv/bin/activate      # macOS/Linux
   venv\Scripts\activate         # Windows
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Run the API**
   ```bash
   python app.py
   ```

The API will be available at `http://localhost:5000`

## API Endpoints

### 1. Predict Fuel Consumption

**Endpoint**: `POST /predict-fuel-consumption`

**Request**:
```json
{
  "engineSize": 2.5,
  "cylinderNo": 4
}
```

**Response**:
```json
{
  "input": {
    "engineSize": 2.5,
    "cylinderNo": 4
  },
  "prediction": {
    "fuelConsumpCmb": 7.8
  }
}
```

**Example**:
```bash
curl -X POST http://localhost:5000/predict-fuel-consumption \
  -H "Content-Type: application/json" \
  -d '{"engineSize": 3.5, "cylinderNo": 6}'
```

---

### 2. Predict CO₂ Emissions

**Endpoint**: `POST /predict-co2`

**Request**:
```json
{
  "fuelConsumpCmb": 7.8,
  "fuelType": 1
}
```

**Note**: `fuelType` is numerically encoded (e.g., Petrol = 0, Diesel = 1)

**Response**:
```json
{
  "input": {
    "fuelConsumpCmb": 7.8,
    "fuelType": 1
  },
  "prediction": {
    "co2Emissions": 180.5
  }
}
```

**Example**:
```bash
curl -X POST http://localhost:5000/predict-co2 \
  -H "Content-Type: application/json" \
  -d '{"fuelConsumpCmb": 9.2, "fuelType": 1}'
```

## Model Performance

### Fuel Consumption Models

| Model | MAE | RMSE | R² |
|---|---|---|---|
| Gradient Boosting | 1.008 | 1.396 | 0.767 |
| Decision Tree | 1.012 | 1.400 | 0.765 |
| Random Forest | 1.018 | 1.406 | 0.763 |
| Linear Regression | 1.217 | 1.642 | 0.677 |
| SVR | 1.198 | 1.662 | 0.669 |

### CO₂ Emission Models

| Model | MAE | RMSE | R² |
|---|---|---|---|
| Decision Tree | 2.821 | 8.780 | 0.981 |
| Gradient Boosting | 3.244 | 8.712 | 0.981 |
| Random Forest | 2.923 | 8.901 | 0.980 |
| Linear Regression | 5.973 | 16.109 | 0.935 |
| SVR | 7.736 | 18.404 | 0.915 |

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.

## Author

[Ahmed Drid](https://github.com/ahmeddrid14)