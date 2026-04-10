import { createSlice } from '@reduxjs/toolkit';
import { createPrintJob } from './printThunks';

const initialState = {
  creating: false,
  lastJob: null,
  error: null,
};

const printSlice = createSlice({
  name: 'print',
  initialState,
  reducers: {
    clearPrintState: (state) => {
      state.creating = false;
      state.lastJob = null;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createPrintJob.pending, (state) => {
        state.creating = true;
        state.error = null;
      })
      .addCase(createPrintJob.fulfilled, (state, action) => {
        state.creating = false;
        state.lastJob = action.payload;
      })
      .addCase(createPrintJob.rejected, (state, action) => {
        state.creating = false;
        state.error = action.payload;
      });
  },
});

export const { clearPrintState } = printSlice.actions;
export default printSlice.reducer;
