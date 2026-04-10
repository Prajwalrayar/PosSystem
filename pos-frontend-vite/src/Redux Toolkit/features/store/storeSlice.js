import { createSlice } from '@reduxjs/toolkit';
import {
  createStore,
  getStoreById,
  getAllStores,
  updateStore,
  deleteStore,
  getStoreByAdmin,
  getStoreByEmployee,
  getStoreEmployees,
  addEmployee,
  moderateStore,
  saveStoreSettings,
  getStoreSettings,
} from './storeThunks';

const initialState = {
  store: null,
  stores: [],
  employees: [],
  loading: false,
  error: null,
  storeSettings: {
    data: null,
    saving: false,
    error: null,
    successMessage: null,
  },
};

const storeSlice = createSlice({
  name: 'store',
  initialState,
  reducers: {
    clearStoreState: (state) => {
      state.store = null;
      state.error = null;
      state.employees = [];
      state.storeSettings = {
        data: null,
        saving: false,
        error: null,
        successMessage: null,
      };
    },
    clearStoreSettingsState: (state) => {
      state.storeSettings.error = null;
      state.storeSettings.successMessage = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createStore.pending, (state) => {
        state.loading = true;
      })
      .addCase(createStore.fulfilled, (state, action) => {
        state.loading = false;
        state.store = action.payload;
      })
      .addCase(createStore.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(getStoreById.fulfilled, (state, action) => {
        state.store = action.payload;
      })
      .addCase(getAllStores.fulfilled, (state, action) => {
        state.stores = action.payload;
      })
      .addCase(updateStore.fulfilled, (state, action) => {
        state.store = action.payload;
      })
      .addCase(deleteStore.fulfilled, (state) => {
        state.store = null;
      })
      .addCase(getStoreByAdmin.fulfilled, (state, action) => {
        state.store = action.payload;
      })
      .addCase(getStoreByEmployee.fulfilled, (state, action) => {
        state.store = action.payload;
      })
      .addCase(getStoreEmployees.fulfilled, (state, action) => {
        state.employees = action.payload;
      })
      .addCase(addEmployee.fulfilled, (state, action) => {
        state.employees.push(action.payload);
      })

      // Update store in list after moderation
      .addCase(moderateStore.fulfilled, (state, action) => {
        const updated = action.payload;
        state.stores = state.stores.map(store =>
          store.id === updated.id ? updated : store
        );
      })

      .addCase(saveStoreSettings.pending, (state) => {
        state.storeSettings.saving = true;
        state.storeSettings.error = null;
        state.storeSettings.successMessage = null;
      })
      .addCase(saveStoreSettings.fulfilled, (state, action) => {
        state.storeSettings.saving = false;
        state.storeSettings.data = action.payload;
        state.storeSettings.successMessage = 'Store settings saved successfully';
      })
      .addCase(saveStoreSettings.rejected, (state, action) => {
        state.storeSettings.saving = false;
        state.storeSettings.error = action.payload;
      })

      .addCase(getStoreSettings.pending, (state) => {
        state.loading = true;
      })
      .addCase(getStoreSettings.fulfilled, (state, action) => {
        state.loading = false;
        state.storeSettings.data = action.payload;
      })
      .addCase(getStoreSettings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addMatcher(
        (action) => action.type.startsWith('store/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = action.payload;
        }
      );
  },
});

export const { clearStoreState, clearStoreSettingsState } = storeSlice.actions;
export default storeSlice.reducer;
