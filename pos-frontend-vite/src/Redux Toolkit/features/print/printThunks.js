import { createAsyncThunk } from '@reduxjs/toolkit';
import { createPrintJob as createPrintJobApi } from '@/utils/api';

export const createPrintJob = createAsyncThunk(
  'print/createJob',
  async ({ type, referenceId, printerId }, { rejectWithValue }) => {
    try {
      const res = await createPrintJobApi({ type, referenceId, printerId });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);
