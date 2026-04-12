import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { EmployeeForm, EmployeeTable } from ".";
import { useDispatch, useSelector } from "react-redux";
import { useEffect } from "react";
import {
  createStoreEmployee,
  findStoreEmployees,
  updateEmployee,
  deleteEmployee,
} from "@/Redux Toolkit/features/employee/employeeThunks";
import { storeAdminRole } from "../../../utils/userRole";

const STORE_CREATE_ALLOWED_ROLES = ["ROLE_BRANCH_MANAGER", "ROLE_BRANCH_CASHIER"];

const buildEmployeePayload = (data, storeId) => {
  const payload = {
    fullName: data.fullName,
    email: data.email,
    password: data.password,
    phone: data.phone,
    role: data.role,
    storeId,
  };

  if (data.branchId) {
    payload.branchId = Number(data.branchId);
  }

  return payload;
};

export default function StoreEmployees() {
  const dispatch = useDispatch();
  const { employees } = useSelector((state) => state.employee);
  const {store}=useSelector(state=>state.store)

  useEffect(() => {
    if (store?.id) {
      dispatch(
        findStoreEmployees({
          storeId: store?.id,
          token: localStorage.getItem("jwt"),
        })
      );
    }
  }, [dispatch, store?.id, localStorage.getItem("jwt")]);

  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [currentEmployee, setCurrentEmployee] = useState(null);

  const handleAddEmployee = (newEmployeeData) => {
    if (store?.id && localStorage.getItem("jwt")) {
      const selectedRole = STORE_CREATE_ALLOWED_ROLES.includes(newEmployeeData.role)
        ? newEmployeeData.role
        : STORE_CREATE_ALLOWED_ROLES[0];
      const employeePayload = buildEmployeePayload({ ...newEmployeeData, role: selectedRole }, store?.id);

      dispatch(
        createStoreEmployee({
          employee: {
            ...employeePayload,
            username: newEmployeeData.email.split("@")[0],
          },
          storeId: store?.id,
          token: localStorage.getItem("jwt"),
        })
      );
      setIsAddDialogOpen(false);
    }
  };

  const handleEditEmployee = (updatedEmployeeData) => {
    if (currentEmployee?.id && localStorage.getItem("jwt")) {
      const employeePayload = buildEmployeePayload(updatedEmployeeData, store?.id);

      dispatch(
        updateEmployee({
          employeeId: currentEmployee.id,
          employeeDetails: employeePayload,
          token: localStorage.getItem("jwt"),
        })
      );
      setIsEditDialogOpen(false);
    }
  };

  const handleDeleteEmployee = (id) => {
    if (localStorage.getItem("jwt")) {
      dispatch(deleteEmployee({ employeeId: id, token: localStorage.getItem("jwt") }));
    }
  };

  const openEditDialog = (employee) => {
    setCurrentEmployee(employee);
    setIsEditDialogOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">
          Employee Management
        </h1>
        <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
          <DialogTrigger asChild>
            <Button className="bg-emerald-600 hover:bg-emerald-700">
              <Plus className="mr-2 h-4 w-4" /> Add Employee
            </Button>
          </DialogTrigger>
          <DialogContent className="max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Add New Employee</DialogTitle>
            </DialogHeader>
            <EmployeeForm
              onSubmit={handleAddEmployee}
              formContext="store-create"
              initialData={{
                fullName: "",
                email: "",
                password: "",
                phone: "",
                role: STORE_CREATE_ALLOWED_ROLES[0],
                branchId: "",
              }}
              roles={STORE_CREATE_ALLOWED_ROLES}
            />
          </DialogContent>
        </Dialog>

        <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
          <DialogContent className="max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Edit Employee</DialogTitle>
            </DialogHeader>
            <EmployeeForm
              onSubmit={handleEditEmployee}
              roles={storeAdminRole}
              initialData={
                currentEmployee
                  ? {
                      ...currentEmployee,
                      branchId: currentEmployee.branchId || "",
                    }
                  : null
              }
            />
          </DialogContent>
        </Dialog>
      </div>

      <EmployeeTable
        employees={employees}
        onEdit={openEditDialog}
        onDelete={handleDeleteEmployee}
      />
    </div>
  );
}
