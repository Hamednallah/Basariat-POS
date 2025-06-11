package com.basariatpos.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashPassword_shouldReturnNonNullAndNonEmptyHash_forValidPassword() {
        String plainPassword = "testPassword123!";
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        assertNotNull(hashedPassword, "Hashed password should not be null.");
        assertFalse(hashedPassword.isEmpty(), "Hashed password should not be empty.");
        assertNotEquals(plainPassword, hashedPassword, "Hashed password should not be the same as plain password.");
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$"),
                   "Hashed password should start with a valid jBCrypt prefix.");
    }

    @Test
    void hashPassword_shouldReturnNull_forNullInput() {
        assertNull(PasswordHasher.hashPassword(null), "Hashing null password should return null.");
    }

    @Test
    void hashPassword_shouldReturnNull_forEmptyInput() {
        assertNull(PasswordHasher.hashPassword(""), "Hashing empty password should return null.");
    }

    @Test
    void checkPassword_shouldReturnTrue_forCorrectPassword() {
        String plainPassword = "mySecurePassword@2024";
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        assertNotNull(hashedPassword); // Ensure hashing worked

        assertTrue(PasswordHasher.checkPassword(plainPassword, hashedPassword),
                   "checkPassword should return true for the correct plain password.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forIncorrectPassword() {
        String plainPassword = "mySecurePassword@2024";
        String incorrectPassword = "WrongPassword123";
        String hashedPassword = PasswordHasher.hashPassword(plainPassword);
        assertNotNull(hashedPassword);

        assertFalse(PasswordHasher.checkPassword(incorrectPassword, hashedPassword),
                    "checkPassword should return false for an incorrect plain password.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forNullPlainPassword() {
        String hashedPassword = PasswordHasher.hashPassword("somePassword");
        assertNotNull(hashedPassword);
        assertFalse(PasswordHasher.checkPassword(null, hashedPassword),
                    "checkPassword should return false if plain password is null.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forEmptyPlainPassword() {
        String hashedPassword = PasswordHasher.hashPassword("somePassword");
        assertNotNull(hashedPassword);
        assertFalse(PasswordHasher.checkPassword("", hashedPassword),
                    "checkPassword should return false if plain password is empty.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forNullHashedPassword() {
        assertFalse(PasswordHasher.checkPassword("somePassword", null),
                    "checkPassword should return false if hashed password is null.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forEmptyHashedPassword() {
        assertFalse(PasswordHasher.checkPassword("somePassword", ""),
                    "checkPassword should return false if hashed password is empty.");
    }

    @Test
    void checkPassword_shouldReturnFalse_forInvalidHashedPasswordFormat() {
        String plainPassword = "testPassword";
        String invalidHash = "not-a-bcrypt-hash";
        assertFalse(PasswordHasher.checkPassword(plainPassword, invalidHash),
                    "checkPassword should return false for an invalid hash format.");
    }

    @Test
    void hashPassword_generatesDifferentHashes_forSamePasswordDueToSalt() {
        String plainPassword = "commonPassword";
        String hash1 = PasswordHasher.hashPassword(plainPassword);
        String hash2 = PasswordHasher.hashPassword(plainPassword);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2, "Two hashes of the same password should be different due to salting.");
        assertTrue(PasswordHasher.checkPassword(plainPassword, hash1));
        assertTrue(PasswordHasher.checkPassword(plainPassword, hash2));
    }
}
