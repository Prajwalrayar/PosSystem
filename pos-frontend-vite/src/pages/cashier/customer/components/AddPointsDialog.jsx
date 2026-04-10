import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

const AddPointsDialog = ({ 
  isOpen, 
  onClose, 
  customer, 
  pointsToAdd, 
  onPointsChange, 
  note,
  onNoteChange,
  onAddPoints,
  isLoading,
}) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add Loyalty Points</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <p><span className="font-medium">Customer:</span> {customer?.fullName || customer?.name}</p>
            <p><span className="font-medium">Current Points:</span> {customer?.loyaltyPoints || 0}</p>
          </div>
          
          <div className="space-y-2">
            <label htmlFor="points" className="text-sm font-medium">Points to Add</label>
            <Input
              id="points"
              type="number"
              min="1"
              value={pointsToAdd}
              onChange={(e) => onPointsChange(parseInt(e.target.value) || 0)}
              disabled={isLoading}
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="note" className="text-sm font-medium">Note</label>
            <Input
              id="note"
              type="text"
              value={note}
              onChange={(e) => onNoteChange(e.target.value)}
              placeholder="Festival bonus"
              disabled={isLoading}
            />
          </div>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isLoading}>Cancel</Button>
          <Button onClick={onAddPoints} disabled={isLoading}>
            {isLoading ? 'Saving...' : 'Add Points'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default AddPointsDialog; 