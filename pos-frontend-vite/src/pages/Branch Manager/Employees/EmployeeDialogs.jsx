import React from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmployeeForm } from "../../store/Employee";
import { Plus } from "lucide-react";
import { branchAdminRole } from "../../../utils/userRole";
import { useDispatch, useSelector } from "react-redux";
import { useEffect, useMemo, useState } from "react";
import { fetchEmployeePerformance } from "@/Redux Toolkit/features/employee/employeeThunks";

export const AddEmployeeDialog = ({
  isAddDialogOpen,
  setIsAddDialogOpen,
  handleAddEmployee,
  roles,
  formContext,
  currentBranchId,
}) => (
  <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
    <DialogTrigger asChild>
      <Button className="bg-emerald-600 hover:bg-emerald-700">
        <Plus className="mr-2 h-4 w-4" /> Add Employee
      </Button>
    </DialogTrigger>
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Add New Employee</DialogTitle>
      </DialogHeader>
      <EmployeeForm
        initialData={null}
        onSubmit={handleAddEmployee}
        roles={roles}
        formContext={formContext}
        currentBranchId={currentBranchId}
      />
    </DialogContent>
  </Dialog>
);

export const EditEmployeeDialog = ({
  isEditDialogOpen,
  setIsEditDialogOpen,
  selectedEmployee,
  handleEditEmployee,
  roles,
  formContext,
  currentBranchId,
}) =>
  selectedEmployee && (
    <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Employee</DialogTitle>
        </DialogHeader>
        <EmployeeForm
          initialData={
            selectedEmployee
              ? {
                  ...selectedEmployee,
                  branchId: selectedEmployee.branchId || "",
                }
              : null
          }
          onSubmit={handleEditEmployee}
          roles={roles}
          formContext={formContext}
          currentBranchId={currentBranchId}
        />
      </DialogContent>
    </Dialog>
  );

export const ResetPasswordDialog = ({
  isResetPasswordDialogOpen,
  setIsResetPasswordDialogOpen,
  selectedEmployee,
  handleResetPassword,
  resetPasswordForm,
  onResetPasswordFormChange,
  isResetting,
}) =>
  selectedEmployee && (
    <Dialog
      open={isResetPasswordDialogOpen}
      onOpenChange={setIsResetPasswordDialogOpen}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Reset Password</DialogTitle>
        </DialogHeader>
        <div className="py-4">
          <p>
            Are you sure you want to reset the password for{" "}
            <strong>{selectedEmployee.fullName || selectedEmployee.name}</strong>?
          </p>
          <p className="text-sm text-gray-500 mt-2">
            A temporary password will be generated and sent to their email
            address.
          </p>
          <div className="space-y-4 mt-4">
            <div className="space-y-2">
              <label htmlFor="temporaryPassword" className="text-sm font-medium">
                Temporary Password
              </label>
              <input
                id="temporaryPassword"
                type="password"
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={resetPasswordForm.temporaryPassword}
                onChange={(event) =>
                  onResetPasswordFormChange((prev) => ({
                    ...prev,
                    temporaryPassword: event.target.value,
                  }))
                }
                disabled={isResetting}
              />
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={resetPasswordForm.forceChangeOnNextLogin}
                onChange={(event) =>
                  onResetPasswordFormChange((prev) => ({
                    ...prev,
                    forceChangeOnNextLogin: event.target.checked,
                  }))
                }
                disabled={isResetting}
              />
              Force employee to change password on next login
            </label>
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => setIsResetPasswordDialogOpen(false)}
            disabled={isResetting}
          >
            Cancel
          </Button>
          <Button onClick={handleResetPassword} disabled={isResetting}>
            {isResetting ? "Resetting..." : "Reset Password"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );

