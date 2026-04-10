import { createSlice } from '@reduxjs/toolkit';
import { fetchCommissions, updateCommissionRate } from './commissionsThunks';

const initialState = {
  items: [],
  loading: false,
  updateLoading: false,
  error: null,
  updatingStoreId: null,
};

const commissionsSlice = createSlice({
  name: 'commissions',
  initialState,
  reducers: {
    clearCommissionsError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCommissions.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCommissions.fulfilled, (state, action) => {
        state.loading = false;
        state.items = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchCommissions.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(updateCommissionRate.pending, (state, action) => {
        state.updateLoading = true;
        state.error = null;
        state.updatingStoreId = action.meta.arg.storeId;
      })
      .addCase(updateCommissionRate.fulfilled, (state, action) => {
        state.updateLoading = false;
        const updated = action.payload;
        if (updated?.storeId !== undefined && updated?.storeId !== null) {
          const index = state.items.findIndex((item) => String(item.storeId) === String(updated.storeId));
          if (index !== -1) {
            state.items[index] = {
              ...state.items[index],
              ...updated,
            };
          }
        }
        state.updatingStoreId = null;
      })
      .addCase(updateCommissionRate.rejected, (state, action) => {
        state.updateLoading = false;
        state.error = action.payload;
        state.updatingStoreId = null;
      });
  },
});

export const { clearCommissionsError } = commissionsSlice.actions;
export default commissionsSlice.reducer;
