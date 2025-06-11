package com.basariatpos.service;

import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.UserRepository;
import com.basariatpos.util.AppLogger;
import com.basariatpos.util.PasswordHasher;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern; // For basic password strength

public class UserServiceImpl implements UserService {

    private static final Logger logger = AppLogger.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    // Basic password strength regex: at least 8 chars, 1 digit, 1 lower, 1 upper, 1 special char
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public UserServiceImpl(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null.");
        }
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserDTO> authenticate(String username, String password) throws UserException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username and password cannot be empty.",
                                          List.of("Username or password empty.")); // Or more specific errors
        }
        try {
            Optional<UserDTO> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                UserDTO user = userOpt.get();
                if (!user.isActive()) {
                    logger.warn("Authentication failed for user '{}': Account is inactive.", username);
                    return Optional.empty(); // Or throw specific InactiveUserException
                }
                if (PasswordHasher.checkPassword(password, user.getPasswordHash())) {
                    logger.info("User '{}' authenticated successfully.", username);
                    return Optional.of(stripPasswordHash(user)); // Return DTO without hash
                } else {
                    logger.warn("Authentication failed for user '{}': Incorrect password.", username);
                }
            } else {
                logger.warn("Authentication failed: User '{}' not found.", username);
            }
            return Optional.empty();
        } catch (Exception e) { // Catch DataAccessException from repo or other runtime issues
            logger.error("Error during authentication for user '{}': {}", username, e.getMessage(), e);
            throw new UserException("Authentication service error.", e);
        }
    }

    @Override
    public UserDTO createUser(UserDTO userDto, String plainPassword) throws ValidationException, UserAlreadyExistsException, UserException {
        validateUserDto(userDto, true); // true for create (check username)
        validatePassword(plainPassword);

        try {
            if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException(userDto.getUsername());
            }
            String passwordHash = PasswordHasher.hashPassword(plainPassword);
            if (passwordHash == null) {
                throw new UserException("Password hashing failed.");
            }
            UserDTO savedUser = userRepository.save(userDto, passwordHash);
            return stripPasswordHash(savedUser);
        } catch (UserAlreadyExistsException uaee) {
            throw uaee;
        }
        catch (Exception e) {
            logger.error("Error creating user '{}': {}", userDto.getUsername(), e.getMessage(), e);
            throw new UserException("Could not create user.", e);
        }
    }

    @Override
    public void updateUserDetails(UserDTO userDto) throws ValidationException, UserNotFoundException, UserException {
        if (userDto.getUserId() <= 0) {
             throw new ValidationException("User ID is invalid for update.", List.of("Invalid User ID"));
        }
        validateUserDto(userDto, false); // false for update (don't re-check username for existence against itself)

        try {
            userRepository.findById(userDto.getUserId())
                          .orElseThrow(() -> new UserNotFoundException(userDto.getUserId()));
            userRepository.update(userDto);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error updating user ID '{}': {}", userDto.getUserId(), e.getMessage(), e);
            throw new UserException("Could not update user details.", e);
        }
    }

    @Override
    public void changePassword(int userId, String newPlainPassword) throws ValidationException, UserNotFoundException, UserException {
        validatePassword(newPlainPassword);
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            String newPasswordHash = PasswordHasher.hashPassword(newPlainPassword);
            if (newPasswordHash == null) {
                throw new UserException("Password hashing failed during password change.");
            }
            userRepository.updateUserPassword(userId, newPasswordHash);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error changing password for user ID '{}': {}", userId, e.getMessage(), e);
            throw new UserException("Could not change password.", e);
        }
    }

    @Override
    public void grantPermission(int userId, String permissionName) throws UserNotFoundException, PermissionException, UserException {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new PermissionException("Permission name cannot be empty.");
        }
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            userRepository.grantPermission(userId, permissionName);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error granting permission '{}' to user ID '{}': {}", permissionName, userId, e.getMessage(), e);
            throw new UserException("Could not grant permission.", e);
        }
    }

    @Override
    public void revokePermission(int userId, String permissionName) throws UserNotFoundException, PermissionException, UserException {
         if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new PermissionException("Permission name cannot be empty.");
        }
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            userRepository.revokePermission(userId, permissionName);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error revoking permission '{}' from user ID '{}': {}", permissionName, userId, e.getMessage(), e);
            throw new UserException("Could not revoke permission.", e);
        }
    }

    @Override
    public Optional<UserDTO> getUserById(int userId) throws UserException {
        try {
            return userRepository.findById(userId).map(this::stripPasswordHash);
        } catch (Exception e) {
            logger.error("Error getting user by ID '{}': {}", userId, e.getMessage(), e);
            throw new UserException("Could not retrieve user by ID.", e);
        }
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) throws UserException {
        try {
            return userRepository.findByUsername(username).map(this::stripPasswordHash);
        } catch (Exception e) {
            logger.error("Error getting user by username '{}': {}", username, e.getMessage(), e);
            throw new UserException("Could not retrieve user by username.", e);
        }
    }

    @Override
    public List<UserDTO> getAllUsers() throws UserException {
        try {
            return userRepository.findAll().stream()
                                 .map(this::stripPasswordHash)
                                 .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting all users: {}", e.getMessage(), e);
            throw new UserException("Could not retrieve all users.", e);
        }
    }

    @Override
    public void activateUser(int userId) throws UserNotFoundException, UserException {
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            userRepository.setUserActiveStatus(userId, true);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error activating user ID '{}': {}", userId, e.getMessage(), e);
            throw new UserException("Could not activate user.", e);
        }
    }

    @Override
    public void deactivateUser(int userId) throws UserNotFoundException, UserException {
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            userRepository.setUserActiveStatus(userId, false);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error deactivating user ID '{}': {}", userId, e.getMessage(), e);
            throw new UserException("Could not deactivate user.", e);
        }
    }

    @Override
    public List<String> getUserPermissions(int userId) throws UserNotFoundException, UserException {
        try {
             userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            return userRepository.findUserPermissions(userId);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error getting permissions for user ID '{}': {}", userId, e.getMessage(), e);
            throw new UserException("Could not retrieve user permissions.", e);
        }
    }

    @Override
    public boolean doesUserHavePermission(int userId, String permissionName) throws UserNotFoundException, UserException {
        try {
            userRepository.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId));
            return userRepository.hasPermission(userId, permissionName);
        } catch (UserNotFoundException unfe){
            throw unfe;
        }
        catch (Exception e) {
            logger.error("Error checking permission '{}' for user ID '{}': {}", permissionName, userId, e.getMessage(), e);
            throw new UserException("Could not check user permission.", e);
        }
    }

    // --- Helper Methods ---
    private UserDTO stripPasswordHash(UserDTO userDto) {
        if (userDto == null) return null;
        // Create a new DTO or set hash to null to avoid exposing it
        return new UserDTO(userDto.getUserId(), userDto.getUsername(), userDto.getFullName(),
                           userDto.getRole(), userDto.isActive(), userDto.getPermissions(), null /* password hash explicitly null */);
    }

    private void validateUserDto(UserDTO userDto, boolean isCreate) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (userDto == null) {
            errors.add("User data cannot be null.");
            throw new ValidationException(errors);
        }
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            errors.add("Username is required.");
        } else if (userDto.getUsername().trim().length() < 3) {
            errors.add("Username must be at least 3 characters long.");
        }
        if (userDto.getFullName() == null || userDto.getFullName().trim().isEmpty()) {
            errors.add("Full Name is required.");
        }
        if (userDto.getRole() == null || userDto.getRole().trim().isEmpty()) {
            errors.add("User Role is required.");
        } // Add more role validation if roles are predefined enum/list

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validatePassword(String password) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            errors.add("Password is required.");
        } else {
            if (password.length() < 8) {
                 errors.add("Password must be at least 8 characters long.");
            }
            // Example: Using regex for basic complexity check (can be more sophisticated)
            // if (!PASSWORD_PATTERN.matcher(password).matches()) {
            //     errors.add("Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace.");
            // }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
