import React from "react";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Loader2, AlertCircle } from "lucide-react";
import { useSelector } from "react-redux";
import {
  selectCartItems,
  selectNote,
  selectPaymentMethod,
  selectSelectedCustomer,
  selectTotal,
  setCurrentOrder,
  setPaymentMethod,
} from "../../../Redux Toolkit/features/cart/cartSlice";
import { useToast } from "../../../components/ui/use-toast";
import { useDispatch } from "react-redux";
import { createOrder } from "../../../Redux Toolkit/features/order/orderThunks";
import { paymentMethods } from "./data";
import { getAllowedPaymentMethods, getBranchCheckoutPolicy } from "./branchPolicy";

const PaymentDialog = ({
  showPaymentDialog,
  setShowPaymentDialog,
  setShowReceiptDialog,
}) => {
  const paymentMethod = useSelector(selectPaymentMethod);
  const {toast} = useToast();
  const cart = useSelector(selectCartItems);
  const branchState = useSelector((state) => state.branch);
  const branch = branchState?.branch;
  const { userProfile } = useSelector((state) => state.user);
  const dispatch = useDispatch();

  const selectedCustomer = useSelector(selectSelectedCustomer);
  const hasSelectedCustomer = Boolean(
    selectedCustomer?.id ||
    selectedCustomer?.name ||
    selectedCustomer?.fullName ||
    selectedCustomer?.phone ||
    selectedCustomer?.email
  );

  const total = useSelector(selectTotal);

  const note = useSelector(selectNote);

  const checkoutPolicy = getBranchCheckoutPolicy(branchState);
  const isLoadingSettings = branchState?.branchSettings?.loading;
  const allowedPaymentMethods = checkoutPolicy ? getAllowedPaymentMethods(checkoutPolicy) : [];
  const enabledPaymentMethods = paymentMethods.filter((method) =>
    allowedPaymentMethods.includes(method.key)
  );

  React.useEffect(() => {
    if (checkoutPolicy && !allowedPaymentMethods.includes(paymentMethod)) {
      dispatch(setPaymentMethod(allowedPaymentMethods[0] || "cash"));
    }
  }, [allowedPaymentMethods, dispatch, paymentMethod, checkoutPolicy]);

  

  const processPayment = async () => {
    if (isLoadingSettings) {
      toast({
        title: "Loading Settings",
        description: "Please wait for branch settings to load.",
        variant: "destructive",
      });
      return;
    }

    if (!checkoutPolicy) {
      toast({
        title: "Settings Not Available",
        description: "Branch settings could not be loaded. Please try again.",
        variant: "destructive",
      });
      return;
    }

    if (cart.length === 0) {
      toast({
        title: "Empty Cart",
        description: "Please add items to cart before processing payment",
        variant: "destructive",
      });
      return;
    }

    if (!hasSelectedCustomer) {
      toast({
        title: "Customer Required",
        description: "Please select a customer before processing payment",
        variant: "destructive",
      });
      return;
    }

    if (!allowedPaymentMethods.includes(paymentMethod)) {
      toast({
        title: "Payment Method Not Allowed",
        description: "The selected payment method is disabled for this branch.",
        variant: "destructive",
      });
      return;
    }

    if (!branch?.id) {
      toast({
        title: "Branch Not Loaded",
        description: "Unable to process payment without branch details.",
        variant: "destructive",
      });
      return;
    }

    if (enabledPaymentMethods.length === 0) {
      toast({
        title: "No Payment Methods Enabled",
        description: "Please enable at least one payment method in branch settings.",
        variant: "destructive",
      });
      return;
    }

    try {
      // Prepare order data according to OrderDTO structure
      const orderData = {
        totalAmount: total,
        branchId: branch.id,
        cashierId: userProfile.id,
        customer: selectedCustomer || null,
        items: cart.map((item) => ({
          productId: item.id,
          quantity: item.quantity,
          price: item.price,
          total: item.price * item.quantity,
        })),
        paymentType: paymentMethod,
        note: note || "",
      };

      console.log("Creating order:", orderData);

      // Create order
      const createdOrder = await dispatch(createOrder(orderData)).unwrap();
      dispatch(setCurrentOrder(createdOrder));

      setShowPaymentDialog(false);
      setShowReceiptDialog(true);

      toast({
        title: "Order Created Successfully",
        description: `Order #${createdOrder.id} created and payment processed`,
      });
    } catch (error) {
      console.error("Failed to create order:", error);
      toast({
        title: "Order Creation Failed",
        description: error || "Failed to create order. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handlePaymentMethod = (method) => dispatch(setPaymentMethod(method));

  return (
    <Dialog open={showPaymentDialog} onOpenChange={setShowPaymentDialog}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Payment</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">
              ₹{total.toFixed(2)}
            </div>
            <p className="text-sm text-gray-600">Amount to be paid</p>
          </div>

          {isLoadingSettings ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-5 h-5 animate-spin text-muted-foreground mr-2" />
              <span className="text-sm text-muted-foreground">Loading payment methods...</span>
            </div>
          ) : !checkoutPolicy ? (
            <Alert variant="destructive" className="mt-4">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                Branch settings not available. Cannot process payment.
              </AlertDescription>
            </Alert>
          ) : (
            <div className="space-y-2">
              {enabledPaymentMethods.map((method) => (
                <Button
                  key={method.key}
                  variant={paymentMethod === method.key ? "default" : "outline"}
                  className="w-full justify-start"
                  onClick={() => handlePaymentMethod(method.key)}
                >
                  {method.label}
                </Button>
              ))}
              {enabledPaymentMethods.length === 0 && (
                <p className="text-sm text-red-600">
                  No payment methods are enabled for this branch.
                </p>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => setShowPaymentDialog(false)}>
            Cancel
          </Button>
          <Button 
            onClick={processPayment} 
            disabled={isLoadingSettings || !checkoutPolicy || enabledPaymentMethods.length === 0}
          >
            Complete Payment
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PaymentDialog;
