package com.capstone.service.impl;

import com.capstone.configuration.JwtConstant;
import com.capstone.configuration.JwtProvider;
import com.capstone.domain.UserRole;
import com.capstone.exceptions.UserException;
import com.capstone.mapper.UserMapper;
import com.capstone.model.Users;
import com.capstone.payload.dto.UserDto;
import com.capstone.payload.response.AuthResponse;
import com.capstone.repository.UserRepository;
import com.capstone.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserImplementation customUserImplementation;


    @Override
    public AuthResponse SignUp(UserDto userDto) throws UserException {

        Users user = userRepository.findByEmail(userDto.getEmail());
        if (user != null) {
            throw new UserException("Email Already register. Please login! ");
        }
        if(userDto.getRole().equals(UserRole.ROLE_ADMIN)){
            throw new UserException("You are not allowed to register as Admin.");
        }

        Users newUser = new Users();
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRole(userDto.getRole());
        newUser.setFullName(userDto.getFullName());
        newUser.setPhone(userDto.getPhone());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        Users savedUser = userRepository.save(newUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Register Successful");
        authResponse.setUser(UserMapper.toDTO(savedUser));


        return authResponse;
    }

    @Override
    public AuthResponse Login(UserDto userDto) throws Exception {

        String email = userDto.getEmail();
        String password = userDto.getPassword();
        Authentication authentication = authenticate(email,password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String role = authorities.iterator().next().getAuthority();

        String jwt = jwtProvider.generateToken(authentication);

        Users user = userRepository.findByEmail(email);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Login Successful");
        authResponse.setUser(UserMapper.toDTO(user));

        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws Exception {
        UserDetails userDetails =  customUserImplementation.loadUserByUsername(email);

        if(userDetails == null){
            throw new Exception("Email doesn't exist");
        }
        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            throw new Exception("Wrong password! ");
        }
        return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

    }
}
