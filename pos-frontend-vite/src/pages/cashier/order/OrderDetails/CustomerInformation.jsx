import React from 'react'
import { Card, CardContent } from '../../../../components/ui/card'

const CustomerInformation = ({selectedOrder}) => {
  return (
       <Card className="min-w-0">
          <CardContent className="p-3 sm:p-4">
            <h3 className="mb-2 text-sm font-semibold sm:text-base">Customer Information</h3>
            <div className="space-y-1 text-xs sm:text-sm">
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Name:</span>
                <span className="text-right break-words">
                  {selectedOrder.customer?.fullName || "Walk-in Customer"}
                </span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Phone:</span>
                <span className="text-right break-words">{selectedOrder.customer?.phone || "N/A"}</span>
              </div>
              <div className="flex items-start justify-between gap-3">
                <span className="text-muted-foreground">Email:</span>
                <span className="text-right break-all">{selectedOrder.customer?.email || "N/A"}</span>
              </div>
            </div>
          </CardContent>
        </Card>
  )
}

export default CustomerInformation