import React, { useState, useEffect } from "react";

import {
  OrderDetailsSection,
  ReturnItemsSection,

  ReturnReceiptDialog,
} from "./components";
import { useDispatch, useSelector } from "react-redux";
import { getOrdersByBranch } from "../../../Redux Toolkit/features/order/orderThunks";
import OrderTable from "./components/OrderTable";
import { useLocation } from "react-router";

// Return reasons

const ReturnOrderPage = () => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showReceiptDialog, setShowReceiptDialog] = useState(false);

  const dispatch = useDispatch();
  const location = useLocation();
  const { branch } = useSelector((state) => state.branch);
  const { returnSessionId } = useSelector((state) => state.returns);

  // Fetch orders for the branch on mount or when branch changes
  useEffect(() => {
    if (branch?.id) {
      dispatch(getOrdersByBranch({ branchId: branch.id }));
    }
  }, [dispatch, branch]);

  useEffect(() => {
    const preselectedOrder = location.state?.selectedOrder;
    if (preselectedOrder) {
      setSelectedOrder(preselectedOrder);
    }
  }, [location.state]);



  const handleSelectOrder = (order) => {
    console.log("selected order", order);
    setSelectedOrder(order);
  };

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 bg-card border-b">
        <h1 className="text-2xl font-bold">Return / Refund</h1>
        {returnSessionId && (
          <p className="text-sm text-muted-foreground mt-1">
            Active return session: {returnSessionId}
          </p>
        )}
      </div>

      <div className="flex-1 flex overflow-hidden">
        {/* Left Column - Order Search & Selection */}
        {!selectedOrder ? (
          <OrderTable handleSelectOrder={handleSelectOrder} />
        ) : (
          <>
            <OrderDetailsSection
              selectedOrder={selectedOrder}
              setSelectedOrder={setSelectedOrder}
            />
            <ReturnItemsSection
              setShowReceiptDialog={setShowReceiptDialog}
              selectedOrder={selectedOrder}
            />
          </>
        )}
      </div>

      {selectedOrder && (
        <ReturnReceiptDialog
          showReceiptDialog={showReceiptDialog}
          setShowReceiptDialog={setShowReceiptDialog}
          selectedOrder={selectedOrder}
        />
      )}
    </div>
  );
};

export default ReturnOrderPage;
