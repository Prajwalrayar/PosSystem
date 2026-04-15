import React, { useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/components/ui/use-toast";
import { fetchSmartAiAnalysis } from "@/api/smartAi";
import { getAllBranchesByStore } from "@/Redux Toolkit/features/branch/branchThunks";
import { getProductsByStore } from "@/Redux Toolkit/features/product/productThunks";
import {
  Brain,
  ShieldAlert,
  Sparkles,
  TrendingUp,
  PackageSearch,
  Check,
  Plus,
  X,
} from "lucide-react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

const MODE_LABELS = {
  DEMAND: "Demand Prediction",
  BASKET: "Basket Analysis",
};

const TREND_COLORS = {
  UP: "text-emerald-600 bg-emerald-50",
  DOWN: "text-red-600 bg-red-50",
  STABLE: "text-slate-600 bg-slate-100",
};

const RISK_COLORS = {
  LOW: "text-emerald-700 bg-emerald-50",
  MEDIUM: "text-amber-700 bg-amber-50",
  HIGH: "text-red-700 bg-red-50",
};

const initialFormState = {
  mode: "DEMAND",
  branchId: "",
  horizon: "WEEK",
};

export default function SmartAiModule() {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const { userProfile } = useSelector((state) => state.user);
  const branch = useSelector((state) => state.branch.branch);
  const { store } = useSelector((state) => state.store);
  const { branches } = useSelector((state) => state.branch);
  const { products } = useSelector((state) => state.product);

  const [formState, setFormState] = useState(initialFormState);
  const [selectedProductNames, setSelectedProductNames] = useState([]);
  const [productToAdd, setProductToAdd] = useState("");
  const [result, setResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(false);

  useEffect(() => {
    if (!store?.id) return;

    const loadData = async () => {
      try {
        setIsLoadingData(true);
        await Promise.all([
          dispatch(
            getAllBranchesByStore({
              storeId: store.id,
              jwt: localStorage.getItem("jwt"),
            })
          ).unwrap(),
          dispatch(getProductsByStore(store.id)).unwrap(),
        ]);
      } catch (error) {
        toast({
          title: "Failed to load Smart AI data",
          description: error || "Could not fetch branches and products",
          variant: "destructive",
        });
      } finally {
        setIsLoadingData(false);
      }
    };

    loadData();
  }, [dispatch, store?.id]);

  useEffect(() => {
    const preferredBranchId = branch?.id ?? userProfile?.branchId ?? "";
    if (!preferredBranchId) return;

    setFormState((current) =>
      current.branchId
        ? current
        : {
            ...current,
            branchId: String(preferredBranchId),
          }
    );
  }, [branch?.id, userProfile?.branchId]);

  useEffect(() => {
    if (!products?.length || selectedProductNames.length > 0) return;
    setSelectedProductNames(products.slice(0, 5).map((product) => product.name));
  }, [products]);

  const demandChartData = useMemo(() => {
    if (result?.mode !== "DEMAND" || !result.chart) return [];

    return result.chart.map((point) => ({
      label: new Date(point.date).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
      }),
      value: point.value,
    }));
  }, [result]);

  const formatCurrency = (value) =>
    new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(value || 0);

  const toggleProductSelection = (productName) => {
    setSelectedProductNames((current) =>
      current.includes(productName)
        ? current.filter((name) => name !== productName)
        : [...current, productName]
    );
  };

  const addProductFromDropdown = () => {
    if (!productToAdd) return;
    setSelectedProductNames((current) =>
      current.includes(productToAdd) ? current : [...current, productToAdd]
    );
    setProductToAdd("");
  };

  const removeProduct = (productName) => {
    setSelectedProductNames((current) =>
      current.filter((name) => name !== productName)
    );
  };

  const handleAnalyze = async (event) => {
    event.preventDefault();

    const fallbackBranchId =
      formState.branchId ||
      branch?.id ||
      userProfile?.branchId ||
      branches?.[0]?.id ||
      "";
    const branchId = Number(fallbackBranchId);
    if (!Number.isInteger(branchId) || branchId < 1) {
      toast({
        title: "Invalid branch",
        description: "Please select a valid branch to run Smart AI.",
        variant: "destructive",
      });
      return;
    }

    const payload = {
      mode: formState.mode,
      branchId,
      horizon: formState.horizon,
      productNames: selectedProductNames,
    };

    setIsLoading(true);
    try {
      const response = await fetchSmartAiAnalysis(payload);
      setResult(response);
      toast({
        title: "Smart AI analysis ready",
        description: `${MODE_LABELS[response.mode]} completed successfully.`,
      });
    } catch (error) {
      const message =
        error.response?.data?.detail ||
        error.response?.data?.message ||
        error.message ||
        "Failed to run Smart AI analysis";
      toast({
        title: "Smart AI error",
        description: message,
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="border border-slate-200 bg-white shadow-sm overflow-hidden">
      <CardHeader className="border-b border-slate-100 bg-slate-50/70">
        <div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
          <div className="space-y-1">
            <CardTitle className="text-2xl font-bold tracking-tight flex items-center gap-2 text-slate-900">
              <Sparkles className="h-6 w-6 text-emerald-400" />
              Smart AI Analytics
            </CardTitle>
            <p className="text-sm text-slate-500">
              Dedicated Smart AI page for demand prediction and basket analysis.
            </p>
          </div>
          <div className="flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-xs text-emerald-700">
            <Brain className="h-4 w-4 text-emerald-400" />
            Python AI API connected to {import.meta.env.VITE_AI_API_BASE_URL}
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-6 lg:p-8">
        <form onSubmit={handleAnalyze} className="grid items-stretch gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
          <div className="h-full space-y-4 rounded-2xl border border-slate-200 bg-slate-50/60 p-5">
            <div className="grid gap-2">
              <Label htmlFor="smart-ai-mode" className="text-slate-700">
                Analysis mode
              </Label>
              <Select
                value={formState.mode}
                onValueChange={(value) =>
                  setFormState((current) => ({ ...current, mode: value }))
                }
              >
                <SelectTrigger id="smart-ai-mode" className="bg-white border-slate-200 text-slate-900">
                  <SelectValue placeholder="Choose a mode" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DEMAND">Demand Prediction</SelectItem>
                  <SelectItem value="BASKET">Basket Analysis</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="smart-ai-branch" className="text-slate-700">
                Branch
              </Label>
              <Select
                value={formState.branchId}
                onValueChange={(value) =>
                  setFormState((current) => ({ ...current, branchId: value }))
                }
              >
                <SelectTrigger id="smart-ai-branch" className="bg-white border-slate-200 text-slate-900">
                  <SelectValue placeholder="Select branch" />
                </SelectTrigger>
                <SelectContent>
                  {branches.map((item) => (
                    <SelectItem key={item.id} value={String(item.id)}>
                      {item.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="smart-ai-horizon" className="text-slate-700">
                Forecast horizon
              </Label>
              <Select
                value={formState.horizon}
                onValueChange={(value) =>
                  setFormState((current) => ({ ...current, horizon: value }))
                }
              >
                <SelectTrigger id="smart-ai-horizon" className="bg-white border-slate-200 text-slate-900">
                  <SelectValue placeholder="Choose horizon" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DAY">Day</SelectItem>
                  <SelectItem value="WEEK">Week</SelectItem>
                  <SelectItem value="MONTH">Month</SelectItem>
                  <SelectItem value="YEAR">Year</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid gap-2">
              <div className="flex items-center justify-between">
                <Label className="text-slate-700">Products</Label>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="h-7 border-slate-200 bg-white text-slate-700 hover:bg-slate-100"
                    onClick={() => setSelectedProductNames(products.map((product) => product.name))}
                  >
                    Select all
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="h-7 border-slate-200 bg-white text-slate-700 hover:bg-slate-100"
                    onClick={() => setSelectedProductNames([])}
                  >
                    Clear
                  </Button>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Select value={productToAdd} onValueChange={setProductToAdd}>
                  <SelectTrigger className="bg-white border-slate-200 text-slate-900">
                    <SelectValue placeholder="Select product item" />
                  </SelectTrigger>
                  <SelectContent>
                    {products.map((product) => (
                      <SelectItem key={product.id || product.name} value={product.name}>
                        {product.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button
                  type="button"
                  className="bg-emerald-600 hover:bg-emerald-700"
                  onClick={addProductFromDropdown}
                  disabled={!productToAdd}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Add
                </Button>
              </div>

              <div className="flex flex-wrap gap-2 rounded-lg border border-slate-200 bg-white p-2 min-h-12">
                {selectedProductNames.length === 0 ? (
                  <span className="text-xs text-slate-400 px-1 py-2">No items selected</span>
                ) : (
                  selectedProductNames.map((name) => (
                    <span
                      key={name}
                      className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700"
                    >
                      <Check className="h-3.5 w-3.5" />
                      {name}
                      <button
                        type="button"
                        onClick={() => removeProduct(name)}
                        className="text-emerald-700/70 hover:text-emerald-900"
                      >
                        <X className="h-3.5 w-3.5" />
                      </button>
                    </span>
                  ))
                )}
              </div>

              <p className="text-xs text-slate-500">
                Selected: {selectedProductNames.length} of {products.length || 0}
              </p>
            </div>

            <Button
              type="submit"
              className="w-full bg-emerald-500 text-slate-950 hover:bg-emerald-400"
              disabled={isLoading || isLoadingData}
            >
              {isLoading ? "Analyzing..." : "Run Smart AI"}
            </Button>

            <p className="text-xs text-slate-500">
              Smart AI uses branch and product lists from your store data, not manual ID entry.
            </p>
          </div>

          <div className="h-full space-y-6">
            {isLoadingData ? (
              <Card className="h-full border border-slate-200 bg-white text-slate-900">
                <CardContent className="flex h-full min-h-[520px] items-center justify-center text-slate-500">
                  Loading branches and products...
                </CardContent>
              </Card>
            ) : result ? (
              <>
                <div className="grid gap-4 md:grid-cols-3">
                  <MetricCard
                    icon={<TrendingUp className="h-4 w-4" />}
                    label="Mode"
                    value={MODE_LABELS[result.mode] || result.mode}
                    note="Unified AI endpoint"
                  />
                  {result.mode === "DEMAND" ? (
                    <>
                      <MetricCard
                        icon={<TrendingUp className="h-4 w-4" />}
                        label="Average demand"
                        value={formatCurrency(result.summary.avg)}
                        note={`Trend: ${result.summary.trend}`}
                      />
                      <MetricCard
                        icon={<ShieldAlert className="h-4 w-4" />}
                        label="Stock risk"
                        value={result.summary.stockRisk}
                        note="Inventory planning signal"
                      />
                    </>
                  ) : (
                    <>
                      <MetricCard
                        icon={<PackageSearch className="h-4 w-4" />}
                        label="Top combo"
                        value={result.summary.topCombo}
                        note={`Avg basket size: ${result.summary.avgBasketSize.toFixed(2)}`}
                      />
                      <MetricCard
                        icon={<ShieldAlert className="h-4 w-4" />}
                        label="Recommendations"
                        value={String(result.recommendations.length)}
                        note="Association outputs"
                      />
                    </>
                  )}
                </div>

                {result.mode === "DEMAND" ? (
                  <div className="grid gap-6 xl:grid-cols-[minmax(0,1.4fr)_minmax(320px,0.6fr)]">
                    <Card className="border-slate-200 bg-white text-slate-900">
                      <CardHeader>
                        <CardTitle className="text-lg">Forecast chart</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="h-72">
                          <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={demandChartData}>
                              <XAxis dataKey="label" stroke="#94a3b8" tickLine={false} axisLine={false} fontSize={12} />
                              <YAxis stroke="#94a3b8" tickLine={false} axisLine={false} fontSize={12} />
                              <Tooltip
                                formatter={(value) => [formatCurrency(value), "Demand"]}
                                contentStyle={{ background: "#ffffff", border: "1px solid #e2e8f0", borderRadius: "12px", color: "#0f172a" }}
                              />
                              <Line
                                type="monotone"
                                dataKey="value"
                                stroke="#34d399"
                                strokeWidth={3}
                                dot={{ r: 4, fill: "#34d399" }}
                                activeDot={{ r: 6 }}
                              />
                            </LineChart>
                          </ResponsiveContainer>
                        </div>
                      </CardContent>
                    </Card>

                    <Card className="border-slate-200 bg-white text-slate-900">
                      <CardHeader>
                        <CardTitle className="text-lg">AI Summary</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <Pill label="Trend" value={result.summary.trend} tone={TREND_COLORS[result.summary.trend] || TREND_COLORS.STABLE} />
                        <Pill label="Stock risk" value={result.summary.stockRisk} tone={RISK_COLORS[result.summary.stockRisk] || RISK_COLORS.MEDIUM} />

                        <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                          <p className="text-sm text-slate-500">Generated points</p>
                          <p className="text-2xl font-semibold">{result.chart.length}</p>
                          <p className="mt-2 text-sm text-slate-600">The API returns a forecast learned from the cleaned retail_store_100k dataset, so the UI can stay the same while the model improves over time.</p>
                        </div>
                      </CardContent>
                    </Card>
                  </div>
                ) : (
                  <div className="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
                    <Card className="border-slate-200 bg-white text-slate-900">
                      <CardHeader>
                        <CardTitle className="text-lg">Top recommendations</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        {result.recommendations.map((item) => (
                          <div key={item.product} className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                            <div className="flex items-start justify-between gap-4">
                              <div>
                                <p className="font-medium">{item.product}</p>
                                <p className="text-sm text-slate-500">Best combo candidate</p>
                              </div>
                              <div className="text-right text-sm text-slate-600">
                                <p>Confidence: {item.confidence.toFixed(2)}</p>
                                <p>Support: {item.support.toFixed(2)}</p>
                                <p>Lift: {item.lift.toFixed(2)}</p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </CardContent>
                    </Card>

                    <Card className="border-slate-200 bg-white text-slate-900">
                      <CardHeader>
                        <CardTitle className="text-lg">Basket summary</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                          <p className="text-sm text-slate-500">Top combo</p>
                          <p className="mt-1 text-lg font-semibold">{result.summary.topCombo}</p>
                        </div>
                        <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                          <p className="text-sm text-slate-500">Average basket size</p>
                          <p className="mt-1 text-2xl font-semibold">{result.summary.avgBasketSize.toFixed(2)}</p>
                        </div>
                        <p className="text-sm text-slate-600">
                          The backend mines association rules from the cleaned retail_store_100k dataset and ranks the strongest basket combinations for the selected products.
                        </p>
                      </CardContent>
                    </Card>
                  </div>
                )}
              </>
            ) : (
              <Card className="h-full border border-dashed border-slate-300 bg-white/60 shadow-none">
                <CardContent className="flex h-full min-h-[520px] flex-col items-center justify-center text-center text-slate-600">
                  <Brain className="h-12 w-12 text-emerald-500" />
                  <h3 className="mt-4 text-lg font-semibold text-slate-900">No AI analysis yet</h3>
                  <p className="mt-2 max-w-md text-sm text-slate-500">
                    Choose a branch and products from the store list, then run Smart AI for predictions.
                  </p>
                </CardContent>
              </Card>
            )}
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function MetricCard({ icon, label, value, note }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 text-slate-900 shadow-sm">
      <div className="flex items-center gap-2 text-slate-500 text-sm">
        <span className="inline-flex h-7 w-7 items-center justify-center rounded-full bg-emerald-100 text-emerald-600">
          {icon}
        </span>
        {label}
      </div>
      <div className="mt-3 text-lg font-semibold break-words">{value}</div>
      <div className="mt-1 text-xs text-slate-500">{note}</div>
    </div>
  );
}

function Pill({ label, value, tone }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
      <p className="text-sm text-slate-500">{label}</p>
      <span className={`mt-2 inline-flex rounded-full px-3 py-1 text-sm font-semibold ${tone}`}>
        {value}
      </span>
    </div>
  );
}
