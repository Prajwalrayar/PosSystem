import React from 'react'
import { Card, CardContent } from '../../../../components/ui/card'
import { formatDate, getPaymentModeLabel } from '../data'

const OrderInformation = ({selectedOrder}) => {
  const toAmount = (...values) => {
    for (const value of values) {
      const parsed = Number(value);
      if (Number.isFinite(parsed)) {
        return parsed;
      }
    }
    return 0;
  };

  const subtotal = toAmount(selectedOrder?.subtotal, selectedOrder?.subTotal);
  const tax = toAmount(selectedOrder?.taxTotal, selectedOrder?.taxAmount);
  const discount = toAmount(selectedOrder?.discountTotal, selectedOrder?.discountAmount);
  const grandTotal = toAmount(selectedOrder?.grandTotal, selectedOrder?.totalAmount);

  return (
  <Card className="min-w-0">
          <CardContent className="p-3 sm:p-4">
            <h3 className="mb-2 text-sm font-semibold sm:text-base">Order Information</h3>
            <div className="space-y-1 text-xs sm:text-sm">
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Date:</span>
                <span className="text-right break-words">{formatDate(selectedOrder.createdAt)}</span>
              </div>
              
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Payment Method:</span>
                <span className="text-right">{getPaymentModeLabel(selectedOrder.paymentType)}</span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Subtotal:</span>
                <span className="text-right">₹{subtotal.toFixed(2)}</span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Tax:</span>
                <span className="text-right">₹{tax.toFixed(2)}</span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Discount:</span>
                <span className="text-right text-red-600">- ₹{discount.toFixed(2)}</span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Grand Total:</span>
                <span className="font-semibold text-right">
                  ₹{grandTotal.toFixed(2)}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
  )
}

export default OrderInformation