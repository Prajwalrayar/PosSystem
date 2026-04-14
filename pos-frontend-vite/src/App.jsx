import React, { useEffect, useRef, useState } from "react";
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
import { clearCustomerState } from "./Redux Toolkit/features/customer/customerSlice";
import { resetCheckoutContext, setSelectedCustomer } from "./Redux Toolkit/features/cart/cartSlice";
import { getAllCustomers } from "./Redux Toolkit/features/customer/customerThunks";
import { clearInvoiceState } from "./Redux Toolkit/features/invoice/invoiceSlice";
import { clearOrderPreviewState } from "./Redux Toolkit/features/order/orderSlice";

const App = () => {
  const dispatch = useDispatch();
  const { userProfile, loading: userLoading } = useSelector((state) => state.user);
  const { store } = useSelector((state) => state.store);
  const { branch } = useSelector((state) => state.branch);
  const hasStoredToken = Boolean(
    sessionStorage.getItem("jwt") || localStorage.getItem("jwt")
  );
  const [authInitialized, setAuthInitialized] = useState(false);
  const [storeInitialized, setStoreInitialized] = useState(false);
  const [customerScopeKey, setCustomerScopeKey] = useState(null);
  const profileFetchAttemptRef = useRef({ token: null, attempted: false });

  useEffect(() => {
    const sessionJwt = sessionStorage.getItem("jwt");
    const localJwt = localStorage.getItem("jwt");
    const jwt = sessionJwt || localJwt;

    // If no token exists anywhere, clear both storages.
    if (!jwt) {
      sessionStorage.removeItem("jwt");
      sessionStorage.removeItem("token");
      localStorage.removeItem("jwt");
      localStorage.removeItem("token");
      setAuthInitialized(true);
      return;
    }

    // Keep both storages in sync so refresh doesn't log users out unexpectedly.
    if (!sessionJwt && localJwt) {
      sessionStorage.setItem("jwt", localJwt);
    }
    if (sessionJwt && localStorage.getItem("jwt") !== sessionJwt) {
      localStorage.setItem("jwt", sessionJwt);
    }

    if (profileFetchAttemptRef.current.token !== jwt) {
      profileFetchAttemptRef.current = { token: jwt, attempted: false };
    }

    if (userProfile) {
      setAuthInitialized(true);
      return;
    }

    if (userLoading) {
      return;
    }

    if (profileFetchAttemptRef.current.attempted) {
      setAuthInitialized(true);
      return;
    }

    profileFetchAttemptRef.current.attempted = true;

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
  }, [dispatch, userProfile, store]);

  useEffect(() => {
    if (!userProfile) {
      setCustomerScopeKey(null);
      return;
    }

    const role = userProfile.role;
    const shouldManageCustomers =
      role === "ROLE_BRANCH_CASHIER" ||
      role === "ROLE_BRANCH_MANAGER" ||
      role === "ROLE_BRANCH_ADMIN" ||
      role === "ROLE_STORE_ADMIN" ||
      role === "ROLE_STORE_MANAGER";

    if (!shouldManageCustomers) {
      return;
    }

    const activeBranchId = branch?.id ?? userProfile?.branchId ?? "none";
    const activeStoreId = branch?.storeId ?? userProfile?.storeId ?? store?.id ?? "none";
    const nextScopeKey = `${userProfile?.id || "anon"}:${activeStoreId}:${activeBranchId}`;

    if (nextScopeKey === customerScopeKey) {
      return;
    }

    setCustomerScopeKey(nextScopeKey);
    dispatch(clearCustomerState());
    dispatch(setSelectedCustomer(null));
    dispatch(resetCheckoutContext());
    dispatch(clearInvoiceState());
    dispatch(clearOrderPreviewState());
    dispatch(getAllCustomers());
  }, [dispatch, userProfile, branch?.id, branch?.storeId, store?.id, customerScopeKey]);

  const isStoreRole =
    userProfile?.role === "ROLE_STORE_ADMIN" ||
    userProfile?.role === "ROLE_STORE_MANAGER";

  if (authInitialized && hasStoredToken && !userProfile) {
    return (
      <div className="min-h-screen flex items-center justify-center text-muted-foreground">
        Restoring your session...
      </div>
    );
  }

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
          <Route path="/auth/*" element={<Navigate to="/super-admin" replace />} />
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
          <Route path="/auth/*" element={<Navigate to="/cashier" replace />} />
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
            <Route path="/auth/*" element={<Navigate to="/store" replace />} />
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
          <Route path="/auth/*" element={<Navigate to="/branch" replace />} />
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
