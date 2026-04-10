import { createSlice } from '@reduxjs/toolkit';
import { createExportJob, pollExportStatus, downloadExportFile } from './exportsThunks';

const initialState = {
  currentJob: null,
  history: [],
  creating: false,
  polling: false,
  downloading: false,
  error: null,
};

const exportsSlice = createSlice({
  name: 'exports',
  initialState,
  reducers: {
    clearExportError: (state) => {
      state.error = null;
    },
    clearCurrentExportJob: (state) => {
      state.currentJob = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createExportJob.pending, (state) => {
        state.creating = true;
        state.error = null;
      })
      .addCase(createExportJob.fulfilled, (state, action) => {
        state.creating = false;
        state.currentJob = action.payload;
      })
      .addCase(createExportJob.rejected, (state, action) => {
        state.creating = false;
        state.error = action.payload;
      })

      .addCase(pollExportStatus.pending, (state) => {
        state.polling = true;
      })
      .addCase(pollExportStatus.fulfilled, (state, action) => {
        state.polling = false;
        state.currentJob = action.payload;
      })
      .addCase(pollExportStatus.rejected, (state, action) => {
        state.polling = false;
        state.error = action.payload;
      })

      .addCase(downloadExportFile.pending, (state) => {
        state.downloading = true;
      })
      .addCase(downloadExportFile.fulfilled, (state, action) => {
        state.downloading = false;
        state.history.unshift(action.payload);
      })
      .addCase(downloadExportFile.rejected, (state, action) => {
        state.downloading = false;
        state.error = action.payload;
      });
  },
});

export const { clearExportError, clearCurrentExportJob } = exportsSlice.actions;
export default exportsSlice.reducer;
