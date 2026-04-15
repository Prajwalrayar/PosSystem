import axios from "axios";

const SMART_AI_API_BASE_URL = import.meta.env.VITE_AI_API_BASE_URL || "http://localhost:8000";

const smartAiApi = axios.create({
  baseURL: SMART_AI_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

const normalizeProductNames = (value) =>
  Array.isArray(value)
    ? value.map((item) => String(item).trim()).filter(Boolean)
    : [];

export const buildSmartAiPayload = (payload) => ({
  mode: payload?.mode || "DEMAND",
  branchId: payload?.branchId ? Number(payload.branchId) : undefined,
  horizon: payload?.horizon || "WEEK",
  productNames: normalizeProductNames(payload?.productNames ?? payload?.product_names),
});

export const fetchSmartAiAnalysis = (payload) => {
  return smartAiApi
    .post("/api/smart-ai", buildSmartAiPayload(payload))
    .then((response) => response.data);
};

export default smartAiApi;
