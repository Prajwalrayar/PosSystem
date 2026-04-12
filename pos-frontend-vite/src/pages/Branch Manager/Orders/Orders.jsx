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
import { getApiErrorMessage } from "@/utils/apiError";
import { sendInvoiceEmail } from "@/Redux Toolkit/features/invoice/invoiceThunks";
import { getInvoiceIdFromOrder } from "@/utils/invoice";
import { useToast } from "@/components/ui/use-toast";

const Orders = () => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const branchId = useSelector((state) => state.branch.branch?.id);
  const { orders, loading } = useSelector((state) => state.order);
  const { selectedOrder } = useSelector((state) => state.order);
  const { emailSending } = useSelector((state) => state.invoice);


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

  const handleSendInvoiceEmail = async (orderId) => {
    const order = orders.find((item) => item.id === orderId);
    const invoiceId = getInvoiceIdFromOrder(order || selectedOrder);

    if (!invoiceId) {
      toast({
        title: "Invoice Not Ready",
        description: "This order does not have an invoice id yet.",
        variant: "destructive",
      });
      return;
    }

    try {
      const data = await dispatch(
        sendInvoiceEmail({
          invoiceId,
          payload: {
            orderId,
            customerEmail: order?.customer?.email,
            resend: true,
          },
        })
      ).unwrap();

      toast({
        title: "Invoice Email Queued",
        description: data?.message || `Invoice email queued for order ${orderId}`,
      });
    } catch (errorPayload) {
      toast({
        title: "Email Failed",
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
        onSendInvoiceEmail={handleSendInvoiceEmail}
        emailSending={emailSending}
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
