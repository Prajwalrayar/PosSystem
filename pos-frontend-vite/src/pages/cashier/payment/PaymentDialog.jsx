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
  selectCartPricingBreakdown,
  selectNote,
  selectPaymentMethod,
  selectSelectedCustomer,
  setCurrentOrder,
  setPaymentMethod,
  setSelectedCustomer,
} from "../../../Redux Toolkit/features/cart/cartSlice";
import { useToast } from "../../../components/ui/use-toast";
import { useDispatch } from "react-redux";
import { createOrder } from "../../../Redux Toolkit/features/order/orderThunks";
import { completeOrderPayment } from "../../../Redux Toolkit/features/invoice/invoiceThunks";
import { getAllCustomers } from "@/Redux Toolkit/features/customer/customerThunks";
import { fetchPaymentGatewayStatus } from "@/Redux Toolkit/features/payment/paymentThunks";
import { paymentMethods } from "./data";
import { getAllowedPaymentMethods, getBranchCheckoutPolicy } from "./branchPolicy";

const PaymentDialog = ({
  showPaymentDialog,
  setShowPaymentDialog,
  setShowReceiptDialog,
}) => {
  const [isProcessingPayment, setIsProcessingPayment] = React.useState(false);
  const [paymentError, setPaymentError] = React.useState(null);
  const paymentCancelledRef = React.useRef(false);
  const razorpayInstanceRef = React.useRef(null);
  const paymentMethod = useSelector(selectPaymentMethod);
  const {toast} = useToast();
  const cart = useSelector(selectCartItems);
  const branchState = useSelector((state) => state.branch);
  const branch = branchState?.branch;
  const { userProfile } = useSelector((state) => state.user);
  const customers = useSelector((state) => state.customer.customers || []);
  const { gatewayStatus, gatewayStatusLoading } = useSelector((state) => state.payment);
  const dispatch = useDispatch();

  const selectedCustomer = useSelector(selectSelectedCustomer);
  const hasSelectedCustomer = Boolean(
    selectedCustomer?.id ||
    selectedCustomer?.name ||
    selectedCustomer?.fullName ||
    selectedCustomer?.phone ||
    selectedCustomer?.email
  );
  const customerEmail = selectedCustomer?.email?.trim() || null;
  const pricing = useSelector(selectCartPricingBreakdown);

  const note = useSelector(selectNote);

  const checkoutPolicy = getBranchCheckoutPolicy(branchState);
  const isLoadingSettings = branchState?.branchSettings?.loading;
  const allowedPaymentMethods = React.useMemo(
    () => (checkoutPolicy ? getAllowedPaymentMethods(checkoutPolicy) : []),
    [checkoutPolicy]
  );
  const enabledPaymentMethods = paymentMethods.filter((method) =>
    allowedPaymentMethods.includes(method.key)
  );
  const isGatewayMethodSelected = paymentMethod === "UPI" || paymentMethod === "CARD";

  React.useEffect(() => {
    dispatch(fetchPaymentGatewayStatus());
  }, [dispatch]);

  React.useEffect(() => {
    if (checkoutPolicy && !allowedPaymentMethods.includes(paymentMethod)) {
      dispatch(setPaymentMethod(allowedPaymentMethods[0] || "cash"));
    }
  }, [allowedPaymentMethods, dispatch, paymentMethod, checkoutPolicy]);

  React.useEffect(() => {
    if (!selectedCustomer?.id) {
      return;
    }

    const inCurrentStoreList = customers.some(
      (customer) => customer.id === selectedCustomer.id
    );

    if (!inCurrentStoreList) {
      dispatch(setSelectedCustomer(null));
      dispatch(getAllCustomers());
      toast({
        title: "Customer Cleared",
        description: "Previously selected customer is not available in this store.",
      });
    }
  }, [customers, selectedCustomer, dispatch, toast]);

  const loadRazorpayScript = React.useCallback(() => {
    if (window.Razorpay) {
      return Promise.resolve(true);
    }

    return new Promise((resolve) => {
      const existingScript = document.getElementById("razorpay-checkout-script");
      if (existingScript) {
        existingScript.addEventListener("load", () => resolve(true));
        existingScript.addEventListener("error", () => resolve(false));
        return;
      }

      const script = document.createElement("script");
      script.id = "razorpay-checkout-script";
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  }, []);

  const openRazorpayCheckout = React.useCallback(
    ({ amount, createdOrder }) => {
      return new Promise((resolve, reject) => {
        const razorpayKey =
          gatewayStatus?.keyId ||
          gatewayStatus?.publicKey ||
          gatewayStatus?.apiKey ||
          import.meta.env.VITE_RAZORPAY_KEY_ID;

        if (!razorpayKey) {
          reject(new Error("Razorpay key is missing. Set it in backend gateway status or VITE_RAZORPAY_KEY_ID."));
          return;
        }

        const amountInPaise = Math.max(100, Math.round(Number(amount || 0) * 100));
        const razorpayOrderId =
          createdOrder?.razorpayOrderId ||
          createdOrder?.gatewayOrderId ||
          createdOrder?.paymentOrderId ||
          createdOrder?.orderId ||
          null;

        const options = {
          key: razorpayKey,
          amount: amountInPaise,
          currency: createdOrder?.currency || "INR",
          name: branch?.name || "POS Checkout",
          description: `Order #${createdOrder?.id || createdOrder?.orderNumber || ""}`,
          order_id: razorpayOrderId || undefined,
          prefill: {
            name:
              selectedCustomer?.fullName ||
              selectedCustomer?.name ||
              userProfile?.fullName ||
              "Walk-in Customer",
            email: customerEmail || undefined,
            contact: selectedCustomer?.phone || userProfile?.mobile || undefined,
          },
          notes: {
            branchId: String(branch?.id || ""),
            localOrderId: String(createdOrder?.id || ""),
            cashierId: String(userProfile?.id || ""),
          },
          theme: {
            color: "#16a34a",
          },
          handler: (response) => {
            razorpayInstanceRef.current = null;
            resolve(response);
          },
          modal: {
            ondismiss: () => {
              razorpayInstanceRef.current = null;
              reject(new Error("Payment cancelled by user."));
            },
            escape: false,
          },
        };

        try {
          let resolved = false;
          const razorpay = new window.Razorpay(options);
          razorpayInstanceRef.current = razorpay;
          
          razorpay.on("payment.failed", (response) => {
            if (resolved) return;
            resolved = true;
            razorpayInstanceRef.current = null;
            const errorMsg = response?.error?.description || response?.error?.reason || "Razorpay payment failed.";
            reject(new Error(errorMsg));
          });

          razorpay.on("payment.error", (response) => {
            if (resolved) return;
            resolved = true;
            razorpayInstanceRef.current = null;
            reject(new Error(response?.error?.description || "Razorpay error occurred."));
          });

          razorpay.open();
        } catch (error) {
          razorpayInstanceRef.current = null;
          reject(error);
        }
      });
    },
    [branch?.id, branch?.name, customerEmail, gatewayStatus, selectedCustomer, userProfile]
  );

  const throwIfPaymentCancelled = React.useCallback(() => {
    if (paymentCancelledRef.current) {
      throw new Error("Payment cancelled.");
    }
  }, []);

  const handleCancelPaymentDialog = React.useCallback(() => {
    paymentCancelledRef.current = true;
    if (razorpayInstanceRef.current?.close) {
      razorpayInstanceRef.current.close();
      razorpayInstanceRef.current = null;
    }
    setIsProcessingPayment(false);
    setShowPaymentDialog(false);
  }, [setShowPaymentDialog]);


  const processPayment = async () => {
    paymentCancelledRef.current = false;
    setPaymentError(null);

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

    if (selectedCustomer?.id) {
      const inCurrentStoreList = customers.some(
        (customer) => customer.id === selectedCustomer.id
      );

      if (!inCurrentStoreList) {
        dispatch(setSelectedCustomer(null));
        dispatch(getAllCustomers());
        toast({
          title: "Customer Unavailable",
          description: "Selected customer is not available in this store.",
          variant: "destructive",
        });
        return;
      }
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
      setIsProcessingPayment(true);
      const normalizedPricing = {
        subtotal: Number(pricing.subtotal || 0),
        taxRate: Number(pricing.taxRate || 0),
        taxAmount: Number(pricing.taxAmount || 0),
        discountType: pricing.discountType || "percentage",
        discountValue: Number(pricing.discountValue || 0),
        discountAmount: Number(pricing.discountAmount || 0),
        totalAmount: Number(pricing.total || 0),
      };

      // Prepare order data according to OrderDTO structure
      const orderData = {
        ...normalizedPricing,
        branchId: branch.id,
        cashierId: userProfile.id,
        customer: selectedCustomer || null,
        items: cart.map((item) => ({
          productId: item.id,
          quantity: item.quantity,
          price: Number(item.sellingPrice ?? item.price ?? 0),
          total: Number(item.sellingPrice ?? item.price ?? 0) * item.quantity,
        })),
        paymentType: paymentMethod,
        note: note || "",
      };

      console.log("Creating order:", orderData);

      // Create order
      const createdOrder = await dispatch(createOrder(orderData)).unwrap();
      throwIfPaymentCancelled();
      let paymentReference = null;

      if (isGatewayMethodSelected) {
        const scriptLoaded = await loadRazorpayScript();
        throwIfPaymentCancelled();
        if (!scriptLoaded) {
          throw new Error("Failed to load Razorpay checkout. Check your internet connection and try again.");
        }

        try {
          const gatewayResponse = await openRazorpayCheckout({
            amount: normalizedPricing.totalAmount,
            createdOrder,
          });
          throwIfPaymentCancelled();

          paymentReference =
            gatewayResponse?.razorpay_payment_id ||
            gatewayResponse?.paymentId ||
            gatewayResponse?.payment_id ||
            null;
        } catch (razorpayError) {
          if (paymentCancelledRef.current) {
            throw razorpayError;
          }
          throw new Error(razorpayError?.message || "Razorpay payment could not be processed. Please try again.");
        }
      }

      const invoicePayload = {
        customerId: selectedCustomer?.id ?? null,
        customerName: selectedCustomer?.fullName || selectedCustomer?.name || "",
        customerEmail,
        customerPhone: selectedCustomer?.phone || null,
        paymentMethod,
        cashierId: userProfile?.id ?? null,
        cashierName: userProfile?.fullName || "",
        branchId: branch.id,
        ...normalizedPricing,
        paymentReference,
        sendEmail: true,
        note: note || "",
      };

      const invoiceResponse = await dispatch(
        completeOrderPayment({
          orderId: createdOrder.id,
          payload: invoicePayload,
        })
      ).unwrap();
      throwIfPaymentCancelled();

      const finalOrder = invoiceResponse?.order || invoiceResponse?.data?.order || createdOrder;
      const invoiceRecord = invoiceResponse?.invoice || invoiceResponse?.data?.invoice || invoiceResponse;
      const backendSubtotal = Number(
        invoiceRecord?.subtotal ?? invoiceResponse?.subtotal ?? finalOrder?.subtotal ?? orderData.subtotal
      );
      const backendTaxAmount = Number(
        invoiceRecord?.taxTotal ??
          invoiceRecord?.taxAmount ??
          invoiceResponse?.taxTotal ??
          finalOrder?.taxAmount ??
          orderData.taxAmount
      );
      const backendDiscountAmount = Number(
        invoiceRecord?.discountTotal ??
          invoiceRecord?.discountAmount ??
          invoiceResponse?.discountTotal ??
          finalOrder?.discountAmount ??
          orderData.discountAmount
      );
      const backendGrandTotal = Number(
        invoiceRecord?.grandTotal ??
          invoiceResponse?.grandTotal ??
          finalOrder?.totalAmount ??
          orderData.totalAmount
      );

      dispatch(
        setCurrentOrder({
          ...finalOrder,
          subtotal: backendSubtotal,
          taxRate: Number(finalOrder?.taxRate ?? orderData.taxRate),
          taxAmount: backendTaxAmount,
          discountType: finalOrder?.discountType ?? orderData.discountType,
          discountValue: Number(finalOrder?.discountValue ?? orderData.discountValue),
          discountAmount: backendDiscountAmount,
          totalAmount: backendGrandTotal,
          invoiceId: invoiceRecord?.id || invoiceRecord?.invoiceId || finalOrder?.invoiceId,
          invoiceNumber: invoiceRecord?.invoiceNumber || finalOrder?.invoiceNumber,
          invoicePdfUrl: invoiceRecord?.invoicePdfUrl || invoiceRecord?.pdfUrl || finalOrder?.invoicePdfUrl,
          invoiceDeliveryStatus:
            invoiceRecord?.deliveryStatus || invoiceRecord?.status || finalOrder?.invoiceDeliveryStatus,
        })
      );

      setShowPaymentDialog(false);
      setShowReceiptDialog(true);

      toast({
        title: "Order Created Successfully",
        description: "Payment successful. You can send the invoice email from Order Details.",
      });
    } catch (error) {
      if (paymentCancelledRef.current || error?.message?.includes("Payment cancelled")) {
        toast({
          title: "Payment Cancelled",
          description: "The payment flow was cancelled.",
        });
        setPaymentError(null);
        return;
      }
      console.error("Failed to create order:", error);
      const errorMsg = 
        error?.message || 
        error?.data?.message || 
        (typeof error === 'string' ? error : null) ||
        "Failed to complete payment. Please try again.";
      setPaymentError(errorMsg);
      toast({
        title: "Payment Error",
        description: errorMsg,
        variant: "destructive",
      });
    } finally {
      setIsProcessingPayment(false);
    }
  };

  const handlePaymentMethod = (method) => {
    dispatch(setPaymentMethod(method));
    setPaymentError(null);
  };

  return (
    <Dialog open={showPaymentDialog} onOpenChange={setShowPaymentDialog}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Payment</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">
              ₹{pricing.total.toFixed(2)}
            </div>
            <p className="text-sm text-gray-600">Amount to be paid</p>
          </div>

          {isLoadingSettings ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-5 h-5 animate-spin text-muted-foreground mr-2" />
              <span className="text-sm text-muted-foreground">Loading payment methods...</span>
            </div>
          ) : paymentError ? (
            <Alert variant="destructive" className="mt-4">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                <div className="space-y-3">
                  <p className="font-medium text-sm">Payment Error</p>
                  <p className="text-sm leading-relaxed break-words">{paymentError}</p>
                  <div className="pt-2 border-t border-red-200">
                    <p className="text-xs text-red-700">💡 Try again by clicking "Complete Payment" or select a different payment method.</p>
                  </div>
                </div>
              </AlertDescription>
            </Alert>
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
              {gatewayStatusLoading && (
                <p className="text-xs text-muted-foreground">Checking gateway status...</p>
              )}
              {enabledPaymentMethods.length === 0 && (
                <p className="text-sm text-red-600">
                  No payment methods are enabled for this branch.
                </p>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleCancelPaymentDialog}>
            Cancel
          </Button>
          <Button 
            onClick={processPayment} 
            disabled={
              isProcessingPayment ||
              isLoadingSettings ||
              !checkoutPolicy ||
              enabledPaymentMethods.length === 0
            }
          >
            {isProcessingPayment ? "Processing..." : "Complete Payment"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PaymentDialog;
