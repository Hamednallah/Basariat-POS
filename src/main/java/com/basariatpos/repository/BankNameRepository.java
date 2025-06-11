package com.basariatpos.repository;

import com.basariatpos.model.BankNameDTO;
import java.util.List;
import java.util.Optional;

public interface BankNameRepository {

    /**
     * Finds a bank name by its ID.
     * @param id The ID of the bank name.
     * @return An Optional containing the BankNameDTO if found, otherwise empty.
     */
    Optional<BankNameDTO> findById(int id);

    /**
     * Finds a bank name by its English name (case-insensitive).
     * @param nameEn The English name to search for.
     * @return An Optional containing the BankNameDTO if found, otherwise empty.
     */
    Optional<BankNameDTO> findByNameEn(String nameEn);

    /**
     * Finds a bank name by its Arabic name (case-insensitive for practical DB purposes, exact for others).
     * @param nameAr The Arabic name to search for.
     * @return An Optional containing the BankNameDTO if found, otherwise empty.
     */
    Optional<BankNameDTO> findByNameAr(String nameAr);


    /**
     * Retrieves all bank names.
     * @param includeInactive true to include inactive bank names, false to retrieve only active ones.
     * @return A list of BankNameDTOs.
     */
    List<BankNameDTO> findAll(boolean includeInactive);

    /**
     * Saves (inserts or updates) a bank name.
     * If bankNameDto.getBankNameId() is 0 or less, it's treated as an insert.
     * Otherwise, it's an update based on the ID.
     * @param bankNameDto The BankNameDTO to save.
     * @return The saved BankNameDTO, updated with a generated ID if it was an insert.
     */
    BankNameDTO save(BankNameDTO bankNameDto);

    /**
     * Sets the active status of a bank name.
     * @param id The ID of the bank name.
     * @param isActive true to activate, false to deactivate.
     */
    void setActiveStatus(int id, boolean isActive);

    /**
     * Deletes a bank name by its ID.
     * Note: Consider if soft delete (isActive=false) is preferred over hard delete.
     * This interface assumes hard delete for now. If soft delete, use setActiveStatus.
     * @param id The ID of the bank name to delete.
     * @return true if deletion was successful, false otherwise.
     */
    // boolean deleteById(int id); // Optional: if hard delete is ever needed
}
