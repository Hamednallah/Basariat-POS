package com.basariatpos.repository;

import com.basariatpos.model.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    /**
     * Finds a user by their username.
     * The returned UserDTO includes the password hash for authentication purposes within the service layer.
     *
     * @param username The username to search for.
     * @return An Optional containing the UserDTO if found (including password hash), otherwise empty.
     */
    Optional<UserDTO> findByUsername(String username);

    /**
     * Finds a user by their ID.
     * The returned UserDTO includes the password hash.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the UserDTO if found, otherwise empty.
     */
    Optional<UserDTO> findById(int userId);

    /**
     * Saves a new user to the database.
     *
     * @param userDto      The UserDTO containing user details (excluding ID, which should be auto-generated or handled by DB).
     * @param passwordHash The hashed password for the new user.
     * @return The saved UserDTO, possibly updated with a generated ID.
     */
    UserDTO save(UserDTO userDto, String passwordHash);

    /**
     * Updates an existing user's details (e.g., full name, role, active status).
     * This method should NOT update the password.
     *
     * @param userDto The UserDTO containing updated information. The userId field must be set.
     * @return The updated UserDTO.
     */
    UserDTO update(UserDTO userDto);

    /**
     * Updates a user's password hash.
     *
     * @param userId          The ID of the user whose password is to be updated.
     * @param newPasswordHash The new hashed password.
     */
    void updateUserPassword(int userId, String newPasswordHash);

    /**
     * Retrieves all users from the database.
     * UserDTOs in the list should generally NOT include the password hash for security reasons if exposed widely,
     * but for internal service use, it might be included if necessary (though typically not for lists).
     * For this definition, assume passwordHash is excluded or null for list views.
     *
     * @return A list of all UserDTOs.
     */
    List<UserDTO> findAll();

    /**
     * Sets the active status of a user.
     *
     * @param userId   The ID of the user.
     * @param isActive true to activate the user, false to deactivate.
     */
    void setUserActiveStatus(int userId, boolean isActive);

    /**
     * Grants a permission to a user.
     * If the permission already exists for the user, it ensures it's marked as granted.
     *
     * @param userId         The ID of the user.
     * @param permissionName The name of the permission to grant (e.g., "manage_settings", "process_sales").
     */
    void grantPermission(int userId, String permissionName);

    /**
     * Revokes a permission from a user by marking it as not granted.
     * It does not delete the permission entry, allowing for re-granting.
     *
     * @param userId         The ID of the user.
     * @param permissionName The name of the permission to revoke.
     */
    void revokePermission(int userId, String permissionName);

    /**
     * Deletes a permission record for a user. (Alternative to revoke if hard delete is desired)
     * @param userId User ID
     * @param permissionName Permission Name
     */
    // void deletePermission(int userId, String permissionName);


    /**
     * Retrieves all currently granted permissions for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of permission name strings.
     */
    List<String> findUserPermissions(int userId);

    /**
     * Checks if a user has a specific permission and it is currently granted.
     *
     * @param userId         The ID of the user.
     * @param permissionName The name of the permission to check.
     * @return true if the user has the permission and it's granted, false otherwise.
     */
    boolean hasPermission(int userId, String permissionName);
}
