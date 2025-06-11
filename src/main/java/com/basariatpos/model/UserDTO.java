package com.basariatpos.model;

import java.util.Objects;

/**
 * Data Transfer Object for User information.
 * This class is a simple POJO or can be a Java Record.
 * Using a class for now for broader compatibility and potential mutability if needed later.
 */
import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private int userId; // Made non-final for potential update operations if DTO is reused
    private String username; // Made non-final
    private String fullName; // Made non-final
    private String role;     // Made non-final
    private boolean isActive;
    private List<String> permissions;
    private String passwordHash; // Sensitive: handle with care

    // Default constructor
    public UserDTO() {
        this.permissions = new ArrayList<>();
        this.isActive = true; // Default to active for new users
    }

    // Constructor for essential fields, often used when creating a new user object for saving
    public UserDTO(int userId, String username, String fullName, String role) {
        this(); // Call default constructor to initialize permissions list and isActive
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    // Full constructor including all fields (could be used by repository when fetching from DB)
    public UserDTO(int userId, String username, String fullName, String role, boolean isActive, List<String> permissions, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.permissions = permissions == null ? new ArrayList<>() : new ArrayList<>(permissions);
        this.passwordHash = passwordHash;
    }


    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }
    public List<String> getPermissions() { return permissions; }
    public String getPasswordHash() { return passwordHash; } // Getter for internal use (e.g. service to repo)

    // Setters (optional, depending on DTO mutability strategy; useful for form binding or updates)
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { isActive = active; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions == null ? new ArrayList<>() : new ArrayList<>(permissions); }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }


    @Override
    public String toString() {
        // Be careful not to include passwordHash in typical toString() output
        return "UserDTO{" +
               "userId=" + userId +
               ", username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", role='" + role + '\'' +
               ", isActive=" + isActive +
               ", permissions=" + (permissions == null ? "null" : permissions.size() + " permissions") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return userId == userDTO.userId &&
               isActive == userDTO.isActive &&
               Objects.equals(username, userDTO.username) &&
               Objects.equals(fullName, userDTO.fullName) &&
               Objects.equals(role, userDTO.role) &&
               Objects.equals(permissions, userDTO.permissions) &&
               Objects.equals(passwordHash, userDTO.passwordHash); // passwordHash included for completeness if DTOs are compared directly
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, fullName, role, isActive, permissions, passwordHash);
    }
}
