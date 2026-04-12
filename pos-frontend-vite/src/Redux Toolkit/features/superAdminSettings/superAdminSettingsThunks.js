import { createAsyncThunk } from "@reduxjs/toolkit";
import {
  getSuperAdminSettings as getSuperAdminSettingsApi,
  saveSuperAdminNotificationSettings as saveSuperAdminNotificationSettingsApi,
  saveSuperAdminSystemSettings as saveSuperAdminSystemSettingsApi,
} from "@/api/superAdminSettings";

const normalizeResponseData = (response) => (response?.data ? response.data : response);

export const fetchSuperAdminSettings = createAsyncThunk(
  "superAdminSettings/fetch",
  async (_, { rejectWithValue }) => {
    try {
      const response = await getSuperAdminSettingsApi();
      const data = normalizeResponseData(response) || {};
      return {
        notificationSettings: data.notificationSettings || null,
        systemSettings: data.systemSettings || null,
      };
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to fetch settings" });
    }
  }
);

export const updateNotificationSettings = createAsyncThunk(
  "superAdminSettings/updateNotification",
  async (payload, { rejectWithValue }) => {
    try {
      const response = await saveSuperAdminNotificationSettingsApi(payload);
      const data = normalizeResponseData(response) || {};
      return data.notificationSettings || payload;
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to save notification settings" });
    }
  }
);

export const updateSystemSettings = createAsyncThunk(
  "superAdminSettings/updateSystem",
  async (payload, { rejectWithValue }) => {
    try {
      const response = await saveSuperAdminSystemSettingsApi(payload);
      const data = normalizeResponseData(response) || {};
      return data.systemSettings || payload;
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to save system settings" });
    }
  }
);
