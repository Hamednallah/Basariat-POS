package com.basariatpos.repository;

import com.basariatpos.model.CenterProfileDTO;
import java.util.Optional;

/**
 * Repository interface for managing CenterProfile data.
 */
public interface CenterProfileRepository {

    /**
     * Saves (inserts or updates) the center profile.
     * Since there's only one profile (ID=1), this method handles
     * either creating it if it doesn't exist or updating it if it does.
     *
     * @param profileDto The CenterProfileDTO containing the data to save.
     * @throws RepositoryException if there is an error during the database operation.
     */
    void save(CenterProfileDTO profileDto);

    /**
     * Retrieves the center profile.
     *
     * @return An {@link Optional} containing the {@link CenterProfileDTO} if found,
     *         or an empty Optional if the profile (ID=1) does not exist.
     * @throws RepositoryException if there is an error during the database operation.
     */
    Optional<CenterProfileDTO> getProfile();

    /**
     * Checks if the center profile (ID=1) has been configured.
     *
     * @return true if the profile exists, false otherwise.
     * @throws RepositoryException if there is an error during the database operation.
     */
    boolean exists();
}

// Custom exception for repository layer (optional, can use DataAccessException from jOOQ or Spring)
// For now, let's assume jOOQ's DataAccessException might be propagated or handled.
// class RepositoryException extends RuntimeException {
//    public RepositoryException(String message, Throwable cause) {
//        super(message, cause);
//    }
//    public RepositoryException(String message) {
//        super(message);
//    }
// }
