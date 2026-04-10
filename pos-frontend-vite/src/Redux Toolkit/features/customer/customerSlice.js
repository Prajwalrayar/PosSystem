import { createSlice } from '@reduxjs/toolkit';
import {
  createCustomer,
  updateCustomer,
  deleteCustomer,
  getCustomerById,
  getAllCustomers,
  addCustomerPoints,
} from './customerThunks';

const initialState = {
  customers: [],
  selectedCustomer: null,
  loading: false,
  error: null,
  loyaltyPointsStatus: {
    isLoading: false,
    error: null,
    successMessage: null,
  },
};

const customerSlice = createSlice({
  name: 'customer',
  initialState,
  reducers: {
    clearCustomerState: (state) => {
      state.customers = [];
      state.selectedCustomer = null;
      state.error = null;
      state.loyaltyPointsStatus = { isLoading: false, error: null, successMessage: null };
    },
    clearSelectedCustomer: (state) => {
      state.selectedCustomer = null;
    },
    clearLoyaltyPointsState: (state) => {
      state.loyaltyPointsStatus = { isLoading: false, error: null, successMessage: null };
    },
  },
  extraReducers: (builder) => {
    builder
      // Create Customer
      .addCase(createCustomer.pending, (state) => {
        state.loading = true;
      })
      .addCase(createCustomer.fulfilled, (state, action) => {
        state.loading = false;
        state.customers.push(action.payload);
      })
      .addCase(createCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Update Customer
      .addCase(updateCustomer.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateCustomer.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.customers.findIndex(customer => customer.id === action.payload.id);
        if (index !== -1) {
          state.customers[index] = action.payload;
        }
        if (state.selectedCustomer && state.selectedCustomer.id === action.payload.id) {
          state.selectedCustomer = action.payload;
        }
      })
      .addCase(updateCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Delete Customer
      .addCase(deleteCustomer.pending, (state) => {
        state.loading = true;
      })
      .addCase(deleteCustomer.fulfilled, (state, action) => {
        state.loading = false;
        state.customers = state.customers.filter(customer => customer.id !== action.payload);
        if (state.selectedCustomer && state.selectedCustomer.id === action.payload) {
          state.selectedCustomer = null;
        }
      })
      .addCase(deleteCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get Customer by ID
      .addCase(getCustomerById.pending, (state) => {
        state.loading = true;
      })
      .addCase(getCustomerById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedCustomer = action.payload;
      })
      .addCase(getCustomerById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Get All Customers
      .addCase(getAllCustomers.pending, (state) => {
        state.loading = true;
      })
      .addCase(getAllCustomers.fulfilled, (state, action) => {
        state.loading = false;
        state.customers = action.payload;
      })
      .addCase(getAllCustomers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(addCustomerPoints.pending, (state) => {
        state.loyaltyPointsStatus.isLoading = true;
        state.loyaltyPointsStatus.error = null;
        state.loyaltyPointsStatus.successMessage = null;
      })
      .addCase(addCustomerPoints.fulfilled, (state, action) => {
        state.loyaltyPointsStatus.isLoading = false;
        state.loyaltyPointsStatus.error = null;
        state.loyaltyPointsStatus.successMessage = 'Loyalty points updated successfully';

        const updatedCustomerId = action.payload?.customerId;
        const updatedPoints = action.payload?.pointsAfter;

        if (updatedCustomerId && typeof updatedPoints === 'number') {
          state.customers = state.customers.map((customer) =>
            customer.id === updatedCustomerId ? { ...customer, loyaltyPoints: updatedPoints } : customer
          );

          if (state.selectedCustomer?.id === updatedCustomerId) {
            state.selectedCustomer = {
              ...state.selectedCustomer,
              loyaltyPoints: updatedPoints,
            };
          }
        }
      })
      .addCase(addCustomerPoints.rejected, (state, action) => {
        state.loyaltyPointsStatus.isLoading = false;
        state.loyaltyPointsStatus.error = action.payload;
      })

      // Generic error handling for all customer actions
      .addMatcher(
        (action) => action.type.startsWith('customer/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = action.payload;
        }
      );
  },
});

export const { clearCustomerState, clearSelectedCustomer, clearLoyaltyPointsState } = customerSlice.actions;
export default customerSlice.reducer; 