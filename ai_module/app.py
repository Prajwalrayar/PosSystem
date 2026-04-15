"""Backward-compatible launcher for the FastAPI service."""

try:
    from main import app
except ImportError:  # pragma: no cover - supports package execution
    from .main import app


if __name__ == "__main__":
    import os

    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", "8000")), reload=False)
