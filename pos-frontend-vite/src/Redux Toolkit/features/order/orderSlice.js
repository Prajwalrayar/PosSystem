import { createSlice } from '@reduxjs/toolkit';
import {
  createOrder,
  getOrderById,
  getOrdersByBranch,
  getOrdersByCashier,
  getTodayOrdersByBranch,
  deleteOrder,
  getOrdersByCustomer,
  getRecentOrdersByBranch
} from './orderThunks';
import { logout } from '../user/userThunks';

const getErrorMessage = (payload, fallback = 'Something went wrong') => {
  if (!payload) {
    return fallback;
  }
  if (typeof payload === 'string') {
    return payload;
  }
  return payload.message || fallback;
};

const initialState = {
  orders: [],
  todayOrders: [],
  customerOrders: [],
  selectedOrder: null,
  loading: false,
  error: null,
  recentOrders: [], // Added for recent orders
};

const orderSlice = createSlice({
  name: 'order',
  initialState,
  reducers: {
    clearOrderState: (state) => {
      state.orders = [];
      state.todayOrders = [];
      state.customerOrders = [];
      state.selectedOrder = null;
      state.error = null;
    },
    clearCustomerOrders: (state) => {
      state.customerOrders = [];
    },
    clearOrderPreviewState: (state) => {
      state.selectedOrder = null;
      state.customerOrders = [];
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createOrder.pending, (state) => {
        state.loading = true;
      })
      .addCase(createOrder.fulfilled, (state, action) => {
        state.loading = false;
        state.orders.push(action.payload);
        state.selectedOrder=action.payload;
      })
      .addCase(createOrder.rejected, (state, action) => {
        state.loading = false;
        state.error = getErrorMessage(action.payload, 'Failed to create order');
      })

      .addCase(getOrderById.fulfilled, (state, action) => {
        state.selectedOrder = action.payload;
      })

      .addCase(getOrdersByBranch.fulfilled, (state, action) => {
        state.orders = action.payload;
      })

      .addCase(getOrdersByCashier.fulfilled, (state, action) => {
        state.orders = action.payload;
        console.log("get order by cashier ", action.payload);
      })

      .addCase(getTodayOrdersByBranch.fulfilled, (state, action) => {
        state.todayOrders = action.payload;
      })

      .addCase(getOrdersByCustomer.fulfilled, (state, action) => {
        state.customerOrders = action.payload;
      })

      .addCase(getRecentOrdersByBranch.fulfilled, (state, action) => {
        state.recentOrders = action.payload;
      })

      .addCase(logout.fulfilled, (state) => {
        state.orders = [];
        state.todayOrders = [];
        state.customerOrders = [];
        state.selectedOrder = null;
        state.loading = false;
        state.error = null;
        state.recentOrders = [];
      })

      .addCase(deleteOrder.fulfilled, (state, action) => {
        state.orders = state.orders.filter((o) => o.id !== action.payload);
      })

      .addMatcher(
        (action) => action.type.startsWith('order/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = getErrorMessage(action.payload);
        }
      );
  },
});

export const { clearOrderState, clearCustomerOrders, clearOrderPreviewState } = orderSlice.actions;
export default orderSlice.reducer;
