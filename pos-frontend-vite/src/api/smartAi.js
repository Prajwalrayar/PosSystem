import axios from "axios";

const SMART_AI_API_BASE_URL = import.meta.env.VITE_AI_API_BASE_URL || "http://localhost:8000";

const smartAiApi = axios.create({
  baseURL: SMART_AI_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const fetchSmartAiAnalysis = (payload) => {
  return smartAiApi.post("/api/smart-ai", payload).then((response) => response.data);
};

export default smartAiApi;
