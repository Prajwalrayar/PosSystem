import { createAsyncThunk } from "@reduxjs/toolkit";
import {
  completeOrderPayment as completeOrderPaymentApi,
  getInvoiceStatus as getInvoiceStatusApi,
  sendInvoiceEmail as sendInvoiceEmailApi,
} from "@/utils/api";

const unwrapData = (response) => response?.data ?? response;

const pickNumber = (...values) => {
  for (const value of values) {
    const parsed = Number(value);
    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }
  return null;
};

const normalizeInvoiceResponse = (response) => {
  const payload = unwrapData(response) || {};
  const invoice = payload?.invoice || payload?.data?.invoice || null;
  const order = payload?.order || payload?.data?.order || invoice?.order || null;

  const subtotal = pickNumber(
    invoice?.subtotal,
    invoice?.subTotal,
    payload?.subtotal,
    order?.subtotal
  );
  const taxTotal = pickNumber(
    invoice?.taxTotal,
    invoice?.taxAmount,
    payload?.taxTotal,
    payload?.taxAmount,
    order?.taxAmount
  );
  const discountTotal = pickNumber(
    invoice?.discountTotal,
    invoice?.discountAmount,
    payload?.discountTotal,
    payload?.discountAmount,
    order?.discountAmount
  );
  const grandTotal = pickNumber(
    invoice?.grandTotal,
    payload?.grandTotal,
    order?.totalAmount,
    payload?.totalAmount
  );

  return {
    ...payload,
    subtotal,
    taxTotal,
    discountTotal,
    grandTotal,
    invoice: invoice
      ? {
          ...invoice,
          subtotal: pickNumber(invoice?.subtotal, invoice?.subTotal, subtotal),
          taxTotal: pickNumber(invoice?.taxTotal, invoice?.taxAmount, taxTotal),
          discountTotal: pickNumber(invoice?.discountTotal, invoice?.discountAmount, discountTotal),
          grandTotal: pickNumber(invoice?.grandTotal, grandTotal),
        }
      : invoice,
    order: order
      ? {
          ...order,
          subtotal: pickNumber(order?.subtotal, subtotal),
          taxAmount: pickNumber(order?.taxAmount, taxTotal),
          discountAmount: pickNumber(order?.discountAmount, discountTotal),
          totalAmount: pickNumber(order?.totalAmount, grandTotal),
        }
      : order,
  };
};

export const completeOrderPayment = createAsyncThunk(
  "invoice/completePayment",
  async ({ orderId, payload }, { rejectWithValue }) => {
    try {
      const response = await completeOrderPaymentApi(orderId, payload);
      return normalizeInvoiceResponse(response);
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
      return normalizeInvoiceResponse(response);
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
      return normalizeInvoiceResponse(response);
    } catch (error) {
      return rejectWithValue(error.response?.data || { message: "Failed to fetch invoice status" });
    }
  }
);
