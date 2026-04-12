import { createAsyncThunk } from '@reduxjs/toolkit';
import api from '@/utils/api';
import { getBranchSettings as getBranchSettingsApi, saveBranchSettings as saveBranchSettingsApi } from '@/utils/api';

// 🔹 Create Branch
export const createBranch = createAsyncThunk('branch/create', async ({ dto, jwt }, { rejectWithValue }) => {
  try {
    const res = await api.post('/api/branches', dto, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    console.log('Create branch success:', res.data);
    return res.data;
  } catch (err) {
    console.error('Create branch error:', err);
    return rejectWithValue(err.response?.data?.message || 'Create branch failed');
  }
});

// 🔹 Get Branch by ID
export const getBranchById = createAsyncThunk('branch/getById', async ({ id, jwt }, { rejectWithValue }) => {
  try {
    const res = await api.get(`/api/branches/${id}`, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    console.log('Get branch by ID success:', res.data);
    return res.data;
  } catch (err) {
    console.error('Get branch by ID error:', err);
    return rejectWithValue(err.response?.data?.message || 'Branch not found');
  }
});

// 🔹 Get All Branches by Store
export const getAllBranchesByStore = createAsyncThunk('branch/getAllByStore', async ({ storeId, jwt }, { rejectWithValue }) => {
  try {
    const res = await api.get(`/api/branches/store/${storeId}`, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    console.log('Get all branches by store success:', res.data);
    return res.data;
  } catch (err) {
    console.error('Get all branches by store error:', err);
    return rejectWithValue(err.response?.data?.message || 'Failed to fetch branches');
  }
});

// 🔹 Update Branch
export const updateBranch = createAsyncThunk('branch/update', async ({ id, dto, jwt }, { rejectWithValue }) => {
  try {
    const res = await api.put(`/api/branches/${id}`, dto, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    console.log('Update branch success:', res.data);
    return res.data;
  } catch (err) {
    console.error('Update branch error:', err);
    return rejectWithValue(err.response?.data?.message || 'Update failed');
  }
});

// 🔹 Delete Branch
export const deleteBranch = createAsyncThunk('branch/delete', async ({ id, jwt }, { rejectWithValue }) => {
  try {
    await api.delete(`/api/branches/${id}`, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    console.log('Delete branch success:', id);
    return id;
  } catch (err) {
    console.error('Delete branch error:', err);
    return rejectWithValue(err.response?.data?.message || 'Delete failed');
  }
});

export const fetchBranchSettings = createAsyncThunk(
  'branch/fetchSettings',
  async ({ branchId }, { rejectWithValue }) => {
    try {
      const res = await getBranchSettingsApi(branchId);
      const settings = res?.data || res;
      return settings;
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || 'Failed to fetch branch settings';
      return rejectWithValue(errorMsg);
    }
  }
);

export const saveBranchSettings = createAsyncThunk(
  'branch/saveSettings',
  async ({ branchId, payload }, { rejectWithValue, getState }) => {
    try {
      const role = (getState()?.user?.userProfile?.role || '').trim().toUpperCase();
      const canManage = role === 'ROLE_BRANCH_ADMIN' || role === 'ROLE_BRANCH_MANAGER';
      if (!canManage) {
        return rejectWithValue({ message: 'Access denied: cashier cannot update branch settings' });
      }

      const res = await saveBranchSettingsApi(branchId, payload);
      return res?.data || res;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);

