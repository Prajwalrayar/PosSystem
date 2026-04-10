import React, { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Badge } from "../../components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../../components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../../components/ui/dialog";
import { Label } from "../../components/ui/label";
import { DollarSign, Edit, TrendingUp, TrendingDown } from "lucide-react";
import { useToast } from "../../components/ui/use-toast";
import { useDispatch, useSelector } from "react-redux";
import { fetchCommissions, updateCommissionRate } from "@/Redux Toolkit/features/commissions/commissionsThunks";
import { getApiErrorMessage } from "@/utils/apiError";

export default function CommissionsPage() {
  const dispatch = useDispatch();
  const { items: commissions, loading, updateLoading, updatingStoreId } = useSelector((state) => state.commissions);
  const [selectedCommission, setSelectedCommission] = useState(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [newRate, setNewRate] = useState("");
  const { toast } = useToast();

  useEffect(() => {
    dispatch(fetchCommissions());
  }, [dispatch]);

  const handleEditCommission = (commission) => {
    setSelectedCommission(commission);
    setNewRate(commission.currentRate.toString());
    setEditDialogOpen(true);
  };

  const confirmEditCommission = async () => {
    if (selectedCommission && newRate) {
      const rate = parseFloat(newRate);
      if (isNaN(rate) || rate < 0 || rate > 10) {
        toast({
          title: "Invalid Rate",
          description: "Commission rate must be between 0% and 10%",
          variant: "destructive",
        });
        return;
      }

      try {
        const updated = await dispatch(
          updateCommissionRate({
            storeId: selectedCommission.storeId,
            rate,
          })
        ).unwrap();

        toast({
          title: "Commission Updated",
          description: updated?.message || `Commission rate for ${selectedCommission.storeName} has been updated.`,
        });

        setEditDialogOpen(false);
        setSelectedCommission(null);
        setNewRate("");
      } catch (errorPayload) {
        toast({
          title: "Update Failed",
          description: getApiErrorMessage(errorPayload),
          variant: "destructive",
        });
      }
    }
  };

  const totalCurrentRate = useMemo(() => {
    if (!commissions.length) {
      return 0;
    }
    return commissions.reduce((acc, current) => acc + (Number(current.currentRate) || 0), 0);
  }, [commissions]);

  const averageRate = useMemo(() => {
    if (!commissions.length) {
      return 0;
    }
    return totalCurrentRate / commissions.length;
  }, [commissions, totalCurrentRate]);

  const positiveTrendCount = useMemo(() => {
    return commissions.filter((commission) => (commission.currentRate || 0) > (commission.previousRate || 0)).length;
  }, [commissions]);

  const getRateChange = (current, previous) => {
    const change = current - previous;
    if (change > 0) {
      return { value: `+${change}%`, className: "text-green-600", icon: <TrendingUp className="w-3 h-3" /> };
    } else if (change < 0) {
      return { value: `${change}%`, className: "text-red-600", icon: <TrendingDown className="w-3 h-3" /> };
    } else {
      return { value: "0%", className: "text-gray-600", icon: null };
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Commissions</h2>
        <p className="text-muted-foreground">
          Manage commission rates for all stores
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Earnings</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCurrentRate.toFixed(2)}%</div>
            <p className="text-xs text-muted-foreground">
              Sum of configured commission rates
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Rate</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{averageRate.toFixed(2)}%</div>
            <p className="text-xs text-muted-foreground">
              Average across all stores
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Stores</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{positiveTrendCount}</div>
            <p className="text-xs text-muted-foreground">
              Stores with rate increases
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Commission Table */}
      <Card>
        <CardHeader>
          <CardTitle>Store Commission Rates</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="border rounded-lg">
            {loading ? (
              <div className="text-center py-10 text-muted-foreground">Loading commissions...</div>
            ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Store Name</TableHead>
                  <TableHead>Current Rate</TableHead>
                  <TableHead>Rate Change</TableHead>
                  <TableHead>Last Updated</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {commissions.map((commission) => {
                  const rateChange = getRateChange(commission.currentRate, commission.previousRate);
                  return (
                    <TableRow key={commission.storeId}>
                      <TableCell className="font-medium">{commission.storeName}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="text-sm">
                          {commission.currentRate}%
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className={`flex items-center gap-1 ${rateChange.className}`}>
                          {rateChange.icon}
                          <span className="text-sm">{rateChange.value}</span>
                        </div>
                      </TableCell>
                      <TableCell>{commission.updatedAt ? new Date(commission.updatedAt).toLocaleString() : '-'}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEditCommission(commission)}
                          disabled={updateLoading && updatingStoreId === commission.storeId}
                        >
                          <Edit className="w-4 h-4 mr-1" />
                          {updateLoading && updatingStoreId === commission.storeId ? 'Updating...' : 'Edit Rate'}
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Edit Commission Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Commission Rate</DialogTitle>
            <DialogDescription>
              Update the commission rate for {selectedCommission?.storeName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="current-rate">Current Rate</Label>
              <div className="text-sm text-muted-foreground">
                {selectedCommission?.currentRate}%
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="new-rate">New Rate (%)</Label>
              <Input
                id="new-rate"
                type="number"
                step="0.1"
                min="0"
                max="10"
                value={newRate}
                onChange={(e) => setNewRate(e.target.value)}
                placeholder="Enter new commission rate"
              />
              <p className="text-xs text-muted-foreground">
                Rate must be between 0% and 10%
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={confirmEditCommission}
              disabled={updateLoading && updatingStoreId === selectedCommission?.storeId}
            >
              <Edit className="w-4 h-4 mr-2" />
              {updateLoading && updatingStoreId === selectedCommission?.storeId ? 'Updating...' : 'Update Rate'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
} 