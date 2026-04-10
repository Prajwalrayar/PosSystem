# AI Module Architecture

This folder contains the AI layer for the POS system. The goal is to keep machine learning separate from the main Spring Boot backend so models can be trained, versioned, and served independently.

## Current State

The current codebase is a minimal prototype:

- `model.py` trains a simple regression model and saves it to `model.pkl`
- `app.py` loads `model.pkl` and exposes a Flask `/predict` API
- `requirement.txt` lists the Python dependencies

Right now, the model uses sample data only. It does not yet read from the POS database.

## Target Architecture

```text
POS Database (MySQL)
    |
    v
Feature Builder / ETL
    |
    v
Model Training Pipeline
    |
    v
Saved Model Artifacts (model.pkl + metadata)
    |
    v
Flask AI Service
    |
    v
Spring Boot Backend
    |
    v
Frontend Dashboard / Alerts / Recommendations
```

## Separate Model Service Design

The model should be deployed as an independent service with its own public or internal URL.

```text
Spring Boot App  --->  Model Service URL  --->  Flask Inference API  --->  Saved Model
```

This keeps the POS application clean and makes the AI layer replaceable, scalable, and easier to test.

### Recommended runtime setup

- Spring Boot keeps a config value like `AI_SERVICE_BASE_URL`
- Flask runs separately on a port or in a container
- The backend calls the model API over HTTP
- The frontend never talks to the model directly unless needed

### Example URLs

- Local development: `http://localhost:5001`
- Staging: `http://ai-service:5001`
- Production: `https://ai.yourdomain.com`

## What This AI Layer Should Do

### 1. Demand Forecasting

Predict future product sales by:

- product
- branch
- day
- hour
- seasonality

Use cases:

- stock planning
- low-stock alerts
- reorder suggestions
- sales trend forecasting

### 2. Queue / Load Prediction

Predict busy periods from order timestamps and branch activity.

Use cases:

- cashier staffing suggestions
- peak-hour alerts
- shift planning

### 3. Basket Analysis

Find products that are frequently bought together.

Use cases:

- cross-sell recommendations
- combo offers
- checkout suggestions

## Data Flow

### Training Flow

1. Spring Boot reads POS data from MySQL.
2. Data is transformed into training features.
3. Python training script trains a model.
4. Model is saved as `model.pkl`.
5. Optional metadata is saved for model versioning.

### Prediction Flow

1. Frontend or backend requests a prediction.
2. Spring Boot sends a request to the Flask service.
3. Flask loads the saved model.
4. The model returns a prediction.
5. Spring Boot uses the response in dashboards, alerts, or checkout logic.

## Suggested API Design

### Demand Forecast

`POST /predict/demand`

Request:

```json
{
  "product_id": 101,
  "branch_id": 12,
  "day_of_week": 2,
  "hour": 14,
  "last_7_day_avg": 24.5
}
```

Response:

```json
{
  "predicted_demand": 28.4
}
```

### How Spring Boot should use it

The Java backend should call the model service like this:

```text
AI_SERVICE_BASE_URL + /predict/demand
AI_SERVICE_BASE_URL + /predict/load
AI_SERVICE_BASE_URL + /recommend/products
```

Spring Boot should:

- build the request payload from POS data
- send the request to the AI service
- receive the prediction response
- store or display the result in analytics dashboards

### Queue Prediction

`POST /predict/load`

Response example:

```json
{
  "expected_orders_next_hour": 62,
  "recommended_cashiers": 3
}
```

### Basket Recommendations

`POST /recommend/products`

Response example:

```json
{
  "product_id": 101,
  "recommendations": [
    { "product_id": 205, "confidence": 0.82 },
    { "product_id": 309, "confidence": 0.61 }
  ]
}
```

## Model Lifecycle

```text
Extract data -> Clean data -> Engineer features -> Train model
-> Validate model -> Save artifact -> Serve predictions
-> Monitor accuracy -> Retrain periodically
```

Recommended model lifecycle rules:

- retrain daily or weekly depending on data volume
- store model version and training date
- compare new model accuracy against the old one
- keep a fallback model if the new one underperforms

## How This Fits The Main Project

The AI module should support the existing POS backend, not replace it.

Good integration points:

- after order creation, update demand statistics
- after shift close, update load and cashier performance metrics
- on product pages, show recommended add-on products
- on inventory dashboards, show stockout risk
- on analytics pages, show forecast charts and alerts

### Integration pattern in the main app

1. User opens dashboard or completes a transaction.
2. Spring Boot collects current business data.
3. Spring Boot sends that data to the AI service URL.
4. AI service returns prediction or recommendation JSON.
5. Spring Boot maps the response into dashboard cards, alerts, or suggestion widgets.

This design lets you build a real modular architecture:

- core POS backend for business logic
- external AI service for prediction
- frontend for visualization only

Relevant backend areas:

- `pos-backend/src/main/java/com/zosh/controller/BranchAnalyticsController.java`
- `pos-backend/src/main/java/com/zosh/controller/StoreAnalyticsController.java`
- `pos-backend/src/main/java/com/zosh/service/ShiftReportService.java`

## Recommended Folder Structure

```text
ai_module/
├── app.py              # Flask inference API
├── model.py            # training script
├── model.pkl           # saved trained model
├── requirements.txt    # Python dependencies
├── data/               # exported training data
├── training/           # feature engineering and model code
├── inference/          # prediction helpers
└── README.md           # architecture documentation
```

## Important Notes

- `model.py` is only a prototype today.
- The real system should train on POS transaction data.
- The AI module should stay independent so it can be scaled or replaced without touching the Java backend.
- Start with demand forecasting first, then add queue prediction and basket analysis.
- Treat the model service as a separate deployable module with its own API contract.

## Next Step

The best next implementation is:

1. export order data from the backend
2. build training features from that data
3. replace the sample dataset in `model.py`
4. expand `app.py` into multiple endpoints
5. connect Spring Boot to the Flask service
