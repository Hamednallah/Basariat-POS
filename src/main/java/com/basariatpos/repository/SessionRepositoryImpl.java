package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.util.AppLogger;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

public class SessionRepositoryImpl implements SessionRepository {

    private static final Logger logger = AppLogger.getLogger(SessionRepositoryImpl.class);

    public SessionRepositoryImpl() {
        // Default constructor
    }

    @Override
    public void setDatabaseUserContext(Integer userId) {
        DSLContext dslContext = DBManager.getDSLContext();
        if (dslContext == null) {
            logger.error("Cannot set database user context: DSLContext is null. Database might be unavailable.");
            // Depending on application requirements, might throw a custom unchecked exception
            return;
        }

        try {
            // Using jOOQ's plain SQL execution for calling a procedure
            // The first argument to execute is the SQL string, subsequent arguments are bind values.
            // Ensure the procedure 'set_app_user' exists and accepts an INTEGER or NULL.
            if (userId == null) {
                dslContext.execute("CALL set_app_user(NULL::INTEGER)");
                logger.info("Cleared database user context (set_app_user(NULL)).");
            } else {
                dslContext.execute("CALL set_app_user(?)", userId);
                logger.info("Set database user context: set_app_user({})", userId);
            }
        } catch (DataAccessException e) {
            logger.error("Error calling set_app_user procedure for userId: {}", (userId == null ? "NULL" : userId), e);
            // Handle or rethrow as appropriate for the application
        } finally {
            // If DSLContext was obtained from a connection that needs to be closed per request, do it here.
            // However, DBManager.getDSLContext() as implemented might share connections or manage them differently.
            // For the current DBManager that creates a new connection each time getDSLContext() is called,
            // we should close the underlying connection.
            if (dslContext != null && dslContext.configuration().connectionProvider().acquire().isAutoCommit()) { // A bit of a hacky check
                 try {
                    dslContext.close(); // This closes the connection if obtained from DSL.using(connection, ...)
                } catch (Exception e) {
                    logger.error("Error closing DSLContext after setDatabaseUserContext", e);
                }
            }
        }
    }

    @Override
    public void setDatabaseShiftContext(Integer shiftId) {
        DSLContext dslContext = DBManager.getDSLContext();
        if (dslContext == null) {
            logger.error("Cannot set database shift context: DSLContext is null. Database might be unavailable.");
            return;
        }

        try {
            // Ensure the procedure 'set_app_shift' exists and accepts an INTEGER or NULL.
            if (shiftId == null) {
                dslContext.execute("CALL set_app_shift(NULL::INTEGER)");
                logger.info("Cleared database shift context (set_app_shift(NULL)).");
            } else {
                dslContext.execute("CALL set_app_shift(?)", shiftId);
                logger.info("Set database shift context: set_app_shift({})", shiftId);
            }
        } catch (DataAccessException e) {
            logger.error("Error calling set_app_shift procedure for shiftId: {}", (shiftId == null ? "NULL" : shiftId), e);
            // Handle or rethrow
        } finally {
            if (dslContext != null && dslContext.configuration().connectionProvider().acquire().isAutoCommit()) {
                 try {
                    dslContext.close();
                } catch (Exception e) {
                    logger.error("Error closing DSLContext after setDatabaseShiftContext", e);
                }
            }
        }
    }
}
