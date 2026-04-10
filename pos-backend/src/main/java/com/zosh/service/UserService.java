package com.zosh.service;


import com.zosh.domain.UserRole;
import com.zosh.exception.UserException;
import com.zosh.modal.User;
import com.zosh.payload.request.ChangePasswordRequest;
import com.zosh.payload.request.UpdateProfileRequest;
import com.zosh.payload.response.ProfileResponse;

import java.util.List;
import java.util.Set;
//import com.zosh.payload.request.UpdateUserDto;


public interface UserService {
	User getUserByEmail(String email) throws UserException;
	User getUserFromJwtToken(String jwt) throws UserException;
	User getUserById(Long id) throws UserException;
	Set<User> getUserByRole(UserRole role) throws UserException;
	List<User> getUsers() throws UserException;
	User getCurrentUser();
    ProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);



//	User updateUser(UpdateUserDto updateData, User user);
//	String sendForgotPasswordOtp(String email) throws UserException, MessagingException;
//	User verifyForgotPasswordOtp(String otp, String updatedPassword) throws Exception;
}
