from __future__ import annotations

import ast
import math
import random
from collections import Counter, defaultdict
from datetime import date, timedelta
from itertools import combinations
from pathlib import Path
from statistics import mean, pstdev

import pandas as pd

try:
    from schemas import (
        AIAnalysisMode,
        BasketRecommendation,
        BasketResponse,
        BasketSummary,
        DemandChartPoint,
        DemandResponse,
        DemandSummary,
        ForecastHorizon,
        SmartAIRequest,
    )
except ImportError:  # pragma: no cover - supports package execution
    from ..schemas import (
        AIAnalysisMode,
        BasketRecommendation,
        BasketResponse,
        BasketSummary,
        DemandChartPoint,
        DemandResponse,
        DemandSummary,
        ForecastHorizon,
        SmartAIRequest,
    )


class AIEngine:
    def __init__(self, base_dir: Path | None = None) -> None:
        self.base_dir = base_dir or Path(__file__).resolve().parent.parent
        self.basket_catalog, self.basket_pairs, self.basket_item_counts, self.avg_basket_size = self._load_basket_stats()
        self.catalog_lookup = {name.lower(): name for name in self.basket_catalog}
        self.demand_baselines = self._load_demand_name_baselines()

    def analyze(self, payload: SmartAIRequest) -> DemandResponse | BasketResponse:
        if payload.mode == AIAnalysisMode.DEMAND:
            return self._build_demand_response(payload)
        if payload.mode == AIAnalysisMode.BASKET:
            return self._build_basket_response(payload)
        raise ValueError(f"Unsupported mode: {payload.mode}")

    def _build_demand_response(self, payload: SmartAIRequest) -> DemandResponse:
        product_names = self._normalize_product_names(payload.productNames) or self._default_product_names(payload.branchId)
        horizon_steps, step_delta = self._horizon_profile(payload.horizon)
        rng = random.Random(self._seed("demand", payload.branchId, product_names, payload.horizon))

        branch_factor = 1 + ((payload.branchId % 11) - 5) * 0.025
        product_baselines = [
            self.demand_baselines.get(name, 18 + ((sum(ord(ch) for ch in name) % 9) * 2.5))
            for name in product_names
        ]
        base_level = mean(product_baselines) * branch_factor

        seasonal_amplitude = {
            ForecastHorizon.DAY: 0.08,
            ForecastHorizon.WEEK: 0.11,
            ForecastHorizon.MONTH: 0.14,
            ForecastHorizon.YEAR: 0.18,
        }[payload.horizon]

        trend_direction = 1 if (payload.branchId + len(product_names)) % 2 == 0 else -1
        trend_strength = {
            ForecastHorizon.DAY: 0.015,
            ForecastHorizon.WEEK: 0.02,
            ForecastHorizon.MONTH: 0.025,
            ForecastHorizon.YEAR: 0.03,
        }[payload.horizon] * trend_direction

        start_day = date.today() + step_delta
        chart: list[DemandChartPoint] = []
        for index in range(horizon_steps):
            current_day = start_day + (step_delta * index)
            phase = index / max(horizon_steps - 1, 1)
            seasonal = math.sin(phase * math.tau) * seasonal_amplitude
            drift = trend_strength * index
            noise = rng.uniform(-0.04, 0.04)
            value = base_level * (1 + seasonal + drift + noise)
            chart.append(DemandChartPoint(date=current_day, value=round(max(value, 0.0), 2)))

        values = [point.value for point in chart]
        summary = DemandSummary(
            avg=round(mean(values), 2),
            trend=self._classify_trend(values),
            stockRisk=self._classify_stock_risk(values, payload.branchId),
        )
        return DemandResponse(chart=chart, summary=summary)

    def _build_basket_response(self, payload: SmartAIRequest) -> BasketResponse:
        seed_labels = self._normalize_product_names(payload.productNames) or self._default_product_names(payload.branchId)
        seed_set = set(seed_labels)
        candidate_scores: dict[str, dict[str, float]] = {}

        for seed_label in seed_labels:
            co_occurrences = self.basket_pairs.get(seed_label, {})
            seed_count = max(self.basket_item_counts.get(seed_label, 1), 1)
            for partner_label, pair_count in co_occurrences.items():
                if partner_label in seed_set:
                    continue

                partner_count = max(self.basket_item_counts.get(partner_label, 1), 1)
                support = pair_count / max(sum(self.basket_item_counts.values()), 1)
                confidence = pair_count / seed_count
                lift = confidence / max(partner_count / max(sum(self.basket_item_counts.values()), 1), 1e-9)
                key = " + ".join(sorted({seed_label, partner_label}))

                current = candidate_scores.get(key)
                candidate = {
                    "confidence": round(min(confidence, 1.0), 4),
                    "support": round(min(support, 1.0), 4),
                    "lift": round(max(lift, 0.01), 4),
                }
                if current is None or candidate["lift"] > current["lift"]:
                    candidate_scores[key] = candidate

        if not candidate_scores:
            candidate_scores = self._fallback_basket_recommendations(seed_labels)

        recommendations = [
            BasketRecommendation(product=product, **scores)
            for product, scores in sorted(candidate_scores.items(), key=lambda item: (item[1]["lift"], item[1]["confidence"]), reverse=True)[:5]
        ]

        top_combo = recommendations[0].product if recommendations else "No strong basket pattern found"
        summary = BasketSummary(topCombo=top_combo, avgBasketSize=round(self.avg_basket_size, 2))
        return BasketResponse(recommendations=recommendations, summary=summary)

    def _load_demand_name_baselines(self) -> dict[str, float]:
        if not self.basket_item_counts:
            return {}

        max_count = max(self.basket_item_counts.values(), default=1)
        baselines: dict[str, float] = {}
        for name, count in self.basket_item_counts.items():
            normalized = count / max_count
            baselines[name] = round(16 + (normalized * 42), 2)
        return baselines

    def _load_basket_stats(self) -> tuple[list[str], dict[str, dict[str, int]], Counter[str], float]:
        csv_path = self.base_dir / "Retail_store_100k.csv"
        if not csv_path.exists():
            return self._default_catalog(), self._default_pair_counts(), Counter(), 3.2

        try:
            frame = pd.read_csv(csv_path)
        except Exception:
            return self._default_catalog(), self._default_pair_counts(), Counter(), 3.2

        if frame.empty or "Product" not in frame.columns:
            return self._default_catalog(), self._default_pair_counts(), Counter(), 3.2

        transactions: list[list[str]] = []
        basket_sizes: list[int] = []
        for raw_items in frame["Product"].dropna().tolist():
            items = self._parse_transaction_items(raw_items)
            if items:
                basket_sizes.append(len(items))
                transactions.append(items)

        unique_items = sorted({item for transaction in transactions for item in transaction})
        catalog = unique_items if unique_items else self._default_catalog()

        item_counts: Counter[str] = Counter()
        pair_counts: dict[str, dict[str, int]] = defaultdict(lambda: defaultdict(int))
        for transaction in transactions:
            deduped = sorted(set(transaction))
            item_counts.update(deduped)
            for left, right in combinations(deduped, 2):
                pair_counts[left][right] += 1
                pair_counts[right][left] += 1

        average_basket_size = float(mean(basket_sizes)) if basket_sizes else 3.2
        return catalog, pair_counts, item_counts, average_basket_size

    def _parse_transaction_items(self, raw_items: object) -> list[str]:
        if not isinstance(raw_items, str):
            return []

        try:
            parsed = ast.literal_eval(raw_items)
            if isinstance(parsed, list):
                return [str(item).strip() for item in parsed if str(item).strip()]
        except Exception:
            pass

        return [item.strip() for item in raw_items.split(",") if item.strip()]

    def _default_catalog(self) -> list[str]:
        return [
            "Bread",
            "Milk",
            "Butter",
            "Eggs",
            "Coffee",
            "Tea",
            "Rice",
            "Chicken",
            "Apples",
            "Bananas",
            "Cheese",
            "Yogurt",
        ]

    def _default_pair_counts(self) -> dict[str, dict[str, int]]:
        return {
            "Bread": {"Butter": 18, "Milk": 12},
            "Milk": {"Bread": 12, "Cereal": 15},
            "Coffee": {"Sugar": 14, "Milk": 11},
        }

    def _default_product_names(self, branch_id: int) -> list[str]:
        catalog = self.basket_catalog or self._default_catalog()
        if not catalog:
            return ["Bread", "Milk", "Butter"]

        start = branch_id % len(catalog)
        selected = [catalog[(start + offset) % len(catalog)] for offset in range(3)]
        return list(dict.fromkeys(selected))

    def _normalize_product_names(self, names: list[str]) -> list[str]:
        normalized: list[str] = []
        for raw in names:
            candidate = str(raw).strip()
            if not candidate:
                continue

            resolved = self.catalog_lookup.get(candidate.lower(), candidate)
            normalized.append(resolved)

        return list(dict.fromkeys(normalized))

    def _fallback_basket_recommendations(self, seed_labels: list[str]) -> dict[str, dict[str, float]]:
        recommendations: dict[str, dict[str, float]] = {}
        fallback_candidates = [label for label in self.basket_catalog if label not in seed_labels] or self._default_catalog()

        for index, seed_label in enumerate(seed_labels):
            partner = fallback_candidates[index % len(fallback_candidates)]
            combo = " + ".join(sorted({seed_label, partner}))
            base = 0.42 + (index * 0.07)
            recommendations[combo] = {
                "confidence": round(min(base + 0.12, 0.95), 4),
                "support": round(min(base / 2, 0.7), 4),
                "lift": round(1.1 + (index * 0.2), 4),
            }
        return recommendations

    def _horizon_profile(self, horizon: ForecastHorizon) -> tuple[int, timedelta]:
        if horizon == ForecastHorizon.DAY:
            return 7, timedelta(days=1)
        if horizon == ForecastHorizon.WEEK:
            return 8, timedelta(weeks=1)
        if horizon == ForecastHorizon.MONTH:
            return 12, timedelta(days=30)
        if horizon == ForecastHorizon.YEAR:
            return 12, timedelta(days=30)
        return 7, timedelta(days=1)

    def _classify_trend(self, values: list[float]) -> str:
        if len(values) < 2:
            return "STABLE"

        midpoint = max(len(values) // 2, 1)
        first_half = mean(values[:midpoint])
        second_half = mean(values[midpoint:])
        delta = second_half - first_half

        if delta > 1.25:
            return "UP"
        if delta < -1.25:
            return "DOWN"
        return "STABLE"

    def _classify_stock_risk(self, values: list[float], branch_id: int) -> str:
        average_value = mean(values)
        volatility = pstdev(values) / max(average_value, 1.0) if len(values) > 1 else 0.0
        trend = self._classify_trend(values)

        if average_value >= 55 or volatility >= 0.18 or (trend == "UP" and average_value >= 35):
            return "HIGH"
        if average_value >= 30 or volatility >= 0.1 or trend == "UP":
            return "MEDIUM"
        if branch_id % 4 == 0:
            return "MEDIUM"
        return "LOW"

    def _seed(self, kind: str, branch_id: int, product_names: list[str], horizon: ForecastHorizon) -> int:
        product_key = "|".join(product_names)
        horizon_factor = {ForecastHorizon.DAY: 11, ForecastHorizon.WEEK: 17, ForecastHorizon.MONTH: 23, ForecastHorizon.YEAR: 29}[horizon]
        return hash((kind, branch_id, product_key, horizon_factor)) & 0xFFFFFFFF
