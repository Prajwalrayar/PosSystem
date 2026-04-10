import { createAsyncThunk } from '@reduxjs/toolkit';
import { getCommissions as getCommissionsApi, updateCommission as updateCommissionApi } from '@/utils/api';

export const fetchCommissions = createAsyncThunk(
  'commissions/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const res = await getCommissionsApi();
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);

export const updateCommissionRate = createAsyncThunk(
  'commissions/updateRate',
  async ({ storeId, rate }, { rejectWithValue }) => {
    try {
      const res = await updateCommissionApi(storeId, { rate });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);
