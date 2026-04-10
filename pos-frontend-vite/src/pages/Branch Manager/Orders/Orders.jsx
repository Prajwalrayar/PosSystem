import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import { Button } from "@/components/ui/button";

import { RefreshCw, ArrowUpDown } from "lucide-react";
import {
  getOrdersByBranch,
  getOrderById,
} from "@/Redux Toolkit/features/order/orderThunks";
import { findBranchEmployees } from "@/Redux Toolkit/features/employee/employeeThunks";
import { getPaymentIcon } from "../../../utils/getPaymentIcon";

import { getStatusColor } from "../../../utils/getStatusColor";
import OrdersFilters from "./OrdersFilters";
import OrdersTable from "./OrdersTable";
import OrderDetailsDialog from "./OrderDetailsDialog";
import { createPrintJob } from "@/Redux Toolkit/features/print/printThunks";
import { getApiErrorMessage } from "@/utils/apiError";
import { getActivePrinterId } from "@/utils/printer";
import { useToast } from "@/components/ui/use-toast";

const Orders = () => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const branchId = useSelector((state) => state.branch.branch?.id);
  const { orders, loading } = useSelector((state) => state.order);
  const { selectedOrder } = useSelector((state) => state.order);
  const { creating: printCreating } = useSelector((state) => state.print);
  const branch = useSelector((state) => state.branch.branch);


  const [showDetails, setShowDetails] = useState(false);

  // Fetch branch employees (cashiers)
  useEffect(() => {
    if (branchId) {
      dispatch(findBranchEmployees({ branchId, role: "ROLE_BRANCH_CASHIER" }));
    }
  }, [branchId, dispatch]);

  // Fetch orders when filters change
  useEffect(() => {
    if (branchId) {
      const data = {
        branchId,
      };
      console.log("filters data ", data);
      dispatch(getOrdersByBranch(data));
    }
  }, [branchId, dispatch]);

  const handleViewDetails = (orderId) => {
    dispatch(getOrderById(orderId));
    setShowDetails(true);
  };

  const handlePrintInvoice = async (orderId) => {
    try {
      const data = await dispatch(
        createPrintJob({
          type: "ORDER_INVOICE",
          referenceId: orderId,
          printerId: getActivePrinterId(branch),
        })
      ).unwrap();

      toast({
        title: "Print Job Queued",
        description: data?.message || `Invoice print job queued for order ${orderId}`,
      });
    } catch (errorPayload) {
      toast({
        title: "Print Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };

  const handleRefresh = () => {
    if (branchId) {
      const data = {
        branchId,
      };
      console.log("filter data ", data);
      dispatch(getOrdersByBranch(data));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Orders</h1>
        <Button
          variant="outline"
          className="gap-2"
          onClick={handleRefresh}
          disabled={loading}
        >
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Search and Filters */}
      <OrdersFilters />

      {/* Orders Table */}
      <OrdersTable
        orders={orders}
        loading={loading}
        onViewDetails={handleViewDetails}
        onPrintInvoice={handlePrintInvoice}
        printLoading={printCreating}
        getStatusColor={getStatusColor}
        getPaymentIcon={getPaymentIcon}
      />

      {/* Order Details Dialog */}
      <OrderDetailsDialog
        open={showDetails && !!selectedOrder}
        onOpenChange={setShowDetails}
        selectedOrder={selectedOrder}
        getStatusColor={getStatusColor}
        getPaymentIcon={getPaymentIcon}
      />
    </div>
  );
};

export default Orders;
