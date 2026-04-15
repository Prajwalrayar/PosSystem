# AI Module

FastAPI service for smart AI analytics with demand and basket analysis.

## Requirements
- Python 3.11+
- The dataset file `Retail_store_300k.csv` must be present in the project root.

## Install Dependencies

From the project root:

```bash
python -m pip install -r requirements.txt
```

## Run the API

You can start the service in any of these ways:

### Option 1: Run the main module directly

```bash
python main.py
```

### Option 2: Run the compatibility launcher

```bash
python app.py
```

### Option 3: Run with Uvicorn

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

If you want to change the port, set `PORT`:

```bash
$env:PORT=8001
python main.py
```

## Health Check

```http
GET /health
```

Example:

```bash
curl http://127.0.0.1:8000/health
```

## Smart AI Endpoint

```http
POST /api/smart-ai
```

### Request Body

Use `product_names` in JSON, not `productNames`:

```json
{
  "mode": "DEMAND",
  "horizon": "MONTH",
  "product_names": ["Product1", "Product2"]
}
```

### Basket Example

```json
{
  "mode": "BASKET",
  "horizon": "WEEK",
  "branchId": 152,
  "product_names": ["soap"]
}
```

## CORS Configuration

Set `CORS_ORIGINS` to a comma-separated list if you do not want wildcard access:

```bash
$env:CORS_ORIGINS="http://localhost:3000,http://127.0.0.1:3000"
python main.py
```

If `CORS_ORIGINS` is not set, the API defaults to `*`.

## Notes

- The request model forbids extra fields, so send only the documented keys.
- `branchId` is optional.
- The API uses the `SmartAIRequest` and `SmartAIResponse` models defined in `schemas.py`.
