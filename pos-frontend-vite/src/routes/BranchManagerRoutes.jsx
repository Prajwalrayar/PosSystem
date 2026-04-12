import React from "react";
import { Routes, Route, Navigate } from "react-router";
import { useSelector } from "react-redux";

// Import Branch Manager Dashboard Layout
import BranchManagerDashboard from "../pages/Branch Manager/Dashboard/BranchManagerDashboard";

// Import Branch Manager pages
import {
  Dashboard,
  Orders,
  Transactions,
  Inventory,
  // Employees,
  Customers,
  Reports,
  Settings
} from "../pages/Branch Manager";
import { BranchEmployees } from "../pages/Branch Manager/Employees";
import Refunds from "../pages/Branch Manager/Refunds/Refunds";

const BranchManagerRoutes = () => {
  const { userProfile } = useSelector((state) => state.user);
  const role = (userProfile?.role || "").trim().toUpperCase();
  const hasBranchManagerAccess = role === "ROLE_BRANCH_MANAGER" || role === "ROLE_BRANCH_ADMIN";

  if (!hasBranchManagerAccess) {
    return <Navigate to="/cashier" replace />;
  }

  return (
    <Routes>
      <Route path="/" element={<BranchManagerDashboard />}>
        <Route index element={<Dashboard />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="orders" element={<Orders />} />
        <Route path="refunds" element={<Refunds />} />
        <Route path="transactions" element={<Transactions />} />
        <Route path="inventory" element={<Inventory />} />
        <Route path="employees" element={<BranchEmployees />} />
        <Route path="customers" element={<Customers />} />
        <Route path="reports" element={<Reports />} />
        <Route path="settings" element={<Settings />} />
      </Route>
    </Routes>
  );
};

export default BranchManagerRoutes;