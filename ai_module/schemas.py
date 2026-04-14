from __future__ import annotations

from datetime import date
from enum import Enum

from pydantic import BaseModel, ConfigDict, Field


class AIAnalysisMode(str, Enum):
    DEMAND = "DEMAND"
    BASKET = "BASKET"


class ForecastHorizon(str, Enum):
    DAY = "DAY"
    WEEK = "WEEK"
    MONTH = "MONTH"
    YEAR = "YEAR"


class SmartAIRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    mode: AIAnalysisMode
    branchId: int = Field(ge=1)
    horizon: ForecastHorizon
    productNames: list[str] = Field(default_factory=list)


class DemandChartPoint(BaseModel):
    date: date
    value: float


class DemandSummary(BaseModel):
    avg: float
    trend: str
    stockRisk: str


class DemandResponse(BaseModel):
    mode: AIAnalysisMode = AIAnalysisMode.DEMAND
    chart: list[DemandChartPoint]
    summary: DemandSummary


class BasketRecommendation(BaseModel):
    product: str
    confidence: float
    support: float
    lift: float


class BasketSummary(BaseModel):
    topCombo: str
    avgBasketSize: float


class BasketResponse(BaseModel):
    mode: AIAnalysisMode = AIAnalysisMode.BASKET
    recommendations: list[BasketRecommendation]
    summary: BasketSummary


SmartAIResponse = DemandResponse | BasketResponse
