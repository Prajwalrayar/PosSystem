import React from "react";
import { handleDownloadOrderPDF } from "../pdf/pdfUtils";
import { useToast } from "../../../../components/ui/use-toast";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Download } from "lucide-react";
import { PrinterIcon } from "lucide-react";
import { useSelector } from "react-redux";
import OrderDetails from "./OrderDetails";
import { useDispatch } from "react-redux";
import { resetOrder } from "../../../../Redux Toolkit/features/cart/cartSlice";
import { createPrintJob } from "@/Redux Toolkit/features/print/printThunks";
import { getActivePrinterId } from "@/utils/printer";
import { getApiErrorMessage } from "@/utils/apiError";

const InvoiceDialog = ({ showInvoiceDialog, setShowInvoiceDialog }) => {
  let { selectedOrder } = useSelector((state) => state.order);
  const branch = useSelector((state) => state.branch.branch);
  const { creating: printCreating } = useSelector((state) => state.print);
//   selectedOrder={customer:{fullName:""},items:[{}]}
//   showInvoiceDialog=true
  
  const { toast } = useToast();
  const dispatch = useDispatch();
  const handlePrintInvoice = async () => {
    if (!selectedOrder?.id) {
      return;
    }

    try {
      const data = await dispatch(
        createPrintJob({
          type: "ORDER_INVOICE",
          referenceId: selectedOrder.id,
          printerId: getActivePrinterId(branch),
        })
      ).unwrap();

      toast({
        title: "Print Job Queued",
        description:
          data?.message || `Invoice print job queued for order ${selectedOrder.id}`,
      });
    } catch (errorPayload) {
      toast({
        title: "Print Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };
  const handleDownloadPDF = async () => {
    await handleDownloadOrderPDF(selectedOrder, toast);
  };

  const finishOrder = () => {
    setShowInvoiceDialog(false);
    // Reset the order
    dispatch(resetOrder());

    toast({
      title: "Order Completed",
      description: "Receipt printed and order saved successfully",
    });
  };

  return (
    <Dialog open={showInvoiceDialog} onOpenChange={setShowInvoiceDialog}>
      {selectedOrder && (
        <DialogContent className="w-5xl">
          <DialogHeader>
            <DialogTitle>Order Details - Invoice</DialogTitle>
          </DialogHeader>
          <OrderDetails selectedOrder={selectedOrder} />

          <DialogFooter className="gap-2 sm:gap-0 space-x-3">
            <Button variant="outline" onClick={handleDownloadPDF}>
              <Download className="h-4 w-4 mr-2" />
              Download PDF
            </Button>
            <Button
              variant="outline"
              onClick={() => handlePrintInvoice(selectedOrder)}
              disabled={printCreating}
            >
              <PrinterIcon className="h-4 w-4 mr-2" />
              {printCreating ? "Printing..." : "Print Invoice"}
            </Button>

            <Button onClick={finishOrder}>Start New Order</Button>
          </DialogFooter>
        </DialogContent>
      )}
    </Dialog>
  );
};

export default InvoiceDialog;
