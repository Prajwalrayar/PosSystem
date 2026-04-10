import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getRecentSales } from "@/Redux Toolkit/features/storeAnalytics/storeAnalyticsThunks";
import { useToast } from "@/components/ui/use-toast";

const RecentSales = () => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const { recentSales, loading } = useSelector((state) => state.storeAnalytics);
  const { userProfile } = useSelector((state) => state.user);

  useEffect(() => {
    if (userProfile?.id) {
      fetchRecentSales();
    }
  }, [userProfile]);

  const fetchRecentSales = async () => {
    try {
      await dispatch(getRecentSales(userProfile.id)).unwrap();
    } catch (err) {
      toast({
        title: "Error",
        description: err || "Failed to fetch recent sales",
        variant: "destructive",
      });
    }
  };

  // Format currency
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount || 0);
  };

  // Format date
  const formatDate = (dateString, periodLabel) => {
    // If periodLabel is provided (like "Today", "Yesterday"), use it
    if (periodLabel) return periodLabel;
    
    // Otherwise parse the ISO date
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) return "Today";
    if (date.toDateString() === yesterday.toDateString()) return "Yesterday";

    return date.toLocaleDateString("en-IN", { month: "short", day: "numeric" });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl font-semibold">Recent Sales</CardTitle>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="text-center text-gray-500 py-4">Loading...</div>
        ) : recentSales && recentSales.length > 0 ? (
          <div className="space-y-4">
            {recentSales.map((sale, index) => (
              <div
                key={index}
                className="flex items-center justify-between border-b pb-3 last:border-0 last:pb-0"
              >
                <div>
                  <p className="font-medium">{sale.branchName}</p>
                  <p className="text-sm text-gray-500">
                    {formatDate(sale.lastSaleAt, sale.periodLabel)}
                  </p>
                </div>
                <p className="font-semibold">{formatCurrency(sale.totalSales)}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center text-gray-500 py-4">No sales data available</div>
        )}
      </CardContent>
    </Card>
  );
};

export default RecentSales;