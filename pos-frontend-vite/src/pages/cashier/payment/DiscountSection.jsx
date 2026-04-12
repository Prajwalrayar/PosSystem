import React from 'react'
import { useDispatch } from 'react-redux';
import { useSelector } from 'react-redux';
import { selectDiscount, setDiscount } from '../../../Redux Toolkit/features/cart/cartSlice';
import { Tag, AlertCircle, Loader2 } from 'lucide-react';
import { Button } from '../../../components/ui/button';
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { getBranchCheckoutPolicy } from './branchPolicy';

const DiscountSection = () => {
  const dispatch = useDispatch();
  const discount = useSelector(selectDiscount);
  const branchState = useSelector((state) => state.branch);
  const userRole = (useSelector((state) => state.user?.userProfile?.role) || '').trim().toUpperCase();
  const policy = getBranchCheckoutPolicy(branchState);
  const isLoadingSettings = branchState?.branchSettings?.loading;

  // Compute values always, before any conditional rendering
  const needsManagerApproval = policy ? Boolean(policy?.discount?.requireManagerApproval) : false;
  const isCashier = userRole === 'ROLE_CASHIER';
  const isDiscountAllowed = policy ? (Boolean(policy?.discount?.allowDiscount) && !(needsManagerApproval && isCashier)) : false;
  const maxDiscountPercentage = policy ? Number(policy?.discount?.maxDiscountPercentage || 0) : 0;

  const handleSetDiscount = (e) => {
    const rawValue = parseFloat(e.target.value) || 0;
    const nextValue =
      discount.type === 'percentage' && maxDiscountPercentage > 0
        ? Math.min(rawValue, maxDiscountPercentage)
        : rawValue;

    dispatch(
      setDiscount({ ...discount, value: nextValue })
    );
  };

  // Hooks must always be called in the same order
  React.useEffect(() => {
    if (!isDiscountAllowed && (discount.value !== 0 || discount.type !== 'percentage')) {
      dispatch(setDiscount({ type: 'percentage', value: 0 }));
      return;
    }

    if (
      isDiscountAllowed &&
      discount.type === 'percentage' &&
      maxDiscountPercentage > 0 &&
      discount.value > maxDiscountPercentage
    ) {
      dispatch(setDiscount({ ...discount, value: maxDiscountPercentage }));
    }
  }, [dispatch, discount, isDiscountAllowed, maxDiscountPercentage]);

  // Conditional rendering in JSX, not as early returns
  if (isLoadingSettings) {
    return (
      <div className="p-4 border-b">
        <h2 className="text-lg font-semibold mb-3 flex items-center">
          <Tag className="w-5 h-5 mr-2" />
          Discount
        </h2>
        <div className="flex items-center justify-center py-8">
          <Loader2 className="w-5 h-5 animate-spin text-muted-foreground mr-2" />
          <span className="text-sm text-muted-foreground">Loading settings...</span>
        </div>
      </div>
    );
  }

  if (!policy) {
    return (
      <div className="p-4 border-b">
        <h2 className="text-lg font-semibold mb-3 flex items-center">
          <Tag className="w-5 h-5 mr-2" />
          Discount
        </h2>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Branch settings not available. Discount is disabled.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
     <div className="p-4 border-b">
        <h2 className="text-lg font-semibold mb-3 flex items-center">
          <Tag className="w-5 h-5 mr-2" />
          Discount
        </h2>
        <div className="space-y-3">
          <Input
            type="number"
            placeholder="Discount amount"
            value={discount.value || ""}
            onChange={handleSetDiscount}
            disabled={!isDiscountAllowed}
            max={discount.type === 'percentage' && maxDiscountPercentage > 0 ? maxDiscountPercentage : undefined}
          />
          <div className="flex space-x-2">
            <Button
              variant={discount.type === "percentage" ? "default" : "outline"}
              size="sm"
              className="flex-1"
              onClick={() => dispatch(setDiscount({ ...discount, type: 'percentage' }))}
              disabled={!isDiscountAllowed}
            >
              %
            </Button>
            <Button
              variant={discount.type === "fixed" ? "default" : "outline"}
              size="sm"
              className="flex-1"
              onClick={() => dispatch(setDiscount({ ...discount, type: 'fixed' }))}
              disabled={!isDiscountAllowed}
            >
              ₹
            </Button>
          </div>
          {!isDiscountAllowed && (
            <p className="text-xs text-muted-foreground">
              {needsManagerApproval && isCashier
                ? 'Manager approval is required to apply discounts.'
                : 'Discount is disabled for this branch.'}
            </p>
          )}
          {isDiscountAllowed && discount.type === 'percentage' && maxDiscountPercentage > 0 && (
            <p className="text-xs text-muted-foreground">Maximum allowed discount: {maxDiscountPercentage}%</p>
          )}
        </div>
      </div>
  )
}

export default DiscountSection