import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useToast } from '@/components/ui/use-toast';
import {
  ShiftInformationCard,
  SalesSummaryCard,
  PaymentSummaryCard,
  TopSellingItemsCard,
  RecentOrdersCard,
  RefundsCard,
  ShiftHeader,
  LogoutConfirmDialog,
  PrintDialog
} from './components';
import { getCurrentShiftProgress, endShift } from '../../../Redux Toolkit/features/shiftReport/shiftReportThunks';
import { logout } from '../../../Redux Toolkit/features/user/userThunks';
import { useNavigate } from 'react-router';
import { createPrintJob } from '@/Redux Toolkit/features/print/printThunks';
import { getActivePrinterId } from '@/utils/printer';
import { getApiErrorMessage } from '@/utils/apiError';

const ShiftSummaryPage = () => {
  const dispatch = useDispatch();
  const { toast } = useToast();
  const [showLogoutConfirmDialog, setShowLogoutConfirmDialog] = useState(false);
  const [showPrintDialog, setShowPrintDialog] = useState(false);
  const navigate=useNavigate()

  const { currentShift, loading, error } = useSelector((state) => state.shiftReport);
  const { creating: printCreating } = useSelector((state) => state.print);
  const branch = useSelector((state) => state.branch.branch);

  useEffect(() => {
    dispatch(getCurrentShiftProgress());
  }, [dispatch]);

  const handlePrintSummary = async () => {
    try {
      const data = await dispatch(
        createPrintJob({
          type: 'SHIFT_SUMMARY',
          referenceId: currentShift?.id,
          printerId: getActivePrinterId(branch),
        })
      ).unwrap();

      setShowPrintDialog(false);
      toast({
        title: 'Print Job Queued',
        description: data?.message || 'Shift summary print job queued.',
      });
    } catch (errorPayload) {
      toast({
        title: 'Print Failed',
        description: getApiErrorMessage(errorPayload),
        variant: 'destructive',
      });
    }
  };

  const handleEndShift = async () => {
    setShowLogoutConfirmDialog(false);
    if (true) {
       dispatch(endShift());
       dispatch(logout())
       navigate("/")
      toast({
        title: 'Shift Ended',
        description: 'You have been logged out successfully',
      });
      
    }
  };

  return (
    <div className="h-full flex flex-col">
      <ShiftHeader 
        onPrintClick={() => setShowPrintDialog(true)}
        onEndShiftClick={() => setShowLogoutConfirmDialog(true)}
      />
      <div className="flex-1 overflow-auto p-4">
        {loading ? (
          <div className="flex justify-center items-center h-full text-lg">Loading shift summary...</div>
        ) : error ? (
          <div className="flex justify-center items-center h-full text-destructive">{error}</div>
        ) : currentShift ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <ShiftInformationCard shiftData={currentShift} />
              <SalesSummaryCard shiftData={currentShift} />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <PaymentSummaryCard shiftData={currentShift} />
              <TopSellingItemsCard shiftData={currentShift} />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <RecentOrdersCard shiftData={currentShift} />
              <RefundsCard shiftData={currentShift} />
            </div>
          </>
        ) : (
          <div className="flex justify-center items-center h-full text-muted-foreground">No shift data available.</div>
        )}
      </div>
      <LogoutConfirmDialog 
        isOpen={showLogoutConfirmDialog}
        onClose={() => setShowLogoutConfirmDialog(false)}
        onConfirm={handleEndShift}
      />
      <PrintDialog 
        isOpen={showPrintDialog}
        onClose={() => setShowPrintDialog(false)}
        onConfirm={handlePrintSummary}
        loading={printCreating}
      />
    </div>
  );
};

export default ShiftSummaryPage;