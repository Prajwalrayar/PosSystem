import { createSlice } from '@reduxjs/toolkit';
import {
  createBranch,
  getBranchById,
  getAllBranchesByStore,
  updateBranch,
  deleteBranch,
  saveBranchSettings,
  // addEmployeeToBranch,
  // getEmployeesByBranch
} from './branchThunks';

const initialState = {
  branch: null,
  branches: [],
  employees: [],
  loading: false,
  error: null,
  branchSettings: {
    data: null,
    saving: false,
    error: null,
    successMessage: null,
  },
};

const branchSlice = createSlice({
  name: 'branch',
  initialState,
  reducers: {
    clearBranchState: (state) => {
      state.branch = null;
      state.branches = [];
      state.employees = [];
      state.error = null;
      state.branchSettings = {
        data: null,
        saving: false,
        error: null,
        successMessage: null,
      };
    },
    clearBranchSettingsState: (state) => {
      state.branchSettings.error = null;
      state.branchSettings.successMessage = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createBranch.pending, (state) => {
        state.loading = true;
      })
      .addCase(createBranch.fulfilled, (state, action) => {
        state.loading = false;
        state.branch = action.payload;
        state.branches.push(action.payload);
      })
      .addCase(createBranch.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(getBranchById.pending, (state) => {
        state.loading = true;
      })
      .addCase(getBranchById.fulfilled, (state, action) => {
        state.loading = false;
        state.branch = action.payload;
      })
      .addCase(getBranchById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(getAllBranchesByStore.fulfilled, (state, action) => {
        state.branches = action.payload;
      })

      .addCase(updateBranch.fulfilled, (state, action) => {
        state.branch = action.payload;
      })

      .addCase(deleteBranch.fulfilled, (state, action) => {
        state.branches = state.branches.filter((b) => b.id !== action.payload);
      })

      .addCase(saveBranchSettings.pending, (state) => {
        state.branchSettings.saving = true;
        state.branchSettings.error = null;
        state.branchSettings.successMessage = null;
      })
      .addCase(saveBranchSettings.fulfilled, (state, action) => {
        state.branchSettings.saving = false;
        state.branchSettings.data = action.payload;
        state.branchSettings.successMessage = 'Branch settings saved successfully';
      })
      .addCase(saveBranchSettings.rejected, (state, action) => {
        state.branchSettings.saving = false;
        state.branchSettings.error = action.payload;
      })



      .addMatcher(
        (action) => action.type.startsWith('branch/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = action.payload;
          state.loading = false;
        }
      );
  },
});

export const { clearBranchState, clearBranchSettingsState } = branchSlice.actions;
export default branchSlice.reducer;
