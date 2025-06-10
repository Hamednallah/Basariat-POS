package com.basariatpos.service;

import com.basariatpos.model.UserDTO;
import java.util.Optional;

public interface UserService {
    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username.
     * @param password The password (plain text for mock, should be hashed in real implementation).
     * @return An Optional containing the UserDTO if authentication is successful,
     *         otherwise an empty Optional.
     */
    Optional<UserDTO> authenticate(String username, String password);

    // In Sprint 1 and beyond, more methods would be added here, e.g.:
    // void createUser(UserDTO user, String password);
    // void updateUser(UserDTO user);
    // void changePassword(int userId, String oldPassword, String newPassword);
    // Optional<UserDTO> findUserByUsername(String username);
    // List<UserDTO> getAllUsers();
    // void deactivateUser(int userId);
}
