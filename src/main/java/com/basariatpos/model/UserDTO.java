package com.basariatpos.model;

import java.util.Objects;

/**
 * Data Transfer Object for User information.
 * This class is a simple POJO or can be a Java Record.
 * Using a class for now for broader compatibility and potential mutability if needed later.
 */
public class UserDTO {
    private final int userId;
    private final String username;
    private final String fullName;
    private final String role;
    // Add more fields as they are defined in the Users table, e.g., email, lastLogin, isActive, etc.

    public UserDTO(int userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
               "userId=" + userId +
               ", username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", role='" + role + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return userId == userDTO.userId &&
               Objects.equals(username, userDTO.username) &&
               Objects.equals(fullName, userDTO.fullName) &&
               Objects.equals(role, userDTO.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, fullName, role);
    }
}
