import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getStoreByAdmin, saveStoreSettings, getStoreSettings } from "@/Redux Toolkit/features/store/storeThunks";
import { toast } from "@/components/ui/use-toast";
import { extractApiError, mapApiErrorsByField } from "@/utils/apiError";
import {
  SettingsHeader,
  SettingsNavigation,
  SettingsContent
} from "./components";

export default function Settings() {
  const dispatch = useDispatch();
  const { store, storeSettings: storeSettingsState } = useSelector((state) => state.store);
  const [fieldErrors, setFieldErrors] = useState({});
  const [storeSettings, setStoreSettings] = useState({
    storeName: "",
    storeEmail: "",
    storePhone: "",
    storeAddress: "",
    storeLogo: "",
    storeDescription: "",
    currency: "",
    taxRate: "",
    timezone: "",
    dateFormat: "",
    receiptFooter: "",
  });

  const [notificationSettings, setNotificationSettings] = useState({
    emailNotifications: false,
    smsNotifications: false,
    lowStockAlerts: false,
    salesReports: false,
    employeeActivity: false,
  });

  const [securitySettings, setSecuritySettings] = useState({
    twoFactorAuth: false,
    passwordExpiry: "",
    sessionTimeout: "",
    ipRestriction: false,
  });

  const [paymentSettings, setPaymentSettings] = useState({
    acceptCash: false,
    acceptCredit: false,
    acceptDebit: false,
    acceptMobile: false,
    stripeEnabled: false,
    paypalEnabled: false,
  });

  const [activeSection, setActiveSection] = useState("store-settings");

  // Fetch store and settings data on component mount
  useEffect(() => {
    const fetchStoreData = async () => {
      try {
        const storeData = await dispatch(getStoreByAdmin()).unwrap();
        // After getting store ID, fetch the settings
        if (storeData?.id) {
          await dispatch(getStoreSettings(storeData.id)).unwrap();
        }
      } catch (err) {
        toast({
          title: "Error",
          description: err || "Failed to fetch store data",
          variant: "destructive",
        });
      }
    };

    fetchStoreData();
  }, [dispatch]);

  // Update store settings when store data is loaded
  useEffect(() => {
    if (store) {
      setStoreSettings({
        storeName: store.brand || "",
        storeEmail: store.contact?.email || "",
        storePhone: store.contact?.phone || "",
        storeAddress: store.contact?.address || "",
        storeDescription: store.description || "",
        currency: store.currency || "USD",
        taxRate: store.taxRate?.toString() || "0",
        timezone: store.timezone || "America/New_York",
        dateFormat: store.dateFormat || "MM/DD/YYYY",
        receiptFooter: store.receiptFooter || "",
      });
    }
  }, [store]);

  // Initialize security, notification, and payment settings from Redux cache
  useEffect(() => {
    if (storeSettingsState?.data) {
      const { 
        storeSettings: apiStoreSettings, 
        securitySettings: apiSecuritySettings, 
        notificationSettings: apiNotificationSettings, 
        paymentSettings: apiPaymentSettings 
      } = storeSettingsState.data;
      
      // Only update if sections exist (handle null/empty responses)
      if (apiStoreSettings) {
        setStoreSettings((prev) => ({
          ...prev,
          storeName: apiStoreSettings.storeName || prev.storeName,
          storeEmail: apiStoreSettings.storeEmail || prev.storeEmail,
          storePhone: apiStoreSettings.storePhone || prev.storePhone,
          storeAddress: apiStoreSettings.storeAddress || prev.storeAddress,
          storeDescription: apiStoreSettings.storeDescription || prev.storeDescription,
          currency: apiStoreSettings.currency || prev.currency,
          taxRate: apiStoreSettings.taxRate?.toString() || prev.taxRate,
          timezone: apiStoreSettings.timezone || prev.timezone,
          dateFormat: apiStoreSettings.dateFormat || prev.dateFormat,
          receiptFooter: apiStoreSettings.receiptFooter || prev.receiptFooter,
        }));
      }
      
      if (apiSecuritySettings) {
        setSecuritySettings((prev) => ({
          ...prev,
          twoFactorAuth: apiSecuritySettings.twoFactorAuth ?? prev.twoFactorAuth,
          passwordExpiry: apiSecuritySettings.passwordExpiry ?? prev.passwordExpiry,
          sessionTimeout: apiSecuritySettings.sessionTimeout ?? prev.sessionTimeout,
          ipRestriction: apiSecuritySettings.ipRestriction ?? prev.ipRestriction,
        }));
      }
      
      if (apiNotificationSettings) {
        setNotificationSettings((prev) => ({
          ...prev,
          emailNotifications: apiNotificationSettings.emailNotifications ?? prev.emailNotifications,
          smsNotifications: apiNotificationSettings.smsNotifications ?? prev.smsNotifications,
          lowStockAlerts: apiNotificationSettings.lowStockAlerts ?? prev.lowStockAlerts,
          salesReports: apiNotificationSettings.salesReports ?? prev.salesReports,
          employeeActivity: apiNotificationSettings.employeeActivity ?? prev.employeeActivity,
        }));
      }
      
      if (apiPaymentSettings) {
        setPaymentSettings((prev) => ({
          ...prev,
          acceptCash: apiPaymentSettings.acceptCash ?? prev.acceptCash,
          acceptCredit: apiPaymentSettings.acceptCredit ?? prev.acceptCredit,
          acceptDebit: apiPaymentSettings.acceptDebit ?? prev.acceptDebit,
          acceptMobile: apiPaymentSettings.acceptMobile ?? prev.acceptMobile,
          stripeEnabled: apiPaymentSettings.stripeEnabled ?? prev.stripeEnabled,
          paypalEnabled: apiPaymentSettings.paypalEnabled ?? prev.paypalEnabled,
        }));
      }
    }
  }, [storeSettingsState?.data]);

  const handleStoreSettingsChange = useCallback((name, value) => {
    setStoreSettings((prev) => {
      if (prev[name] === value) return prev;
      return {
        ...prev,
        [name]: value,
      };
    });
  }, []);

  const handleNotificationSettingsChange = useCallback((name, value) => {
    setNotificationSettings((prev) => {
      if (prev[name] === value) return prev;
      return {
        ...prev,
        [name]: value,
      };
    });
  }, []);

  const handleSecuritySettingsChange = useCallback((name, value) => {
    setSecuritySettings((prev) => {
      if (prev[name] === value) return prev;
      return {
        ...prev,
        [name]: value,
      };
    });
  }, []);

  const handlePaymentSettingsChange = useCallback((name, value) => {
    setPaymentSettings((prev) => {
      if (prev[name] === value) return prev;
      return {
        ...prev,
        [name]: value,
      };
    });
  }, []);

  const handleSaveAllSettings = async () => {
    if (!store?.id) {
      toast({
        title: "Error",
        description: "Store information is missing",
        variant: "destructive",
      });
      return;
    }

    setFieldErrors({});

    const payload = {
      storeSettings: {
        storeName: storeSettings.storeName,
        storeEmail: storeSettings.storeEmail,
        storePhone: storeSettings.storePhone,
        storeAddress: storeSettings.storeAddress,
        storeDescription: storeSettings.storeDescription,
        currency: storeSettings.currency,
        taxRate: Number(storeSettings.taxRate),
        timezone: storeSettings.timezone,
        dateFormat: storeSettings.dateFormat,
        receiptFooter: storeSettings.receiptFooter,
      },
      notificationSettings,
      securitySettings: {
        ...securitySettings,
        passwordExpiry: Number(securitySettings.passwordExpiry),
        sessionTimeout: Number(securitySettings.sessionTimeout),
      },
      paymentSettings,
    };

    try {
      const data = await dispatch(
        saveStoreSettings({
          storeId: store.id,
          payload,
        })
      ).unwrap();

      // Update local state with the response data from the API
      if (data?.storeSettings) {
        setStoreSettings((prev) => ({ ...prev, ...data.storeSettings }));
      }
      if (data?.notificationSettings) {
        setNotificationSettings((prev) => ({ ...prev, ...data.notificationSettings }));
      }
      if (data?.securitySettings) {
        setSecuritySettings((prev) => ({ ...prev, ...data.securitySettings }));
      }
      if (data?.paymentSettings) {
        setPaymentSettings((prev) => ({ ...prev, ...data.paymentSettings }));
      }

      // Refetch settings from backend to ensure we have the latest saved data
      await dispatch(getStoreSettings(store.id)).unwrap();

      toast({
        title: "Saved",
        description: "Store settings saved successfully",
      });
    } catch (errorPayload) {
      const parsedError = extractApiError(errorPayload);
      const mappedErrors = mapApiErrorsByField(parsedError.errors);
      setFieldErrors(mappedErrors);

      toast({
        title: "Save Failed",
        description: parsedError.errors[0]?.message || parsedError.message,
        variant: "destructive",
      });
    }
  };

  

  return (
    <div className="space-y-6">
      <SettingsHeader />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-1">
          <SettingsNavigation activeSection={activeSection} />
        </div>

        <div className="md:col-span-2">
          <SettingsContent
            storeSettings={storeSettings}
            notificationSettings={notificationSettings}
            securitySettings={securitySettings}
            paymentSettings={paymentSettings}
            onStoreSettingsChange={handleStoreSettingsChange}
            onNotificationSettingsChange={handleNotificationSettingsChange}
            onSecuritySettingsChange={handleSecuritySettingsChange}
            onPaymentSettingsChange={handlePaymentSettingsChange}
            onSave={handleSaveAllSettings}
            isSaving={storeSettingsState.saving}
            fieldErrors={fieldErrors}
          />
        </div>
      </div>
    </div>
  );
}