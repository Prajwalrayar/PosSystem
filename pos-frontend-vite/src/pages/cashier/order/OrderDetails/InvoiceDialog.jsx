import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { useToast } from "../../../../components/ui/use-toast";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Download, Mail, ExternalLink } from "lucide-react";

import OrderDetails from "./OrderDetails";
import { resetOrder } from "../../../../Redux Toolkit/features/cart/cartSlice";
import { handleDownloadOrderPDF } from "../pdf/pdfUtils";
import { clearInvoiceState } from "../../../../Redux Toolkit/features/invoice/invoiceSlice";
import { sendInvoiceEmail, fetchInvoiceStatus } from "../../../../Redux Toolkit/features/invoice/invoiceThunks";
import {
  getInvoiceDeliveryStatusFromOrder,
  getInvoiceIdFromOrder,
  getInvoicePdfUrlFromOrder,
} from "../../../../utils/invoice";
import { getApiErrorMessage } from "@/utils/apiError";

const InvoiceDialog = ({ showInvoiceDialog, setShowInvoiceDialog }) => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const orderState = useSelector((state) => state.order);
  const cartOrder = useSelector((state) => state.cart.currentOrder);
  const invoiceState = useSelector((state) => state.invoice);

  const fallbackOrder = cartOrder || orderState.selectedOrder;
  const selectedOrder = React.useMemo(() => {
    const invoicePayload = invoiceState.currentInvoice;
    const invoiceOrder = invoicePayload?.order;
    const baseOrder = invoiceOrder || fallbackOrder;

    if (!baseOrder) {
      return null;
    }

    const subtotal = Number(
      invoicePayload?.subtotal ?? invoicePayload?.invoice?.subtotal ?? baseOrder?.subtotal ?? baseOrder?.subTotal ?? 0
    );
    const taxTotal = Number(
      invoicePayload?.taxTotal ??
        invoicePayload?.invoice?.taxTotal ??
        invoicePayload?.invoice?.taxAmount ??
        baseOrder?.taxTotal ??
        baseOrder?.taxAmount ??
        0
    );
    const discountTotal = Number(
      invoicePayload?.discountTotal ??
        invoicePayload?.invoice?.discountTotal ??
        invoicePayload?.invoice?.discountAmount ??
        baseOrder?.discountTotal ??
        baseOrder?.discountAmount ??
        0
    );
    const grandTotal = Number(
      invoicePayload?.grandTotal ??
        invoicePayload?.invoice?.grandTotal ??
        baseOrder?.grandTotal ??
        baseOrder?.totalAmount ??
        0
    );

    return {
      ...baseOrder,
      subtotal,
      taxTotal,
      discountTotal,
      grandTotal,
      taxAmount: Number(baseOrder?.taxAmount ?? taxTotal),
      discountAmount: Number(baseOrder?.discountAmount ?? discountTotal),
      totalAmount: grandTotal,
    };
  }, [invoiceState.currentInvoice, fallbackOrder]);

  const invoiceId = getInvoiceIdFromOrder(invoiceState.currentInvoice) || getInvoiceIdFromOrder(selectedOrder);
  const invoicePdfUrl = getInvoicePdfUrlFromOrder(invoiceState.currentInvoice) || getInvoicePdfUrlFromOrder(selectedOrder);
  const invoiceDeliveryStatus =
    getInvoiceDeliveryStatusFromOrder(invoiceState.currentInvoice) ||
    getInvoiceDeliveryStatusFromOrder(selectedOrder);

  const handleSendInvoiceEmail = async () => {
    if (!invoiceId) {
      toast({
        title: "Invoice Not Ready",
        description: "Invoice id is missing, so the email cannot be sent yet.",
        variant: "destructive",
      });
      return;
    }

    const customerEmail = selectedOrder?.customer?.email;
    if (!customerEmail) {
      toast({
        title: "Customer Email Required",
        description: "The selected order does not have a customer email.",
        variant: "destructive",
      });
      return;
    }

    try {
      const response = await dispatch(
        sendInvoiceEmail({
          invoiceId,
          payload: {
            customerEmail,
            orderId: selectedOrder?.id,
            resend: true,
          },
        })
      ).unwrap();

      const statusId = response?.invoice?.id || response?.invoiceId || invoiceId;
      if (statusId) {
        dispatch(fetchInvoiceStatus(statusId));
      }

      toast({
        title: "Invoice Email Sent",
        description: response?.message || `Invoice email queued for order ${selectedOrder?.id}`,
      });
    } catch (errorPayload) {
      toast({
        title: "Send Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };

  const handleDownloadPDF = async () => {
    await handleDownloadOrderPDF(selectedOrder, toast);
  };

  const handlePreviewPDF = () => {
    if (!invoicePdfUrl) {
      toast({
        title: "Preview Unavailable",
        description: "Backend PDF url is not available yet.",
        variant: "destructive",
      });
      return;
    }

    window.open(invoicePdfUrl, "_blank", "noopener,noreferrer");
  };

  const finishOrder = () => {
    setShowInvoiceDialog(false);
    dispatch(resetOrder());
    dispatch(clearInvoiceState());
    toast({
      title: "Order Completed",
      description: "Ready for the next order.",
    });
  };

  return (
    <Dialog open={showInvoiceDialog} onOpenChange={setShowInvoiceDialog}>
      {selectedOrder && (
        <DialogContent className="w-[92vw] max-w-3xl overflow-x-hidden p-4 sm:p-5">
          <DialogHeader>
            <DialogTitle className="text-base sm:text-lg">Order Details - Invoice</DialogTitle>
          </DialogHeader>

          {invoiceDeliveryStatus ? (
            <div className="rounded-md border px-3 py-2 text-xs text-muted-foreground sm:text-sm">
              Delivery status: <span className="font-medium text-foreground">{invoiceDeliveryStatus}</span>
            </div>
          ) : null}

          <OrderDetails selectedOrder={selectedOrder} />

          <DialogFooter className="grid w-full grid-cols-2 gap-2 pt-2">
            <Button className="w-full" variant="outline" onClick={handleDownloadPDF}>
              <Download className="h-4 w-4 mr-2" />
              Download PDF
            </Button>
            <Button className="w-full" variant="outline" onClick={handlePreviewPDF} disabled={!invoicePdfUrl}>
              <ExternalLink className="h-4 w-4 mr-2" />
              Preview PDF
            </Button>
            <Button className="w-full" variant="outline" onClick={handleSendInvoiceEmail} disabled={!invoiceId || invoiceState.emailSending}>
              <Mail className="h-4 w-4 mr-2" />
              {invoiceState.emailSending ? "Sending..." : "Send Invoice Email"}
            </Button>
            <Button className="w-full" onClick={finishOrder}>Start New Order</Button>
          </DialogFooter>
        </DialogContent>
      )}
    </Dialog>
  );
};

export default InvoiceDialog;
