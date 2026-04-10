import { createAsyncThunk } from '@reduxjs/toolkit';
import { createExport, getExportStatus, downloadExport } from '@/utils/api';

export const createExportJob = createAsyncThunk(
  'exports/createJob',
  async ({ type, format, filters }, { rejectWithValue }) => {
    try {
      const res = await createExport({ type, format, filters });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);

export const pollExportStatus = createAsyncThunk(
  'exports/pollStatus',
  async (exportId, { rejectWithValue }) => {
    try {
      const res = await getExportStatus(exportId);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || { message: 'Request failed' });
    }
  }
);

export const downloadExportFile = createAsyncThunk(
  'exports/downloadFile',
  async (exportId, { rejectWithValue }) => {
    try {
      const response = await downloadExport(exportId);
      const blob = new Blob([response.data]);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const disposition = response.headers['content-disposition'];
      const match = disposition?.match(/filename="?([^"]+)"?/);
      link.download = match?.[1] || 'export.csv';

      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      return { exportId, filename: link.download };
    } catch (err) {
      return rejectWithValue({ message: 'Download failed' });
    }
  }
);