export const PerformanceDialog = ({
  isPerformanceDialogOpen,
  setIsPerformanceDialogOpen,
  selectedEmployee,
}) => {
  const dispatch = useDispatch();
  const { performance } = useSelector((state) => state.employee);
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");

  const defaultRange = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);
    return {
      from: from.toISOString().slice(0, 10),
      to: to.toISOString().slice(0, 10),
    };
  }, []);

  useEffect(() => {
    if (!isPerformanceDialogOpen || !selectedEmployee?.id) {
      return;
    }

    const from = fromDate || defaultRange.from;
    const to = toDate || defaultRange.to;
    dispatch(fetchEmployeePerformance({ employeeId: selectedEmployee.id, from, to }));
  }, [dispatch, isPerformanceDialogOpen, selectedEmployee?.id, fromDate, toDate, defaultRange.from, defaultRange.to]);

  return (
    selectedEmployee && (
      <Dialog
        open={isPerformanceDialogOpen}
        onOpenChange={setIsPerformanceDialogOpen}
      >
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>
              Performance Summary - {selectedEmployee.fullName || selectedEmployee.name}
            </DialogTitle>
          </DialogHeader>

          <div className="flex items-end gap-3">
            <div className="space-y-1">
              <label className="text-sm font-medium">From</label>
              <input
                type="date"
                className="rounded-md border border-input px-3 py-2 text-sm"
                value={fromDate}
                onChange={(event) => setFromDate(event.target.value)}
              />
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium">To</label>
              <input
                type="date"
                className="rounded-md border border-input px-3 py-2 text-sm"
                value={toDate}
                onChange={(event) => setToDate(event.target.value)}
              />
            </div>
          </div>

          <div className="py-4">
            {performance.loading ? (
              <div className="h-[220px] flex items-center justify-center text-muted-foreground">
                Loading performance...
              </div>
            ) : performance.error ? (
              <div className="h-[220px] flex items-center justify-center text-red-600">
                {performance.error?.message || 'Failed to fetch employee performance'}
              </div>
            ) : (
              <div className="space-y-6">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                  <Card>
                    <CardContent className="p-6">
                      <div className="flex flex-col items-center justify-center">
                        <h3 className="text-lg font-medium text-gray-500">Orders Processed</h3>
                        <p className="text-3xl font-bold mt-2">{performance.data?.ordersProcessed || 0}</p>
                        <p className="text-sm text-gray-500 mt-1">Selected range</p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="p-6">
                      <div className="flex flex-col items-center justify-center">
                        <h3 className="text-lg font-medium text-gray-500">Total Sales</h3>
                        <p className="text-3xl font-bold mt-2">₹{(performance.data?.totalSales || 0).toLocaleString('en-IN')}</p>
                        <p className="text-sm text-gray-500 mt-1">Selected range</p>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="p-6">
                      <div className="flex flex-col items-center justify-center">
                        <h3 className="text-lg font-medium text-gray-500">Avg. Order Value</h3>
                        <p className="text-3xl font-bold mt-2">₹{(performance.data?.averageOrderValue || 0).toLocaleString('en-IN')}</p>
                        <p className="text-sm text-gray-500 mt-1">Selected range</p>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg font-semibold">Daily Sales</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 max-h-40 overflow-auto">
                      {(performance.data?.dailySales || []).map((entry) => (
                        <div key={entry.date} className="flex justify-between rounded-md bg-gray-50 p-2 text-sm">
                          <span>{entry.date}</span>
                          <span>₹{(entry.amount || 0).toLocaleString('en-IN')}</span>
                        </div>
                      ))}
                      {(performance.data?.dailySales || []).length === 0 && (
                        <p className="text-sm text-muted-foreground">No daily sales data.</p>
                      )}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg font-semibold">Activity Log</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 max-h-48 overflow-auto">
                      {(performance.data?.activityLog || []).map((activity, index) => (
                        <div key={`${activity.at}-${index}`} className="flex justify-between items-center border-b pb-2">
                          <p className="font-medium text-sm">{activity.event}</p>
                          <p className="text-xs text-gray-500">{new Date(activity.at).toLocaleString()}</p>
                        </div>
                      ))}
                      {(performance.data?.activityLog || []).length === 0 && (
                        <p className="text-sm text-muted-foreground">No activity logs available.</p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button onClick={() => setIsPerformanceDialogOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    )
  );
};
