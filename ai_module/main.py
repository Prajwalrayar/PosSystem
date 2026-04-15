from __future__ import annotations

import os
import asyncio
import logging

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware

# Configure logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger("smart_ai")

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


# Debug log to verify CORS origins
print(f"DEBUG: CORS origins set to {cors_origins}")


app = FastAPI(title="Smart AI Analytics API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=cors_origins != ["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

engine = AIEngine()

# Debug log to verify catalog_lookup initialization
if not engine.catalog_lookup:
    print("DEBUG: catalog_lookup is empty after AIEngine initialization.")
else:
    print(f"DEBUG: catalog_lookup initialized with {len(engine.catalog_lookup)} entries.")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/smart-ai", response_model=SmartAIResponse)
def smart_ai(payload: SmartAIRequest) -> SmartAIResponse:
    try:
        logger.debug(f"Received payload: {payload}")
        response = engine.analyze(payload)
        logger.debug(f"Response: {response}")
        return response
    except ValueError as exc:
        logger.error(f"ValueError: {exc}")
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.error(f"Unhandled exception: {exc}")
        raise HTTPException(status_code=500, detail="Internal Server Error") from exc


@app.on_event("shutdown")
async def shutdown_event():
    try:
        print("INFO: Application is shutting down...")
        # Perform any cleanup tasks here
    except asyncio.CancelledError:
        print("WARNING: Shutdown process was interrupted by CancelledError.")
    finally:
        print("INFO: Shutdown process completed.")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", "8000")), reload=False)
