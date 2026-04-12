import { createAsyncThunk } from "@reduxjs/toolkit";
import {
  completeOrderPayment as completeOrderPaymentApi,
  getInvoiceStatus as getInvoiceStatusApi,
  sendInvoiceEmail as sendInvoiceEmailApi,
} from "@/utils/api";

const unwrapData = (response) => response?.data ?? response;

export const completeOrderPayment = createAsyncThunk(
  "invoice/completePayment",
  async ({ orderId, payload }, { rejectWithValue }) => {
    try {
      const response = await completeOrderPaymentApi(orderId, payload);
      return unwrapData(response);
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to complete payment" });
    }
  }
);

export const sendInvoiceEmail = createAsyncThunk(
  "invoice/sendEmail",
  async ({ invoiceId, payload }, { rejectWithValue }) => {
    try {
      const response = await sendInvoiceEmailApi(invoiceId, payload);
      return unwrapData(response);
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to send invoice email" });
    }
  }
);

export const fetchInvoiceStatus = createAsyncThunk(
  "invoice/fetchStatus",
  async (invoiceId, { rejectWithValue }) => {
    try {
      const response = await getInvoiceStatusApi(invoiceId);
      return unwrapData(response);
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to fetch invoice status" });
    }
  }
);
