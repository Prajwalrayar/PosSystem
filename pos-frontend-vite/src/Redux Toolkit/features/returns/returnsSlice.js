import { createSlice } from '@reduxjs/toolkit';
import { initiateReturn } from './returnsThunks';

const initialState = {
  creating: false,
  returnSessionId: null,
  eligibleItems: [],
  error: null,
};

const returnsSlice = createSlice({
  name: 'returns',
  initialState,
  reducers: {
    clearReturnSession: (state) => {
      state.returnSessionId = null;
      state.eligibleItems = [];
      state.error = null;
      state.creating = false;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(initiateReturn.pending, (state) => {
        state.creating = true;
        state.error = null;
      })
      .addCase(initiateReturn.fulfilled, (state, action) => {
        state.creating = false;
        state.returnSessionId = action.payload?.returnSessionId || null;
        state.eligibleItems = action.payload?.eligibleItems || [];
      })
      .addCase(initiateReturn.rejected, (state, action) => {
        state.creating = false;
        state.error = action.payload;
      });
  },
});

export const { clearReturnSession } = returnsSlice.actions;
export default returnsSlice.reducer;
