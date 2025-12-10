package com.zstu.math.service;

import com.zstu.math.entity.User;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    boolean validateUser(String username, String password);
    boolean validateUserByLoginId(String loginId, String password);
    int getLoginAttempts(String username);
    boolean usernameExists(String username);
    boolean nicknameExists(String nickname);
    User addUser(User user);
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByLoginId(String loginId);
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    boolean unlockUser(Long userId);

    User registerUser(User user);
}