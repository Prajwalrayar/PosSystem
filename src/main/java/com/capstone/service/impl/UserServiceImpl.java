package com.capstone.service.impl;

import com.capstone.configuration.JwtProvider;
import com.capstone.exceptions.UserException;
import com.capstone.model.Users;
import com.capstone.repository.UserRepository;
import com.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;


    @Override
    public Users getUserFromJwtToken(String token) throws Exception {
        String email = jwtProvider.getEmailFromToken(token);
        Users user = userRepository.findByEmail(email);

        if(user == null){
            throw new Exception("Invalid Token");
        }
        return user;
    }

    @Override
    public Users getCurrentUser() throws Exception {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email);
        if(user == null){
            throw new Exception("User not Found");
        }
        return user;
    }

    @Override
    public Users getUserByEmail(String email) throws Exception {
        Users user = userRepository.findByEmail(email);
        if(user == null){
            throw new Exception("User not Found");
        }
        return user;
    }

    @Override
    public Users getUserById(Long id) throws UserException {
        Users user = userRepository.findById(id).orElseThrow(
                ()-> new UserException("User not Found")
        );
        return user;
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
}
