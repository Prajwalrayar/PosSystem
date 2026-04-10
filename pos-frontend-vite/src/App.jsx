import React, { useEffect, useState } from "react";
import { Routes, Route, Navigate } from "react-router";
import { useDispatch, useSelector } from "react-redux";

// Auth and Store Routes
import AuthRoutes from "./routes/AuthRoutes";
import StoreRoutes from "./routes/StoreRoutes";
import BranchManagerRoutes from "./routes/BranchManagerRoutes";
import { getUserProfile } from "./Redux Toolkit/features/user/userThunks";
import Landing from "./pages/common/Landing/Landing";
import CashierRoutes from "./routes/CashierRoutes";
import Onboarding from "./pages/onboarding/Onboarding";
import { getStoreByAdmin, getStoreByEmployee } from "./Redux Toolkit/features/store/storeThunks";
import SuperAdminRoutes from "./routes/SuperAdminRoutes";
import PageNotFound from "./pages/common/PageNotFound";

const App = () => {
  const dispatch = useDispatch();
  const { userProfile, loading: userLoading } = useSelector((state) => state.user);
  const { store } = useSelector((state) => state.store);
  const [authInitialized, setAuthInitialized] = useState(false);
  const [storeInitialized, setStoreInitialized] = useState(false);

  useEffect(() => {
    const jwt = localStorage.getItem("jwt");

    if (!jwt) {
      setAuthInitialized(true);
      return;
    }

    if (userProfile) {
      setAuthInitialized(true);
      return;
    }

    if (userLoading) {
      return;
    }

    dispatch(getUserProfile(jwt)).finally(() => {
      setAuthInitialized(true);
    });
  }, [dispatch, userProfile, userLoading]);

  useEffect(() => {
    if (!userProfile) {
      setStoreInitialized(true);
      return;
    }

    const isStoreRole =
      userProfile.role === "ROLE_STORE_ADMIN" ||
      userProfile.role === "ROLE_STORE_MANAGER";

    if (!isStoreRole) {
      setStoreInitialized(true);
      return;
    }

    if (store) {
      setStoreInitialized(true);
      return;
    }

    setStoreInitialized(false);
    const fetchStore =
      userProfile.role === "ROLE_STORE_ADMIN"
        ? getStoreByAdmin
        : getStoreByEmployee;

    dispatch(fetchStore()).finally(() => {
      setStoreInitialized(true);
    });
  }, [dispatch, userProfile]);

  const isStoreRole =
    userProfile?.role === "ROLE_STORE_ADMIN" ||
    userProfile?.role === "ROLE_STORE_MANAGER";

  if (!authInitialized || (isStoreRole && !storeInitialized)) {
    return (
      <div className="min-h-screen flex items-center justify-center text-muted-foreground">
        Loading...
      </div>
    );
  }

  let content;

  // console.log("state ", user)

  if (userProfile && userProfile.role) {
    // User is logged in
    if (userProfile.role === "ROLE_ADMIN") {
      content = (
        <Routes>
          <Route path="/" element={<Navigate to="/super-admin" replace />} />
          <Route path="/super-admin/*" element={<SuperAdminRoutes />} />
          <Route
            path="*"
            element={<PageNotFound/>}
          />
        </Routes>
      );
    } else if (userProfile.role === "ROLE_BRANCH_CASHIER") {
      content = (
        <Routes>
          <Route path="/" element={<Navigate to="/cashier" replace />} />
          <Route path="/cashier/*" element={<CashierRoutes />} />
          <Route
            path="*"
            element={<PageNotFound/>}
          />
        </Routes>
      );
    } else if (
      userProfile.role === "ROLE_STORE_ADMIN" ||
      userProfile.role === "ROLE_STORE_MANAGER"
    ) {
      // console.log("get inside", store);
      if (!store) {
        content = (
          <Routes>
            <Route path="/auth/onboarding" element={<Onboarding />} />
            <Route
              path="*"
              element={<Navigate to="/auth/onboarding" replace />}
            />
          </Routes>
        );
        return content;
      } else {
        // console.log("get inside 2");
        content = (
          <Routes>
            <Route path="/" element={<Navigate to="/store" replace />} />
            <Route path="/store/*" element={<StoreRoutes />} />
            <Route
              path="*"
              element={<PageNotFound/>}
            />
          </Routes>
        );
      }
    } else if (
      userProfile.role === "ROLE_BRANCH_MANAGER" ||
      userProfile.role === "ROLE_BRANCH_ADMIN"
    ) {
      content = (
        <Routes>
          <Route path="/" element={<Navigate to="/branch" replace />} />
          <Route path="/branch/*" element={<BranchManagerRoutes />} />
          <Route
            path="*"
            element={<PageNotFound/>}
          />
        </Routes>
      );
    } else {
      // Unknown role, redirect to landing or error page
      content = (
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route
            path="*"
            element={ <PageNotFound/>}
          />
        </Routes>
      );
    }
  } else {
    // User is not logged in, show landing page and auth routes
    content = (
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/auth/*" element={<AuthRoutes />} />
        <Route path="/super-admin/*" element={<Navigate to="/auth/login" replace />} />
        <Route path="/store/*" element={<Navigate to="/auth/login" replace />} />
        <Route path="/branch/*" element={<Navigate to="/auth/login" replace />} />
        <Route path="/cashier/*" element={<Navigate to="/auth/login" replace />} />
        <Route
          path="*"
          element={
          <Navigate to="/" replace />
          }
        />
      </Routes>
    );
  }

  return content;
};

export default App;
