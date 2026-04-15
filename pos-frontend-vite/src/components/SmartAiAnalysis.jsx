import React, { useState } from "react";
import { fetchSmartAiAnalysis } from "../api/smartAi";

const SmartAiAnalysis = () => {
  const [product, setProduct] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleAnalyze = async () => {
    setError(null);
    setResult(null);

    try {
      const response = await fetchSmartAiAnalysis({ product });
      setResult(response);
    } catch (err) {
      setError(err.response?.data?.detail || "An error occurred");
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Smart AI Analysis</h2>
      <input
        type="text"
        value={product}
        onChange={(e) => setProduct(e.target.value)}
        placeholder="Enter product name"
        style={{ padding: "10px", marginRight: "10px" }}
      />
      <button onClick={handleAnalyze} style={{ padding: "10px" }}>
        Analyze
      </button>

      {error && <p style={{ color: "red" }}>Error: {error}</p>}

      {result && (
        <div style={{ marginTop: "20px" }}>
          <h3>Results for: {result.product}</h3>
          <p><strong>Predicted Demand:</strong> {result.predicted_demand} units</p>
          <p><strong>Frequently Bought Together:</strong> {result.recommendations.join(", ")}</p>
        </div>
      )}
    </div>
  );
};

export default SmartAiAnalysis;