from __future__ import annotations

import os

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

try:
    from schemas import SmartAIRequest, SmartAIResponse
    from services.ai_engine import AIEngine
except ImportError:  # pragma: no cover - supports package execution
    from .schemas import SmartAIRequest, SmartAIResponse
    from .services.ai_engine import AIEngine


def _build_cors_origins() -> list[str]:
    raw_origins = os.getenv("CORS_ORIGINS", "*")
    origins = [origin.strip() for origin in raw_origins.split(",") if origin.strip()]
    return origins or ["*"]


cors_origins = _build_cors_origins()


app = FastAPI(title="Smart AI Analytics API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=cors_origins != ["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = AIEngine()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/smart-ai", response_model=SmartAIResponse)
def smart_ai(payload: SmartAIRequest) -> SmartAIResponse:
    try:
        return engine.analyze(payload)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", "8000")), reload=False)
