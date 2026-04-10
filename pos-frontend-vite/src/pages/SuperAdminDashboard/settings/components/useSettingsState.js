import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useToast } from "@/components/ui/use-toast";
import {
  getUserProfile,
  updateProfile,
  changePassword,
} from "@/Redux Toolkit/features/user/userThunks";
import { clearPasswordChangeState, clearProfileUpdateState } from "@/Redux Toolkit/features/user/userSlice";
import { getApiErrorMessage } from "@/utils/apiError";

export const useSettingsState = () => {
  const dispatch = useDispatch();
  const { userProfile, loading, profileUpdateStatus, passwordChangeStatus } = useSelector((state) => state.user);
  const { toast } = useToast();

  const [profileData, setProfileData] = useState({
    fullName: "",
    email: "",
    phone: "",
  });

  useEffect(() => {
    const token = localStorage.getItem("jwt");
    if (token) {
      dispatch(getUserProfile(token));
    }
  }, [dispatch]);

  useEffect(() => {
    if (userProfile) {
      setProfileData({
        fullName: userProfile.fullName || "",
        email: userProfile.email || "",
        phone: userProfile.phone || userProfile.mobile || "",
      });
    }
    if (userProfile?.email) {
      console.log("User data loaded:", userProfile.email);
    }
  }, [userProfile]);

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  });

  const [notifications, setNotifications] = useState({
    newStoreRequests: true,
    storeApprovals: true,
    commissionUpdates: false,
    systemAlerts: true,
    emailNotifications: true,
  });

  const [systemSettings, setSystemSettings] = useState({
    autoApproveStores: false,
    requireDocumentVerification: true,
    commissionAutoCalculation: true,
    maintenanceMode: false,
  });

  const handleProfileUpdate = async () => {
    if (!profileData.fullName?.trim()) {
      toast({
        title: "Validation Error",
        description: "Full name is required.",
        variant: "destructive",
      });
      return;
    }

    dispatch(clearProfileUpdateState());

    try {
      await dispatch(
        updateProfile({
          fullName: profileData.fullName.trim(),
          phone: profileData.phone,
        })
      ).unwrap();

      // Refetch latest profile from backend to ensure frontend shows current data
      const token = localStorage.getItem("jwt");
      if (token) {
        await dispatch(getUserProfile(token)).unwrap();
      }

      toast({
        title: "Profile Updated",
        description: "Profile updated successfully.",
      });
    } catch (errorPayload) {
      toast({
        title: "Update Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };

  const handlePasswordChange = async () => {
    if (!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
      toast({
        title: "Validation Error",
        description: "All password fields are required.",
        variant: "destructive",
      });
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast({
        title: "Password Mismatch",
        description: "New password and confirm password do not match.",
        variant: "destructive",
      });
      return;
    }

    if (passwordData.newPassword.length < 8) {
      toast({
        title: "Weak Password",
        description: "Password must be at least 8 characters long.",
        variant: "destructive",
      });
      return;
    }

    dispatch(clearPasswordChangeState());

    try {
      await dispatch(
        changePassword({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.confirmPassword,
        })
      ).unwrap();

      toast({
        title: "Password Changed",
        description: "Password changed successfully.",
      });

      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (errorPayload) {
      toast({
        title: "Change Failed",
        description: getApiErrorMessage(errorPayload),
        variant: "destructive",
      });
    }
  };

  const handleNotificationToggle = (key) => {
    setNotifications((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const handleSystemSettingToggle = (key) => {
    setSystemSettings((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const handleProfileFieldChange = (field, value) => {
    setProfileData((prev) => ({ ...prev, [field]: value }));
  };

  const handlePasswordFieldChange = (field, value) => {
    setPasswordData((prev) => ({ ...prev, [field]: value }));
  };
  
  const handleShowPasswordToggle = (field) => {
    setShowPasswords((prev) => ({ ...prev, [field]: !prev[field] }));
  };
  
  return {
    profileData,
    loading,
    profileUpdateStatus,
    passwordChangeStatus,
    passwordData,
    showPasswords,
    notifications,
    systemSettings,
    handleProfileUpdate,
    handlePasswordChange,
    handleNotificationToggle,
    handleSystemSettingToggle,
    handleProfileFieldChange,
    handlePasswordFieldChange,
    handleShowPasswordToggle
  };
}; 