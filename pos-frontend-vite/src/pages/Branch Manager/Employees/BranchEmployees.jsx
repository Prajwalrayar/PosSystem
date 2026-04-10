import React, { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search, Plus } from "lucide-react";
import { branchAdminRole } from "../../../utils/userRole";

import EmployeeStats from "./EmployeeStats";
import EmployeeTable from "./EmployeeTable";
import {
  AddEmployeeDialog,
  EditEmployeeDialog,
  ResetPasswordDialog,
  PerformanceDialog,
} from "./EmployeeDialogs";
import { useDispatch } from "react-redux";
import { useSelector } from "react-redux";
import {
  createBranchEmployee,
  findBranchEmployees,
  resetEmployeePassword,
  updateEmployee,
} from "../../../Redux Toolkit/features/employee/employeeThunks";
import { clearResetPasswordState } from "../../../Redux Toolkit/features/employee/employeeSlice";
import { useToast } from "@/components/ui/use-toast";
import { getApiErrorMessage } from "@/utils/apiError";

const getStatusColor = (status) => {
  if (status === "Active") {
    return "text-green-500";
  } else if (status === "Inactive") {
    return "text-red-500";
  } else {
    return "text-gray-500";
  }
};

const BranchEmployees = () => {
  // const [employees, setEmployees] = useState([]); // Initialize with empty array
  const [searchTerm, setSearchTerm] = useState("");
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isResetPasswordDialogOpen, setIsResetPasswordDialogOpen] =
    useState(false);
  const [isPerformanceDialogOpen, setIsPerformanceDialogOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const dispatch = useDispatch();
  const { toast } = useToast();
  // const { store } = useSelector((state) => state);
  const { branch } = useSelector((state) => state.branch);
  const { employees, resetPasswordStatus } = useSelector((state) => state.employee);
  const { userProfile } = useSelector((state) => state.user);
  const [resetPasswordForm, setResetPasswordForm] = useState({
    temporaryPassword: "",
    forceChangeOnNextLogin: true,
  });


  console.log("branch employees", employees);

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleAddEmployee = (newEmployeeData) => {
    if (branch?.id && userProfile.branchId) {
      const data = {
        employee: {
          ...newEmployeeData,

          username: newEmployeeData.email.split("@")[0],
        },
        branchId: branch.id,
        token: localStorage.getItem("jwt"),
      };
      console.log("branch employee data ", data);
      dispatch(createBranchEmployee(data));
      setIsAddDialogOpen(false);
    }
  };

  const handleEditEmployee = (updatedEmployeeData) => {
    if (selectedEmployee?.id && localStorage.getItem("jwt")) {
      const data={
          employeeId: selectedEmployee.id,
          employeeDetails: updatedEmployeeData,
          token: localStorage.getItem("jwt"),

        }
      dispatch(
        updateEmployee(data)
      );
      setIsEditDialogOpen(false);
    }
  };

    useEffect(() => {
      if (branch?.id) {
        dispatch(
          findBranchEmployees({
            branchId: branch?.id
          })
        );
      }
    }, [dispatch, branch?.id]);

  // const handleEditEmployee = (updatedEmployeeData) => {
  //   const updatedEmployees = employees.map((employee) =>
  //     employee.id === updatedEmployeeData.id
  //       ? {
  //           ...updatedEmployeeData,
  //           status: updatedEmployeeData.loginAccess ? "Active" : "Inactive",
  //         }
  //       : employee
  //   );
  //   setEmployees(updatedEmployees);
  //   setIsEditDialogOpen(false);
  // };

  const handleToggleAccess = (employee) => {
    const updatedEmployees = employees.map((emp) =>
      emp.id === employee.id
        ? {
            ...emp,
            loginAccess: !emp.loginAccess,
            status: !emp.loginAccess ? "Inactive" : "Active",
          }
        : emp
    );
    // setEmployees(updatedEmployees);
  };

  const handleResetPassword = async () => {
    if (!selectedEmployee?.id) {
      return;
    }

    if (!resetPasswordForm.temporaryPassword?.trim()) {
      toast({
        title: "Validation Error",
        description: "Temporary password is required.",
        variant: "destructive",
      });
      return;
    }

    dispatch(clearResetPasswordState());

    try {
      const result = await dispatch(
        resetEmployeePassword({
          employeeId: selectedEmployee.id,
          temporaryPassword: resetPasswordForm.temporaryPassword,
          forceChangeOnNextLogin: resetPasswordForm.forceChangeOnNextLogin,
        })
      ).unwrap();

      toast({
        title: "Password Reset",
        description:
          result?.message ||
          `Password reset completed for ${selectedEmployee.fullName || selectedEmployee.name}.`,
      });

      setResetPasswordForm({
        temporaryPassword: "",
        forceChangeOnNextLogin: true,
      });
      setIsResetPasswordDialogOpen(false);
    } catch (errorPayload) {
      toast({
        title: "Reset Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };

  const openEditDialog = (employee) => {
    setSelectedEmployee(employee);
    setIsEditDialogOpen(true);
  };

  const openResetPasswordDialog = (employee) => {
    setSelectedEmployee(employee);
    setResetPasswordForm({
      temporaryPassword: "",
      forceChangeOnNextLogin: true,
    });
    setIsResetPasswordDialogOpen(true);
  };

  const openPerformanceDialog = (employee) => {
    setSelectedEmployee(employee);
    setIsPerformanceDialogOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">
          Employee Management
        </h1>
        <AddEmployeeDialog
          isAddDialogOpen={isAddDialogOpen}
          setIsAddDialogOpen={setIsAddDialogOpen}
          handleAddEmployee={handleAddEmployee}
          roles={branchAdminRole}
        />
      </div>
      <EmployeeStats employees={employees} />
      <EmployeeTable
        employees={employees}
        getStatusColor={getStatusColor}
        handleToggleAccess={handleToggleAccess}
        openEditDialog={openEditDialog}
        openResetPasswordDialog={openResetPasswordDialog}
        openPerformanceDialog={openPerformanceDialog}
      />

      <EditEmployeeDialog
        isEditDialogOpen={isEditDialogOpen}
        setIsEditDialogOpen={setIsEditDialogOpen}
        selectedEmployee={selectedEmployee}
        handleEditEmployee={handleEditEmployee}
        roles={branchAdminRole}
      />

      <ResetPasswordDialog
        isResetPasswordDialogOpen={isResetPasswordDialogOpen}
        setIsResetPasswordDialogOpen={setIsResetPasswordDialogOpen}
        selectedEmployee={selectedEmployee}
        handleResetPassword={handleResetPassword}
        resetPasswordForm={resetPasswordForm}
        onResetPasswordFormChange={setResetPasswordForm}
        isResetting={resetPasswordStatus.isLoading}
      />

      <PerformanceDialog
        isPerformanceDialogOpen={isPerformanceDialogOpen}
        setIsPerformanceDialogOpen={setIsPerformanceDialogOpen}
        selectedEmployee={selectedEmployee}
      />
    </div>
  );
};

export default BranchEmployees;
