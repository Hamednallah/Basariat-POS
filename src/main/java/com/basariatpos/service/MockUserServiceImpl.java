package com.basariatpos.service;

import com.basariatpos.model.UserDTO;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Mock implementation of UserService for Sprint 0.
 * This provides hardcoded users for demonstration and testing purposes.
 * A real implementation would interact with a database and use password hashing.
 */
public class MockUserServiceImpl implements UserService {

    private static final Logger logger = AppLogger.getLogger(MockUserServiceImpl.class);

    // Hardcoded user data for mock authentication
    private static final UserDTO ADMIN_USER = new UserDTO(1, "admin", "Default Administrator", "Admin");
    private static final String ADMIN_PASS = "adminpass";

    private static final UserDTO CASHIER_USER = new UserDTO(2, "cashier", "Default Cashier", "Cashier");
    private static final String CASHIER_PASS = "cashierpass";

    @Override
    public Optional<UserDTO> authenticate(String username, String password) {
        if (username == null || password == null) {
            logger.warn("Authentication attempt with null username or password.");
            return Optional.empty();
        }

        logger.info("Attempting authentication for user: {}", username);

        if (ADMIN_USER.getUsername().equals(username)) {
            if (ADMIN_PASS.equals(password)) {
                logger.info("Admin user '{}' authenticated successfully.", username);
                return Optional.of(ADMIN_USER);
            } else {
                logger.warn("Admin user '{}' authentication failed: Incorrect password.", username);
                return Optional.empty();
            }
        } else if (CASHIER_USER.getUsername().equals(username)) {
            if (CASHIER_PASS.equals(password)) {
                logger.info("Cashier user '{}' authenticated successfully.", username);
                return Optional.of(CASHIER_USER);
            } else {
                logger.warn("Cashier user '{}' authentication failed: Incorrect password.", username);
                return Optional.empty();
            }
        }

        logger.warn("User '{}' not found or not mocked for authentication.", username);
        return Optional.empty();
    }
}
