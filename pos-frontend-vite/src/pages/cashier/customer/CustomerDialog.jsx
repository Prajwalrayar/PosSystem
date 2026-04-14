import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useDispatch, useSelector } from 'react-redux';
import { getAllCustomers, getCustomerById } from '@/Redux Toolkit/features/customer/customerThunks';
import CustomerForm from './CustomerForm';
import { setSelectedCustomer } from '../../../Redux Toolkit/features/cart/cartSlice';
import { clearCustomerState } from '@/Redux Toolkit/features/customer/customerSlice';
import { useToast } from '../../../components/ui/use-toast';

const CustomerDialog = ({
  showCustomerDialog,
  setShowCustomerDialog
}) => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const { customers, loading } = useSelector(state => state.customer);
  const { userProfile } = useSelector((state) => state.user);
  const branch = useSelector((state) => state.branch.branch);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [fetchRetryCount, setFetchRetryCount] = useState(0);

  const activeScopeKey = `${userProfile?.id || "anon"}:${branch?.storeId || userProfile?.storeId || "none"}:${branch?.id || userProfile?.branchId || "none"}`;

  // Refresh customers whenever store/branch scope changes
  useEffect(() => {
    dispatch(clearCustomerState());
    dispatch(setSelectedCustomer(null));
    setFetchRetryCount(0);
    dispatch(getAllCustomers());
  }, [dispatch, activeScopeKey]);

  useEffect(() => {
    if (!showCustomerDialog) {
      return;
    }
    dispatch(getAllCustomers());
  }, [showCustomerDialog, dispatch]);

  // Filter customers based on search term
  const filteredCustomers = customers.filter(customer =>
    customer.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.phone?.includes(searchTerm)
  );

    const handleCustomerSelect = async (customer) => {
      try {
        const verifiedCustomer = await dispatch(getCustomerById(customer.id)).unwrap();
        dispatch(
          setSelectedCustomer({
            ...verifiedCustomer,
            name: verifiedCustomer?.name || verifiedCustomer?.fullName || "",
          })
        );
        setShowCustomerDialog(false);
        toast({
          title: "Customer Selected",
          description: `${verifiedCustomer?.fullName || verifiedCustomer?.name || "Customer"} selected for this order`,
        });
      } catch (errorPayload) {
        const status = errorPayload?.status;
        if (status === 403 || status === 404) {
          dispatch(setSelectedCustomer(null));
          dispatch(getAllCustomers());
          toast({
            title: "Customer Unavailable",
            description: "Customer not available in this store.",
            variant: "destructive",
          });
          return;
        }
        toast({
          title: "Selection Failed",
          description: errorPayload?.message || "Failed to select customer",
          variant: "destructive",
        });
      }
    };

  useEffect(() => {
    if (!showCustomerDialog) {
      return;
    }
    if (loading) {
      return;
    }
    if (customers.length > 0) {
      return;
    }
    if (fetchRetryCount > 0) {
      return;
    }

    setFetchRetryCount(1);
    dispatch(getAllCustomers());
  }, [showCustomerDialog, loading, customers.length, fetchRetryCount, dispatch]);



  return (
    <Dialog open={showCustomerDialog} onOpenChange={setShowCustomerDialog}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Select Customer</DialogTitle>
        </DialogHeader>
        
        <div className="mb-4">
          <Input 
            placeholder="Search customers..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        
        <div className="max-h-96 overflow-y-auto">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <p>Loading customers...</p>
            </div>
          ) : filteredCustomers.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <p className="text-gray-500">
                {searchTerm ? 'No customers found matching your search.' : 'No customers in this store yet'}
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredCustomers.map(customer => (
                  <TableRow key={customer.id}>
                    <TableCell>{customer.fullName}</TableCell>
                    <TableCell>{customer.phone}</TableCell>
                    <TableCell>{customer.email}</TableCell>
                    <TableCell>
                      <Button size="sm" onClick={() => handleCustomerSelect(customer)}>
                        Select
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </div>

        <CustomerForm 
          showCustomerForm={showCustomerForm}
          setShowCustomerForm={setShowCustomerForm}
       
        />
        
        <DialogFooter>
          <Button variant="outline" onClick={() => setShowCustomerDialog(false)}>Cancel</Button>
          <Button onClick={() => setShowCustomerForm(true)}>Add New Customer</Button>
        </DialogFooter>
        
        
      </DialogContent>
    </Dialog>
  );
};

export default CustomerDialog;