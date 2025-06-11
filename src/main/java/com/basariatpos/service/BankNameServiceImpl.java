package com.basariatpos.service;

import com.basariatpos.model.BankNameDTO;
import com.basariatpos.repository.BankNameRepository;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BankNameServiceImpl implements BankNameService {

    private static final Logger logger = AppLogger.getLogger(BankNameServiceImpl.class);
    private final BankNameRepository bankNameRepository;

    public BankNameServiceImpl(BankNameRepository bankNameRepository) {
        if (bankNameRepository == null) {
            throw new IllegalArgumentException("BankNameRepository cannot be null.");
        }
        this.bankNameRepository = bankNameRepository;
    }

    @Override
    public Optional<BankNameDTO> getBankNameById(int id) throws BankNameException {
        try {
            return bankNameRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error getting bank name by ID {}: {}", id, e.getMessage(), e);
            throw new BankNameException("Could not retrieve bank name by ID.", e);
        }
    }

    @Override
    public List<BankNameDTO> getAllBankNames(boolean includeInactive) throws BankNameException {
        try {
            return bankNameRepository.findAll(includeInactive);
        } catch (Exception e) {
            logger.error("Error getting all bank names (includeInactive={}): {}", includeInactive, e.getMessage(), e);
            throw new BankNameException("Could not retrieve all bank names.", e);
        }
    }

    @Override
    public List<BankNameDTO> getActiveBankNames() throws BankNameException {
        try {
            return bankNameRepository.findAll(false); // false to include only active
        } catch (Exception e) {
            logger.error("Error getting active bank names: {}", e.getMessage(), e);
            throw new BankNameException("Could not retrieve active bank names.", e);
        }
    }

    @Override
    public BankNameDTO saveBankName(BankNameDTO bankNameDto) throws ValidationException, BankNameAlreadyExistsException, BankNameException {
        validateBankNameDto(bankNameDto);

        try {
            // Check for uniqueness of English name (case-insensitive)
            Optional<BankNameDTO> existingByNameEn = bankNameRepository.findByNameEn(bankNameDto.getBankNameEn());
            if (existingByNameEn.isPresent()) {
                // If it's an update, the existing name might belong to the same DTO.
                // If it's a new DTO (id=0) or the ID is different, then it's a conflict.
                if (bankNameDto.getBankNameId() == 0 || bankNameDto.getBankNameId() != existingByNameEn.get().getBankNameId()) {
                    throw new BankNameAlreadyExistsException(bankNameDto.getBankNameEn());
                }
            }
            // Similar check for Arabic name if it also needs to be unique
            Optional<BankNameDTO> existingByNameAr = bankNameRepository.findByNameAr(bankNameDto.getBankNameAr());
            if (existingByNameAr.isPresent()) {
                if (bankNameDto.getBankNameId() == 0 || bankNameDto.getBankNameId() != existingByNameAr.get().getBankNameId()) {
                    // Consider if BankNameAlreadyExistsException should specify which name or be more generic
                     throw new BankNameAlreadyExistsException(bankNameDto.getBankNameAr() + " (Arabic)");
                }
            }

            return bankNameRepository.save(bankNameDto);
        } catch (BankNameAlreadyExistsException e) {
            throw e; // Re-throw specific exception
        }
        catch (Exception e) {
            logger.error("Error saving bank name '{}': {}", bankNameDto.getBankNameEn(), e.getMessage(), e);
            throw new BankNameException("Could not save bank name.", e);
        }
    }

    @Override
    public void toggleBankNameStatus(int id) throws BankNameNotFoundException, BankNameException {
        try {
            BankNameDTO bankName = bankNameRepository.findById(id)
                                       .orElseThrow(() -> new BankNameNotFoundException(id));
            bankNameRepository.setActiveStatus(id, !bankName.isActive());
            logger.info("Toggled active status for bank name ID {}. New status: {}", id, !bankName.isActive());
        } catch (BankNameNotFoundException e) {
            throw e; // Re-throw specific exception
        }
        catch (Exception e) {
            logger.error("Error toggling status for bank name ID {}: {}", id, e.getMessage(), e);
            throw new BankNameException("Could not toggle bank name status.", e);
        }
    }

    private void validateBankNameDto(BankNameDTO bankNameDto) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (bankNameDto == null) {
            errors.add("Bank name data cannot be null.");
            throw new ValidationException("Bank name data is null.", errors); // Reusing ValidationException from UserService for now
        }
        if (bankNameDto.getBankNameEn() == null || bankNameDto.getBankNameEn().trim().isEmpty()) {
            errors.add(MessageProvider.getString("bankname.validation.nameEn.required")); // Assuming MessageProvider is available
        }
        if (bankNameDto.getBankNameAr() == null || bankNameDto.getBankNameAr().trim().isEmpty()) {
            errors.add(MessageProvider.getString("bankname.validation.nameAr.required"));
        }
        // Add more validation rules if needed (e.g., length, format)

        if (!errors.isEmpty()) {
            throw new ValidationException("Bank name validation failed.", errors);
        }
    }
}
