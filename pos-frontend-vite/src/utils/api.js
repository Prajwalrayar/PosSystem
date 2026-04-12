import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const getBearerToken = () =>
  sessionStorage.getItem('jwt') ||
  sessionStorage.getItem('token') ||
  localStorage.getItem('jwt') ||
  localStorage.getItem('token');

const getAuthConfig = () => {
  const token = getBearerToken();
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

export const updateProfile = ({ fullName, phone }) => {
  return api.patch('/api/users/me/profile', { fullName, phone }, getAuthConfig()).then((response) => response.data);
};

export const changePassword = ({ currentPassword, newPassword, confirmPassword }) => {
  return api
    .patch('/api/auth/change-password', { currentPassword, newPassword, confirmPassword }, getAuthConfig())
    .then((response) => response.data);
};

export const resetEmployeePassword = (employeeId, { temporaryPassword, forceChangeOnNextLogin }) => {
  return api
    .patch(
      `/api/employees/${employeeId}/reset-password`,
      { temporaryPassword, forceChangeOnNextLogin },
      getAuthConfig()
    )
    .then((response) => response.data);
};

export const addCustomerPoints = (customerId, { type, points, reason, note }) => {
  return api
    .post(`/api/customers/${customerId}/loyalty/transactions`, { type, points, reason, note }, getAuthConfig())
    .then((response) => response.data);
};

export const getCommissions = () => {
  return api.get('/api/super-admin/commissions', getAuthConfig()).then((response) => response.data);
};

export const updateCommission = (storeId, { rate }) => {
  return api.patch(`/api/super-admin/commissions/${storeId}`, { rate }, getAuthConfig()).then((response) => response.data);
};

export const getBranchSettings = (branchId) => {
  return api.get(`/api/branches/${branchId}/settings`, getAuthConfig()).then((response) => response.data);
};

export const saveBranchSettings = (branchId, payload) => {
  return api.patch(`/api/branches/${branchId}/settings`, payload, getAuthConfig()).then((response) => response.data);
};

export const saveStoreSettings = (storeId, payload) => {
  return api.patch(`/api/stores/${storeId}/settings`, payload, getAuthConfig()).then((response) => response.data);
};

export const getStoreSettings = (storeId) => {
  return api.get(`/api/stores/${storeId}/settings`, getAuthConfig()).then((response) => response.data);
};

export const completeOrderPayment = (orderId, payload) => {
  return api
    .post(`/api/orders/${orderId}/complete-payment`, payload, getAuthConfig())
    .then((response) => response.data);
};

export const sendInvoiceEmail = (invoiceId, payload = {}) => {
  return api
    .post(`/api/invoices/${invoiceId}/send-email`, payload, getAuthConfig())
    .then((response) => response.data);
};

export const getInvoiceStatus = (invoiceId) => {
  return api.get(`/api/invoices/${invoiceId}/status`, getAuthConfig()).then((response) => response.data);
};

export const getInvoicePdf = (invoiceId) => {
  return api.get(`/api/invoices/${invoiceId}/pdf`, {
    ...getAuthConfig(),
    responseType: 'blob',
  });
};

export const createExport = (payload) => {
  return api.post('/api/exports', payload, getAuthConfig()).then((response) => response.data);
};

export const getExportStatus = (exportId) => {
  return api.get(`/api/exports/${exportId}`, getAuthConfig()).then((response) => response.data);
};

export const downloadExport = (exportId) => {
  return api.get(`/api/exports/${exportId}/download`, {
    ...getAuthConfig(),
    responseType: 'blob',
  });
};

export const createPrintJob = (payload) => {
  return api.post('/api/print/jobs', payload, getAuthConfig()).then((response) => response.data);
};

export const initiateReturn = (payload) => {
  return api.post('/api/returns/initiate', payload, getAuthConfig()).then((response) => response.data);
};

export const getEmployeePerformance = (employeeId, { from, to }) => {
  return api
    .get(`/api/employees/${employeeId}/performance`, {
      ...getAuthConfig(),
      params: { from, to },
    })
    .then((response) => response.data);
};
export default api;
