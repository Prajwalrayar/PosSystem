import { createAsyncThunk } from '@reduxjs/toolkit';
import { initiateReturn as initiateReturnApi } from '@/utils/api';

export const initiateReturn = createAsyncThunk(
  'returns/initiate',
  async ({ orderId, cashierId, branchId }, { rejectWithValue }) => {
    try {
      const res = await initiateReturnApi({ orderId, cashierId, branchId });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);
