import { Outlet } from "react-router";
import { useDispatch } from "react-redux";
import { useEffect } from "react";
import SuperAdminSidebar from "./components/SuperAdminSidebar";
import SuperAdminTopbar from "./components/SuperAdminTopbar";
import { getAllStores } from "@/Redux Toolkit/features/store/storeThunks";
import { getAllSubscriptionPlans } from "@/Redux Toolkit/features/subscriptionPlan/subscriptionPlanThunks";

export default function SuperAdminDashboard({ children }) {
  const dispatch = useDispatch();
  
  useEffect(() => {
    if (localStorage.getItem("jwt")) {
      dispatch(getAllStores());
      dispatch(getAllSubscriptionPlans());
    }
  }, [dispatch]);

  return (
    <div className="flex h-screen bg-gradient-to-br from-primary/5 via-background to-primary/10">
      <SuperAdminSidebar />
      <div className="flex-1 flex flex-col">
        <SuperAdminTopbar />
        <main className="flex-1 overflow-y-auto p-8 md:p-10 lg:p-12 bg-background/80 rounded-tl-3xl shadow-xl m-4">
          {children || <Outlet />}
        </main>
      </div>
    </div>
  );
} 