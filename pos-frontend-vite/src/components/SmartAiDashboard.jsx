import React, { useState } from "react";
import { fetchSmartAiAnalysis } from "../api/smartAi";

const SmartAiDashboard = () => {
  const [payload, setPayload] = useState({
    mode: "DEMAND", // or "BASKET"
    productNames: [],
    branchId: 1,
    horizon: "WEEK",
  });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetchSmartAiAnalysis(payload);
      setResult(response);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Smart AI Dashboard</h1>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSubmit();
        }}
      >
        <label>
          Mode:
          <select
            value={payload.mode}
            onChange={(e) => setPayload({ ...payload, mode: e.target.value })}
          >
            <option value="DEMAND">Demand</option>
            <option value="BASKET">Basket</option>
          </select>
        </label>
        <label>
          Product Names (comma-separated):
          <input
            type="text"
            value={payload.productNames.join(",")}
            onChange={(e) =>
              setPayload({ ...payload, productNames: e.target.value.split(",") })
            }
          />
        </label>
        <label>
          Branch ID:
          <input
            type="number"
            value={payload.branchId}
            onChange={(e) => setPayload({ ...payload, branchId: Number(e.target.value) })}
          />
        </label>
        <label>
          Horizon:
          <select
            value={payload.horizon}
            onChange={(e) => setPayload({ ...payload, horizon: e.target.value })}
          >
            <option value="DAY">Day</option>
            <option value="WEEK">Week</option>
            <option value="MONTH">Month</option>
            <option value="YEAR">Year</option>
          </select>
        </label>
        <button type="submit" disabled={loading}>
          {loading ? "Loading..." : "Analyze"}
        </button>
      </form>

      {error && <p style={{ color: "red" }}>Error: {error}</p>}
      {result && (
        <div>
          <h2>Analysis Result</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

export default SmartAiDashboard;