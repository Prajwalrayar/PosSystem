package com.capstone.service;


import com.capstone.exceptions.UserException;
import com.capstone.model.Users;

import java.util.List;

public interface UserService {

    Users getUserFromJwtToken(String token) throws Exception;
    Users getCurrentUser() throws Exception;
    Users getUserByEmail(String email) throws Exception;
    Users getUserById(Long id) throws UserException;

    List<Users> getAllUsers();
}
