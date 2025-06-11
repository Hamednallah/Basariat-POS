package com.basariatpos.service;

import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.UserRepository;
import com.basariatpos.util.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO testUserDto;
    private String plainPassword = "password123";
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = PasswordHasher.hashPassword(plainPassword); // Use real hasher for setup consistency
        assertNotNull(hashedPassword);

        testUserDto = new UserDTO(1, "testuser", "Test User FullName", "Cashier");
        testUserDto.setActive(true);
        testUserDto.setPasswordHash(hashedPassword); // Set the hash in the DTO that repo would return
        testUserDto.setPermissions(new ArrayList<>());
    }

    // --- authenticate ---
    @Test
    void authenticate_validCredentialsAndActiveUser_returnsUserDTOWithoutHash() throws UserException {
        when(mockUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserDto));

        Optional<UserDTO> result = userService.authenticate("testuser", plainPassword);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertNull(result.get().getPasswordHash(), "Password hash should be stripped from DTO returned by authenticate.");
        verify(mockUserRepository).findByUsername("testuser");
    }

    @Test
    void authenticate_userNotFound_returnsEmpty() throws UserException {
        when(mockUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        Optional<UserDTO> result = userService.authenticate("unknown", "password");
        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_incorrectPassword_returnsEmpty() throws UserException {
        when(mockUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserDto));
        Optional<UserDTO> result = userService.authenticate("testuser", "wrongPassword");
        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_inactiveUser_returnsEmpty() throws UserException {
        testUserDto.setActive(false);
        when(mockUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUserDto));
        Optional<UserDTO> result = userService.authenticate("testuser", plainPassword);
        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_emptyUsernameOrPassword_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> userService.authenticate("", "password"));
        assertTrue(ex.getMessage().contains("Username and password cannot be empty"));

        ex = assertThrows(ValidationException.class, () -> userService.authenticate("user", null));
        assertTrue(ex.getMessage().contains("Username and password cannot be empty"));
    }

    // --- createUser ---
    @Test
    void createUser_validData_returnsUserDTOWithoutHash() throws UserException {
        UserDTO newUserInput = new UserDTO(0, "newuser", "New User Full", "Editor");
        UserDTO savedUserOutput = new UserDTO(2, "newuser", "New User Full", "Editor", true, new ArrayList<>(), "somehash");

        when(mockUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        // PasswordHasher.hashPassword is static, will use real one unless mocked with PowerMock or equivalent.
        // For this test, we assume PasswordHasher.hashPassword works as expected.
        when(mockUserRepository.save(any(UserDTO.class), anyString())).thenReturn(savedUserOutput);

        UserDTO result = userService.createUser(newUserInput, "ValidPass123!");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(2, result.getUserId()); // ID from "saved" DTO
        assertNull(result.getPasswordHash(), "Password hash should be stripped.");
        verify(mockUserRepository).save(any(UserDTO.class), anyString());
    }

    @Test
    void createUser_usernameExists_throwsUserAlreadyExistsException() {
        UserDTO existingUser = new UserDTO(1, "existinguser", "Exists", "Admin");
        when(mockUserRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        UserDTO newUserInput = new UserDTO(0, "existinguser", "New User Full", "Editor");
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(newUserInput, "ValidPass123!");
        });
        verify(mockUserRepository, never()).save(any(), any());
    }

    @Test
    void createUser_invalidDto_throwsValidationException() {
        UserDTO invalidUser = new UserDTO(0, "u", "", null); // Invalid username, fullname, role
        assertThrows(ValidationException.class, () -> {
            userService.createUser(invalidUser, "ValidPass123!");
        });
    }

    @Test
    void createUser_invalidPassword_throwsValidationException() {
        UserDTO validUser = new UserDTO(0, "gooduser", "Good Name", "Editor");
        assertThrows(ValidationException.class, () -> {
            userService.createUser(validUser, "short"); // Invalid password
        });
    }

    // --- grantPermission ---
    @Test
    void grantPermission_validUserAndPermission_callsRepository() throws UserException {
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        doNothing().when(mockUserRepository).grantPermission(1, "orders.edit");

        userService.grantPermission(1, "orders.edit");

        verify(mockUserRepository).grantPermission(1, "orders.edit");
    }

    @Test
    void grantPermission_userNotFound_throwsUserNotFoundException() {
        when(mockUserRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userService.grantPermission(99, "orders.edit");
        });
    }

    @Test
    void grantPermission_emptyPermissionName_throwsPermissionException() {
         when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        assertThrows(PermissionException.class, () -> {
            userService.grantPermission(1, "");
        });
    }

    // --- getUserById ---
    @Test
    void getUserById_userExists_returnsUserDTOWithoutHash() throws UserException {
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        Optional<UserDTO> result = userService.getUserById(1);
        assertTrue(result.isPresent());
        assertNull(result.get().getPasswordHash());
    }

    // --- activateUser / deactivateUser ---
    @Test
    void activateUser_userExists_callsRepository() throws UserException {
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        doNothing().when(mockUserRepository).setUserActiveStatus(1, true);
        userService.activateUser(1);
        verify(mockUserRepository).setUserActiveStatus(1, true);
    }

    @Test
    void deactivateUser_userExists_callsRepository() throws UserException {
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        doNothing().when(mockUserRepository).setUserActiveStatus(1, false);
        userService.deactivateUser(1);
        verify(mockUserRepository).setUserActiveStatus(1, false);
    }

    // --- changePassword ---
    @Test
    void changePassword_validInput_updatesPassword() throws UserException {
        String newPassword = "newStrongPassword1!";
        when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        // Capture the hash passed to repository to ensure it's not plain text
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(mockUserRepository).updateUserPassword(eq(1), hashCaptor.capture());

        userService.changePassword(1, newPassword);

        verify(mockUserRepository).updateUserPassword(eq(1), anyString());
        String capturedHash = hashCaptor.getValue();
        assertNotNull(capturedHash);
        assertNotEquals(newPassword, capturedHash);
        assertTrue(PasswordHasher.checkPassword(newPassword, capturedHash));
    }

    @Test
    void changePassword_userNotFound_throwsUserNotFoundException() {
        when(mockUserRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userService.changePassword(99, "newStrongPassword1!");
        });
    }

    @Test
    void changePassword_weakPassword_throwsValidationException() {
         when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUserDto));
        assertThrows(ValidationException.class, () -> {
            userService.changePassword(1, "weak");
        });
    }


    // Add more tests for:
    // updateUserDetails, revokePermission, getUserByUsername, getAllUsers, getUserPermissions, doesUserHavePermission
    // Ensure to cover success cases, failure cases (user not found, validation), and edge cases.
}
