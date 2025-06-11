package com.basariatpos.service;

import com.basariatpos.model.BankNameDTO;
import java.util.List;
import java.util.Optional;

// Custom Exceptions for BankNameService
class BankNameException extends RuntimeException { // Using RuntimeException for service layer for now
    public BankNameException(String message) { super(message); }
    public BankNameException(String message, Throwable cause) { super(message, cause); }
}

class BankNameNotFoundException extends BankNameException {
    public BankNameNotFoundException(int id) { super("Bank name with ID " + id + " not found.");}
    public BankNameNotFoundException(String name) { super("Bank name with name '" + name + "' not found.");}
}

class BankNameAlreadyExistsException extends BankNameException {
    public BankNameAlreadyExistsException(String nameEn) { super("Bank name with English name '" + nameEn + "' already exists."); }
}
// Reusing ValidationException from UserService.java or define a similar one here if needed.
// For now, assume ValidationException is a general one or redefine if specific fields are needed.
// class ValidationException extends BankNameException { ... }


public interface BankNameService {

    /**
     * Retrieves a bank name by its ID.
     * @param id The ID of the bank name.
     * @return An Optional containing the BankNameDTO if found.
     * @throws BankNameException if a service-level error occurs.
     */
    Optional<BankNameDTO> getBankNameById(int id) throws BankNameException;

    /**
     * Retrieves all bank names.
     * @param includeInactive true to include inactive bank names, false to retrieve only active ones.
     * @return A list of BankNameDTOs.
     * @throws BankNameException if a service-level error occurs.
     */
    List<BankNameDTO> getAllBankNames(boolean includeInactive) throws BankNameException;

    /**
     * Retrieves all active bank names.
     * @return A list of active BankNameDTOs.
     * @throws BankNameException if a service-level error occurs.
     */
    List<BankNameDTO> getActiveBankNames() throws BankNameException;

    /**
     * Saves (creates or updates) a bank name.
     * Validates required fields and checks for uniqueness of English name.
     * @param bankNameDto The DTO containing bank name details.
     * @return The saved BankNameDTO, updated with ID if new.
     * @throws ValidationException if bank name data is invalid.
     * @throws BankNameAlreadyExistsException if the English name already exists (for new entries or if name changed to an existing one).
     * @throws BankNameException if a service-level error occurs.
     */
    BankNameDTO saveBankName(BankNameDTO bankNameDto) throws ValidationException, BankNameAlreadyExistsException, BankNameException;

    /**
     * Toggles the active status of a bank name.
     * If active, it becomes inactive. If inactive, it becomes active.
     * @param id The ID of the bank name to toggle.
     * @throws BankNameNotFoundException if the bank name is not found.
     * @throws BankNameException if a service-level error occurs.
     */
    void toggleBankNameStatus(int id) throws BankNameNotFoundException, BankNameException;
}
