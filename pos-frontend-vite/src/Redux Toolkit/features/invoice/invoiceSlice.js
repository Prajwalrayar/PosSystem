import { createSlice } from "@reduxjs/toolkit";
import { completeOrderPayment, fetchInvoiceStatus, sendInvoiceEmail } from "./invoiceThunks";

const initialState = {
  currentInvoice: null,
  completePaymentLoading: false,
  emailSending: false,
  statusLoading: false,
  error: null,
  successMessage: null,
};

const invoiceSlice = createSlice({
  name: "invoice",
  initialState,
  reducers: {
    clearInvoiceState: (state) => {
      state.currentInvoice = null;
      state.completePaymentLoading = false;
      state.emailSending = false;
      state.statusLoading = false;
      state.error = null;
      state.successMessage = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(completeOrderPayment.pending, (state) => {
        state.completePaymentLoading = true;
        state.error = null;
        state.successMessage = null;
      })
      .addCase(completeOrderPayment.fulfilled, (state, action) => {
        state.completePaymentLoading = false;
        state.currentInvoice = action.payload;
        state.successMessage = "Invoice created successfully";
      })
      .addCase(completeOrderPayment.rejected, (state, action) => {
        state.completePaymentLoading = false;
        state.error = action.payload;
      })
      .addCase(sendInvoiceEmail.pending, (state) => {
        state.emailSending = true;
        state.error = null;
        state.successMessage = null;
      })
      .addCase(sendInvoiceEmail.fulfilled, (state, action) => {
        state.emailSending = false;
        state.currentInvoice = {
          ...(state.currentInvoice || {}),
          ...(action.payload || {}),
        };
        state.successMessage = "Invoice email sent";
      })
      .addCase(sendInvoiceEmail.rejected, (state, action) => {
        state.emailSending = false;
        state.error = action.payload;
      })
      .addCase(fetchInvoiceStatus.pending, (state) => {
        state.statusLoading = true;
        state.error = null;
      })
      .addCase(fetchInvoiceStatus.fulfilled, (state, action) => {
        state.statusLoading = false;
        state.currentInvoice = {
          ...(state.currentInvoice || {}),
          ...(action.payload || {}),
        };
      })
      .addCase(fetchInvoiceStatus.rejected, (state, action) => {
        state.statusLoading = false;
        state.error = action.payload;
      });
  },
});

export const { clearInvoiceState } = invoiceSlice.actions;
export default invoiceSlice.reducer;
