import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { PrinterIcon } from 'lucide-react';
import { Loader2 } from 'lucide-react';

const PrintDialog = ({ isOpen, onClose, onConfirm, loading }) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Print Shift Summary</DialogTitle>
        </DialogHeader>
        
        <div className="py-4">
          <p>Do you want to print your shift summary report?</p>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={loading}>Cancel</Button>
          <Button onClick={onConfirm} disabled={loading}>
            {loading ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <PrinterIcon className="h-4 w-4 mr-2" />
            )}
            {loading ? 'Printing...' : 'Print Summary'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PrintDialog; 