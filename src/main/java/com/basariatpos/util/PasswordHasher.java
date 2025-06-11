package com.basariatpos.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;

public final class PasswordHasher {

    private static final Logger logger = AppLogger.getLogger(PasswordHasher.class);

    private PasswordHasher() {
        // Private constructor to prevent instantiation of this utility class
    }

    /**
     * Hashes a plain text password using jBCrypt.
     * A new salt is generated for each password.
     *
     * @param plainPassword The password to hash.
     * @return The hashed password string, or null if the input password was null or empty.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.warn("Attempted to hash an empty or null password.");
            return null; // Or throw IllegalArgumentException
        }
        try {
            // gensalt() automatically handles generating a salt with a default log_rounds (work factor)
            // Default work factor is 10. Consider making it configurable if needed.
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            logger.debug("Password hashed successfully.");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error during password hashing: {}", e.getMessage(), e);
            // Depending on policy, could re-throw as a custom unchecked exception
            return null;
        }
    }

    /**
     * Checks a plain text password against a previously hashed one.
     *
     * @param plainPassword  The plain text password to check.
     * @param hashedPassword The stored hashed password.
     * @return true if the password matches the hash, false otherwise.
     *         Returns false if either input is null or empty, or if hashing fails.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() ||
            hashedPassword == null || hashedPassword.isEmpty()) {
            logger.warn("Attempted to check password with empty or null inputs.");
            return false;
        }

        try {
            boolean passwordsMatch = BCrypt.checkpw(plainPassword, hashedPassword);
            if (passwordsMatch) {
                logger.debug("Password check successful: Passwords match.");
            } else {
                logger.debug("Password check failed: Passwords do not match.");
            }
            return passwordsMatch;
        } catch (IllegalArgumentException e) {
            // This can happen if the hashedPassword is not a valid BCrypt hash
            logger.warn("Password check failed due to invalid hash format: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error during password checking: {}", e.getMessage(), e);
            return false;
        }
    }
}
