import { useState, useEffect, useRef } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useToast } from "@/components/ui/use-toast";

// Import components
import POSHeader from "./components/POSHeader";
import ProductSection from "./product/ProductSection";
import CartSection from "./cart/CartSection";
import CustomerPaymentSection from "./payment/CustomerPaymentSection";

import PaymentDialog from "./payment/PaymentDialog";
import ReceiptDialog from "./components/ReceiptDialog";
import HeldOrdersDialog from "./components/HeldOrdersDialog";
import CustomerDialog from "./customer/CustomerDialog";
import InvoiceDialog from "./order/OrderDetails/InvoiceDialog";
import { setTaxRate } from "../../Redux Toolkit/features/cart/cartSlice";
import { getBranchCheckoutPolicy } from "./payment/branchPolicy";
import { fetchBranchSettings } from "../../Redux Toolkit/features/branch/branchThunks";

const CreateOrderPage = () => {
  const { toast } = useToast();
  const searchInputRef = useRef(null);
  const dispatch = useDispatch();

  const { error: orderError } = useSelector((state) => state.order);
  const branchState = useSelector((state) => state.branch);
  const { userProfile } = useSelector((state) => state.user);

  const [showCustomerDialog, setShowCustomerDialog] = useState(false);
  const [showPaymentDialog, setShowPaymentDialog] = useState(false);
  const [showReceiptDialog, setShowReceiptDialog] = useState(false);
  const [showHeldOrdersDialog, setShowHeldOrdersDialog] = useState(false);

  useEffect(() => {
    if (orderError) {
      toast({
        title: "Order Error",
        description: orderError,
        variant: "destructive",
      });
    }
  }, [orderError, toast]);

  // Focus on search input when component mounts
  useEffect(() => {
    if (searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, []);

  // Fetch branch settings on mount (needs branchId from userProfile)
  useEffect(() => {
    const branchId = userProfile?.branchId;
    if (branchId) {
      dispatch(fetchBranchSettings({ branchId }));
    }
  }, [dispatch, userProfile?.branchId]);

  // Apply tax rate from branch settings whenever branchState changes
  useEffect(() => {
    const policy = getBranchCheckoutPolicy(branchState);
    
    if (policy?.tax) {
      const gstRate = policy.tax.gstEnabled ? Number(policy.tax.gstPercentage || 0) : 0;
      dispatch(setTaxRate(gstRate));
    }
  }, [branchState, dispatch]);

  return (
    <div className="h-full flex flex-col bg-background">
      {/* Header */}
      <POSHeader />

      {/* Main Content - 3 Column Layout */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Column - Product Search & List */}
        <ProductSection searchInputRef={searchInputRef} />

        {/* Middle Column - Cart */}
        <CartSection setShowHeldOrdersDialog={setShowHeldOrdersDialog} />

        {/* Right Column - Customer & Payment */}
        <CustomerPaymentSection
          setShowCustomerDialog={setShowCustomerDialog}
          setShowPaymentDialog={setShowPaymentDialog}
        />
      </div>

      <CustomerDialog
        showCustomerDialog={showCustomerDialog}
        setShowCustomerDialog={setShowCustomerDialog}
      />

      <PaymentDialog
        showPaymentDialog={showPaymentDialog}
        setShowPaymentDialog={setShowPaymentDialog}
        setShowReceiptDialog={setShowReceiptDialog}
      />

      {/* <ReceiptDialog
        showReceiptDialog={showReceiptDialog}
        setShowReceiptDialog={setShowReceiptDialog}
      /> */}
      <InvoiceDialog
        showInvoiceDialog={showReceiptDialog}
        setShowInvoiceDialog={setShowReceiptDialog}
        
      />

      <HeldOrdersDialog
        showHeldOrdersDialog={showHeldOrdersDialog}
        setShowHeldOrdersDialog={setShowHeldOrdersDialog}
      />
    </div>
  );
};

export default CreateOrderPage;
