import { createSlice } from '@reduxjs/toolkit';
import {
  createStoreEmployee,
  createBranchEmployee,
  updateEmployee,
  deleteEmployee,
  findEmployeeById,
  findStoreEmployees,
  findBranchEmployees,
  resetEmployeePassword,
  fetchEmployeePerformance,
} from './employeeThunks';

const initialState = {
  employees: [],
  employee: null,
  loading: false,
  error: null,
  resetPasswordStatus: {
    isLoading: false,
    error: null,
    successMessage: null,
  },
  performance: {
    loading: false,
    data: null,
    error: null,
  },
};

const employeeSlice = createSlice({
  name: 'employee',
  initialState,
  reducers: {
    clearEmployeeState: (state) => {
      state.employee = null;
      state.employees = [];
      state.error = null;
      state.resetPasswordStatus = { isLoading: false, error: null, successMessage: null };
      state.performance = { loading: false, data: null, error: null };
    },
    clearResetPasswordState: (state) => {
      state.resetPasswordStatus = { isLoading: false, error: null, successMessage: null };
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createStoreEmployee.pending, (state) => {
        state.loading = true;
      })
      .addCase(createStoreEmployee.fulfilled, (state, action) => {
        state.loading = false;
        state.employees.push(action.payload);
      })
      .addCase(createStoreEmployee.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(createBranchEmployee.pending, (state) => {
        state.loading = true;
      })
      .addCase(createBranchEmployee.fulfilled, (state, action) => {
        state.loading = false;
        state.employees.push(action.payload);
      })
      .addCase(createBranchEmployee.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(updateEmployee.fulfilled, (state, action) => {
        const index = state.employees.findIndex((e) => e.id === action.payload.id);
        if (index !== -1) {
          state.employees[index] = action.payload;
        }
      })

      .addCase(deleteEmployee.fulfilled, (state, action) => {
        state.employees = state.employees.filter((e) => e.id !== action.payload);
      })

      .addCase(findEmployeeById.fulfilled, (state, action) => {
        state.employee = action.payload;
      })

      .addCase(findStoreEmployees.fulfilled, (state, action) => {
        state.employees = action.payload;
      })

      .addCase(findBranchEmployees.fulfilled, (state, action) => {
        state.employees = action.payload;
        // console.log("")
      })

      .addCase(resetEmployeePassword.pending, (state) => {
        state.resetPasswordStatus.isLoading = true;
        state.resetPasswordStatus.error = null;
        state.resetPasswordStatus.successMessage = null;
      })
      .addCase(resetEmployeePassword.fulfilled, (state, action) => {
        state.resetPasswordStatus.isLoading = false;
        state.resetPasswordStatus.error = null;
        state.resetPasswordStatus.successMessage = 'Employee password reset successful';

        if (action.payload?.employeeId) {
          const index = state.employees.findIndex((employee) => employee.id === action.payload.employeeId);
          if (index !== -1) {
            state.employees[index] = {
              ...state.employees[index],
              forceChangeOnNextLogin: action.payload.forceChangeOnNextLogin,
            };
          }
        }
      })
      .addCase(resetEmployeePassword.rejected, (state, action) => {
        state.resetPasswordStatus.isLoading = false;
        state.resetPasswordStatus.error = action.payload;
      })

      .addCase(fetchEmployeePerformance.pending, (state) => {
        state.performance.loading = true;
        state.performance.error = null;
      })
      .addCase(fetchEmployeePerformance.fulfilled, (state, action) => {
        state.performance.loading = false;
        state.performance.data = action.payload;
      })
      .addCase(fetchEmployeePerformance.rejected, (state, action) => {
        state.performance.loading = false;
        state.performance.error = action.payload;
      })

      .addMatcher(
        (action) => action.type.startsWith('employee/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = action.payload;
        }
      );
  },
});

export const { clearEmployeeState, clearResetPasswordState } = employeeSlice.actions;
export default employeeSlice.reducer;