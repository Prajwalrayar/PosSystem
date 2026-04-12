import { createSlice } from "@reduxjs/toolkit";
import {
  fetchSuperAdminSettings,
  updateNotificationSettings,
  updateSystemSettings,
} from "./superAdminSettingsThunks";

const initialState = {
  notificationSettings: null,
  systemSettings: null,
  loading: false,
  savingNotification: false,
  savingSystem: false,
  error: null,
  notificationError: null,
  systemError: null,
};

const superAdminSettingsSlice = createSlice({
  name: "superAdminSettings",
  initialState,
  reducers: {
    setNotificationSettingField: (state, action) => {
      const { field, value } = action.payload;
      state.notificationSettings = state.notificationSettings || {};
      state.notificationSettings[field] = value;
    },
    setSystemSettingField: (state, action) => {
      const { field, value } = action.payload;
      state.systemSettings = state.systemSettings || {};
      state.systemSettings[field] = value;
    },
    clearSuperAdminSettingsErrors: (state) => {
      state.error = null;
      state.notificationError = null;
      state.systemError = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchSuperAdminSettings.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSuperAdminSettings.fulfilled, (state, action) => {
        state.loading = false;
        state.notificationSettings = action.payload.notificationSettings || {};
        state.systemSettings = action.payload.systemSettings || {};
      })
      .addCase(fetchSuperAdminSettings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(updateNotificationSettings.pending, (state) => {
        state.savingNotification = true;
        state.notificationError = null;
      })
      .addCase(updateNotificationSettings.fulfilled, (state, action) => {
        state.savingNotification = false;
        state.notificationSettings = action.payload || state.notificationSettings;
      })
      .addCase(updateNotificationSettings.rejected, (state, action) => {
        state.savingNotification = false;
        state.notificationError = action.payload;
      })
      .addCase(updateSystemSettings.pending, (state) => {
        state.savingSystem = true;
        state.systemError = null;
      })
      .addCase(updateSystemSettings.fulfilled, (state, action) => {
        state.savingSystem = false;
        state.systemSettings = action.payload || state.systemSettings;
      })
      .addCase(updateSystemSettings.rejected, (state, action) => {
        state.savingSystem = false;
        state.systemError = action.payload;
      });
  },
});

export const {
  setNotificationSettingField,
  setSystemSettingField,
  clearSuperAdminSettingsErrors,
} = superAdminSettingsSlice.actions;

export default superAdminSettingsSlice.reducer;
