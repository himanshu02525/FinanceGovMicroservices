package com.finance.service;

import java.util.List;

import com.finance.dto.UserCreationRequest;
import com.finance.dto.UserResponseDto;
import com.finance.model.User;

public interface UserService {

    // ========== INTERNAL (ENTITY) ==========
    User findUserById(Long id);
    User getUserByEmail(String email);
    User getUserByUserName(String username);
    List<User> findAllUsers();

    // ========== API SAFE (DTO) ==========
    UserResponseDto getUserById(Long id);
    List<UserResponseDto> getAllUserDtos();

    // ========== ACTIONS ==========
    String createOfficer(UserCreationRequest request);
    User updateUser(Long id, User updatedUser);
    User updateUserStatus(Long id, String status);
    void deleteUser(Long id);
    void deleteOfficer(Long id);
}
