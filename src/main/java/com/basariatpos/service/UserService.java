package com.basariatpos.service;

import com.basariatpos.model.UserDTO;
import java.util.List;
import java.util.Optional;

// Custom Exceptions for UserService
class UserException extends Exception {
    public UserException(String message) { super(message); }
    public UserException(String message, Throwable cause) { super(message, cause); }
}
class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) { super(message); }
    public UserNotFoundException(int userId) { super("User with ID " + userId + " not found.");}
    public UserNotFoundException(String username) { super("User with username '" + username + "' not found.");}
}
class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String username) { super("User with username '" + username + "' already exists."); }
}
class ValidationException extends UserException {
    private List<String> errors;
    public ValidationException(String message, List<String> errors) { super(message); this.errors = errors; }
    public ValidationException(List<String> errors) { super("User data validation failed."); this.errors = errors; }
    public List<String> getErrors() { return errors; }
}
class AuthenticationException extends UserException {
    public AuthenticationException(String message) { super(message); }
}
class PermissionException extends UserException {
    public PermissionException(String message) { super(message); }
}


public interface UserService {
    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username.
     * @param password The password (plain text).
     * @return An Optional containing the UserDTO (without password hash) if authentication is successful,
     *         otherwise an empty Optional.
     * @throws UserException if a service-level error occurs during authentication.
     */
    Optional<UserDTO> authenticate(String username, String password) throws UserException;

    /**
     * Creates a new user.
     * @param userDto The DTO containing user details (username, fullName, role).
     * @param plainPassword The plain text password for the new user.
     * @return The created UserDTO, updated with generated ID.
     * @throws ValidationException if user data is invalid.
     * @throws UserAlreadyExistsException if the username already exists.
     * @throws UserException if a service-level error occurs.
     */
    UserDTO createUser(UserDTO userDto, String plainPassword) throws ValidationException, UserAlreadyExistsException, UserException;

    /**
     * Updates details of an existing user (e.g., full name, role). Does not update password or username.
     * @param userDto The DTO containing user details to update. userId must be set.
     * @throws ValidationException if user data is invalid.
     * @throws UserNotFoundException if the user to update is not found.
     * @throws UserException if a service-level error occurs.
     */
    void updateUserDetails(UserDTO userDto) throws ValidationException, UserNotFoundException, UserException;

    /**
     * Changes the password for a specified user.
     * @param userId The ID of the user whose password is to be changed.
     * @param newPlainPassword The new plain text password.
     * @throws ValidationException if the new password is invalid (e.g., too weak).
     * @throws UserNotFoundException if the user is not found.
     * @throws UserException if a service-level error occurs.
     */
    void changePassword(int userId, String newPlainPassword) throws ValidationException, UserNotFoundException, UserException;

    /**
     * Grants a permission to a user.
     * @param userId The ID of the user.
     * @param permissionName The name of the permission to grant.
     * @throws UserNotFoundException if the user is not found.
     * @throws PermissionException if the permission name is invalid or cannot be granted.
     * @throws UserException if a service-level error occurs.
     */
    void grantPermission(int userId, String permissionName) throws UserNotFoundException, PermissionException, UserException;

    /**
     * Revokes a permission from a user.
     * @param userId The ID of the user.
     * @param permissionName The name of the permission to revoke.
     * @throws UserNotFoundException if the user is not found.
     * @throws PermissionException if the permission name is invalid or cannot be revoked.
     * @throws UserException if a service-level error occurs.
     */
    void revokePermission(int userId, String permissionName) throws UserNotFoundException, PermissionException, UserException;

    /**
     * Retrieves a user by their ID.
     * @param userId The ID of the user.
     * @return An Optional containing the UserDTO (without password hash) if found.
     * @throws UserNotFoundException if the user is not found. (Alternative: return Optional.empty())
     * @throws UserException if a service-level error occurs.
     */
    Optional<UserDTO> getUserById(int userId) throws UserException;

    /**
     * Retrieves a user by their username.
     * @param username The username of the user.
     * @return An Optional containing the UserDTO (without password hash) if found.
     * @throws UserException if a service-level error occurs.
     */
    Optional<UserDTO> getUserByUsername(String username) throws UserException;


    /**
     * Retrieves all users.
     * @return A list of UserDTOs (without password hashes).
     * @throws UserException if a service-level error occurs.
     */
    List<UserDTO> getAllUsers() throws UserException;

    /**
     * Activates a user account.
     * @param userId The ID of the user to activate.
     * @throws UserNotFoundException if the user is not found.
     * @throws UserException if a service-level error occurs.
     */
    void activateUser(int userId) throws UserNotFoundException, UserException;

    /**
     * Deactivates a user account.
     * @param userId The ID of the user to deactivate.
     * @throws UserNotFoundException if the user is not found.
     * @throws UserException if a service-level error occurs.
     */
    void deactivateUser(int userId) throws UserNotFoundException, UserException;

    /**
     * Retrieves all granted permissions for a user.
     * @param userId The ID of the user.
     * @return A list of permission strings.
     * @throws UserNotFoundException if the user is not found.
     * @throws UserException if a service-level error occurs.
     */
    List<String> getUserPermissions(int userId) throws UserNotFoundException, UserException;

    /**
     * Checks if a user has a specific permission.
     * @param userId The ID of the user.
     * @param permissionName The name of the permission.
     * @return true if the user has the permission, false otherwise.
     * @throws UserNotFoundException if the user is not found.
     * @throws UserException if a service-level error occurs.
     */
    boolean doesUserHavePermission(int userId, String permissionName) throws UserNotFoundException, UserException;
}
