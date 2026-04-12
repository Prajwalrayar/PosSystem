import React from 'react'
import { Card, CardContent } from '../../../../components/ui/card'
import { formatDate, getPaymentModeLabel } from '../data'

const OrderInformation = ({selectedOrder}) => {
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
                <span className="text-muted-foreground">Total Amount:</span>
                <span className="font-semibold text-right">
                  ₹{selectedOrder.totalAmount?.toFixed(2) || "0.00"}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
  )
}

export default OrderInformation