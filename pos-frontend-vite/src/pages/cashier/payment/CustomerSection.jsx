import React from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { selectSelectedCustomer, setSelectedCustomer } from '../../../Redux Toolkit/features/cart/cartSlice';
import { User } from 'lucide-react';
import { Card, CardContent } from '../../../components/ui/card';
import { Button } from '../../../components/ui/button';

const CustomerSection = ({setShowCustomerDialog}) => {
    const dispatch = useDispatch();
    const selectedCustomer = useSelector(selectSelectedCustomer);

    const handleRemoveCustomer = () => {
      dispatch(setSelectedCustomer(null));
    };
  return (
         <div className="p-4 border-b">
        <h2 className="text-lg font-semibold mb-3 flex items-center">
          <User className="w-5 h-5 mr-2" />
          Customer
        </h2>
        {selectedCustomer ? (
          <Card className="border-green-200 bg-green-50/80 shadow-sm dark:bg-green-950 dark:border-green-800">
            <CardContent className="p-3 space-y-3">
              <div className="flex items-start gap-3">
                <div className="h-9 w-9 rounded-full bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-200 flex items-center justify-center">
                  <User className="w-4 h-4" />
                </div>
                <div className="min-w-0">
                  <h3 className="font-semibold text-green-800 dark:text-green-200 truncate">
                    {selectedCustomer?.name || selectedCustomer?.fullName}
                  </h3>
                  <p className="text-sm text-green-700/80 dark:text-green-300 truncate">
                    {selectedCustomer?.phone || selectedCustomer?.email || "Customer selected"}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full"
                  onClick={() => setShowCustomerDialog(true)}
                >
                  Change
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/40"
                  onClick={handleRemoveCustomer}
                >
                  Remove
                </Button>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Button
            variant="outline"
            className="w-full"
            onClick={() => setShowCustomerDialog(true)}
          >
            <User className="w-4 h-4 mr-2" />
            Select Customer
          </Button>
        )}
      </div>
  )
}

export default CustomerSection