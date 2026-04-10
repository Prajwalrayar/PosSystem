package com.zosh.service.impl;


import com.zosh.configrations.JwtProvider;
import com.zosh.domain.UserRole;
import com.zosh.exception.BusinessValidationException;
import com.zosh.exception.UserException;

import com.zosh.payload.request.ChangePasswordRequest;
import com.zosh.payload.request.UpdateProfileRequest;
import com.zosh.payload.response.ProfileResponse;
import com.zosh.repository.PasswordResetTokenRepository;
import com.zosh.repository.UserRepository;

import com.zosh.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.zosh.modal.User;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

//	private final OtpRepository otpRepository;
	private final UserRepository userRepository;
//	private final EmailUtil emailUtil;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final PasswordResetTokenRepository passwordResetTokenRepository;


	@Override
	public User getUserByEmail(String email) throws UserException {
		User user=userRepository.findByEmail(email);
		if(user==null){
			throw new UserException("User not found with email: "+email);
		}
		return user;
	}

	@Override
	public User getUserFromJwtToken(String jwt) throws UserException {
		String email = jwtProvider.getEmailFromJwtToken(jwt);
		User user = userRepository.findByEmail(email);
		if(user==null) throw new UserException("user not exist with email "+email);
		return user;
	}

	@Override
	public User getUserById(Long id) throws UserException {
		return userRepository.findById(id).orElse(null);
	}

	@Override
	public Set<User> getUserByRole(UserRole role) throws UserException {
		return userRepository.findByRole(role);
	}

	@Override
	public User getCurrentUser() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user= userRepository.findByEmail(email);
		if(user == null) {
			throw new EntityNotFoundException("User not found");
		}
		return user;
	}

	@Override
	public List<User> getUsers() throws UserException {
		return userRepository.findAll();
	}

    @Override
    @Transactional
    public ProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(normalizePhone(request.getPhone()));
        User savedUser = userRepository.save(user);

        return new ProfileResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone()
        );
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessValidationException("currentPassword", "Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessValidationException("confirmPassword", "Confirm password must match new password");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BusinessValidationException("newPassword", "New password must be different from current password");
        }
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessValidationException(
                    "newPassword",
                    "New password must be at least 8 characters and include upper, lower, digit, and special character"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setForcePasswordChange(false);
        userRepository.save(user);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.trim();
        return normalized.isEmpty() ? null : normalized;
    }


}
