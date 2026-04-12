import { createAsyncThunk } from "@reduxjs/toolkit";
import api from "@/utils/api";
import { saveStoreSettings as saveStoreSettingsApi, getStoreSettings as getStoreSettingsApi } from "@/utils/api";

const buildStoreDtoPayload = (storeData = {}) => {
  const payload = {
    brand: storeData.brand,
    storeType: storeData.storeType,
    description: storeData.description ?? null,
    commissionRate: storeData.commissionRate ?? 0,
  };

  const contact = storeData.contact;
  if (contact && (contact.address || contact.phone || contact.email)) {
    payload.contact = {
      address: contact.address ?? "",
      phone: contact.phone ?? "",
      email: contact.email ?? "",
    };
  }

  return payload;
};

// Helper function to get JWT token
const getAuthToken = () => {
  const token = localStorage.getItem('jwt');
  if (!token) {
    throw new Error('No JWT token found');
  }
  return token;
};

// Helper function to set auth headers
const getAuthHeaders = () => {
  const token = getAuthToken();
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
};

// 🔹 Create store
export const createStore = createAsyncThunk(
  "store/create",
  async (storeData, { rejectWithValue }) => {
    try {
      const payload = buildStoreDtoPayload(storeData);
      console.log('🔄 Creating store...', { storeData: payload });
      
      const headers = getAuthHeaders();
      const res = await api.post("/api/stores", payload, { headers });
      
      console.log('✅ Store created successfully:', {
        storeId: res.data.id,
        brand: res.data.brand,
        storeType: res.data.storeType,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to create store:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        requestData: storeData
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to create store"
      );
    }
  }
);

// 🔹 Get store by ID
export const getStoreById = createAsyncThunk(
  "store/getById",
  async (id, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching store by ID...', { storeId: id });
      
      const headers = getAuthHeaders();
      const res = await api.get(`/api/stores/${id}`, { headers });
      
      console.log('✅ Store fetched successfully:', {
        storeId: res.data.id,
        brand: res.data.brand,
        storeType: res.data.storeType,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to fetch store by ID:', {
        storeId: id,
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Store not found"
      );
    }
  }
);

// 🔹 Get all stores
export const getAllStores = createAsyncThunk(
  "store/getAll",
  async (status, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching all stores...');
      
      const headers = getAuthHeaders();
      const res = await api.get("/api/stores", { headers,
        params: status ? { status } : undefined,
       });
      
      console.log('✅ All stores fetched successfully:', {
        storeCount: res.data.length,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to fetch all stores:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to fetch stores"
      );
    }
  }
);

// 🔹 Update store
export const updateStore = createAsyncThunk(
  "store/update",
  async ({ id, storeData }, { rejectWithValue }) => {
    try {
      const payload = buildStoreDtoPayload(storeData);
      console.log('🔄 Updating store...', { storeId: id, storeData: payload });
      
      const headers = getAuthHeaders();
      const res = await api.put(`/api/stores/${id}`, payload, { headers });
      
      console.log('✅ Store updated successfully:', {
        storeId: res.data.id,
        brand: res.data.brand,
        storeType: res.data.storeType,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to update store:', {
        storeId: id,
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        requestData: storeData
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to update store"
      );
    }
  }
);

// 🔹 Delete store
export const deleteStore = createAsyncThunk(
  "store/delete",
  async (_, { rejectWithValue }) => {
    try {
      console.log('🔄 Deleting store...');
      
      const headers = getAuthHeaders();
      const res = await api.delete("/api/stores", { headers });
      
      console.log('✅ Store deleted successfully:', { response: res.data });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to delete store:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to delete store"
      );
    }
  }
);

// 🔹 Get store by admin
export const getStoreByAdmin = createAsyncThunk(
  "store/getByAdmin",
  async (_, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching store by admin...');
      
      const headers = getAuthHeaders();
      const res = await api.get("/api/stores/admin", { headers });
      
      console.log('✅ Store fetched by admin successfully:', {
        storeId: res.data.id,
        brand: res.data.brand,
        storeType: res.data.storeType,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to fetch store by admin:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Not authorized"
      );
    }
  }
);

// 🔹 Get store by employee
export const getStoreByEmployee = createAsyncThunk(
  "store/getByEmployee",
  async (_, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching store by employee...');
      
      const headers = getAuthHeaders();
      const res = await api.get("/api/stores/employee", { headers });
      
      console.log('✅ Store fetched by employee successfully:', {
        storeId: res.data.id,
        brand: res.data.brand,
        storeType: res.data.storeType,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to fetch store by employee:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Not authorized"
      );
    }
  }
);

// 🔹 Get employee list by store
export const getStoreEmployees = createAsyncThunk(
  "store/getEmployees",
  async (storeId, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching store employees...', { storeId });
      
      const headers = getAuthHeaders();
      const res = await api.get(`/api/stores/${storeId}/employee/list`, { headers });
      
      console.log('✅ Store employees fetched successfully:', {
        storeId,
        employeeCount: res.data.length,
        employees: res.data.map(employee => ({
          id: employee.id,
          name: employee.name,
          role: employee.role
        }))
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to fetch store employees:', {
        storeId,
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to get employees"
      );
    }
  }
);

// 🔹 Add employee
export const addEmployee = createAsyncThunk(
  "store/addEmployee",
  async (employeeData, { rejectWithValue }) => {
    try {
      console.log('🔄 Adding employee to store...', { employeeData });
      
      const headers = getAuthHeaders();
      const res = await api.post("/api/stores/add/employee", employeeData, { headers });
      
      console.log('✅ Employee added successfully:', {
        employeeId: res.data.id,
        name: res.data.name,
        role: res.data.role,
        response: res.data
      });
      
      return res.data;
    } catch (err) {
      console.error('❌ Failed to add employee:', {
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        requestData: employeeData
      });
      
      return rejectWithValue(
        err.response?.data?.message || "Failed to add employee"
      );
    }
  }
);

// 🔹 Moderate store (approve/block/reject)
export const moderateStore = createAsyncThunk(
  "store/moderateStore",
  async ({ storeId, action }, { rejectWithValue }) => {
    try {
      console.log('🔄 Moderating store...', { storeId, action });
      const headers = getAuthHeaders();
      const res = await api.put(`/api/stores/${storeId}/moderate`, null, {
        headers,
        params: { action },
      });
      console.log('✅ Store moderated successfully:', {
        storeId: res.data.id,
        status: res.data.status,
        response: res.data
      });
      return res.data;
    } catch (err) {
      console.error('❌ Failed to moderate store:', {
        storeId,
        action,
        error: err.response?.data || err.message,
        status: err.response?.status,
        statusText: err.response?.statusText
      });
      return rejectWithValue(
        err.response?.data?.message || 'Failed to moderate store'
      );
    }
  }
);

export const saveStoreSettings = createAsyncThunk(
  'store/saveSettings',
  async ({ storeId, payload }, { rejectWithValue }) => {
    try {
      const res = await saveStoreSettingsApi(storeId, payload);
      // API function already returns response.data, so just return it as-is
      return res;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);

export const getStoreSettings = createAsyncThunk(
  'store/getSettings',
  async (storeId, { rejectWithValue }) => {
    try {
      console.log('🔄 Fetching store settings...', { storeId });
      const res = await getStoreSettingsApi(storeId);
      console.log('✅ Store settings fetched:', res);
      // Return response as-is (will have null sections if no record exists)
      return res || {
        storeSettings: null,
        notificationSettings: null,
        securitySettings: null,
        paymentSettings: null,
      };
    } catch (err) {
      console.error('❌ Failed to fetch store settings:', {
        storeId,
        status: err.response?.status,
        error: err.response?.data || err.message,
      });
      return rejectWithValue({
        message: err.response?.data?.message || 'Failed to fetch settings',
        status: err.response?.status,
        details: err.response?.data,
      });
    }
  }
);
