import { createSlice } from '@reduxjs/toolkit';
import {
  getUserProfile,
  getCustomers,
  getCashiers,
  getAllUsers,
  getUserById,
  logout,
  updateProfile,
  changePassword,
} from './userThunks';

const initialState = {
  userProfile: null,
  users: [],
  customers: [],
  cashiers: [],
  selectedUser: null,
  loading: false,
  error: null,
  profileUpdateStatus: {
    isLoading: false,
    error: null,
    successMessage: null,
  },
  passwordChangeStatus: {
    isLoading: false,
    error: null,
    successMessage: null,
  },
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    clearUserState: (state) => {
      state.userProfile = null;
      state.selectedUser = null;
      state.users = [];
      state.customers = [];
      state.cashiers = [];
      state.error = null;
      state.profileUpdateStatus = { isLoading: false, error: null, successMessage: null };
      state.passwordChangeStatus = { isLoading: false, error: null, successMessage: null };
    },
    clearProfileUpdateState: (state) => {
      state.profileUpdateStatus = { isLoading: false, error: null, successMessage: null };
    },
    clearPasswordChangeState: (state) => {
      state.passwordChangeStatus = { isLoading: false, error: null, successMessage: null };
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getUserProfile.pending, (state) => { state.loading = true; })
      .addCase(getUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.userProfile = action.payload;
      })
      .addCase(getUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      .addCase(getCustomers.fulfilled, (state, action) => {
        state.customers = action.payload;
      })

      .addCase(getCashiers.fulfilled, (state, action) => {
        state.cashiers = action.payload;
      })

      .addCase(getAllUsers.fulfilled, (state, action) => {
        state.users = action.payload;
      })

      .addCase(getUserById.fulfilled, (state, action) => {
        state.selectedUser = action.payload;
      })

      .addCase(updateProfile.pending, (state) => {
        state.profileUpdateStatus.isLoading = true;
        state.profileUpdateStatus.error = null;
        state.profileUpdateStatus.successMessage = null;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.profileUpdateStatus.isLoading = false;
        state.profileUpdateStatus.error = null;
        state.profileUpdateStatus.successMessage = 'Profile updated successfully';
        if (state.userProfile) {
          state.userProfile = {
            ...state.userProfile,
            ...action.payload,
          };
        } else {
          state.userProfile = action.payload;
        }
      })
      .addCase(updateProfile.rejected, (state, action) => {
        state.profileUpdateStatus.isLoading = false;
        state.profileUpdateStatus.error = action.payload;
      })

      .addCase(changePassword.pending, (state) => {
        state.passwordChangeStatus.isLoading = true;
        state.passwordChangeStatus.error = null;
        state.passwordChangeStatus.successMessage = null;
      })
      .addCase(changePassword.fulfilled, (state) => {
        state.passwordChangeStatus.isLoading = false;
        state.passwordChangeStatus.error = null;
        state.passwordChangeStatus.successMessage = 'Password changed successfully';
      })
      .addCase(changePassword.rejected, (state, action) => {
        state.passwordChangeStatus.isLoading = false;
        state.passwordChangeStatus.error = action.payload;
      })

      .addCase(logout.fulfilled, (state) => {
        state.userProfile = null;
        state.selectedUser = null;
        state.error = null;
      })

      .addMatcher(
        (action) => action.type.startsWith('user/') && action.type.endsWith('/rejected'),
        (state, action) => {
          state.error = action.payload;
        }
      );
  },
});

export const { clearUserState, clearProfileUpdateState, clearPasswordChangeState } = userSlice.actions;
export default userSlice.reducer;
