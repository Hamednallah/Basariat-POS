package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.routines.Startshift;
import com.basariatpos.db.generated.routines.Pauseshift;
import com.basariatpos.db.generated.routines.Resumeshift;
// import com.basariatpos.db.generated.routines.Endshift; // For later
import com.basariatpos.db.generated.tables.records.ShiftsRecord;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.SHIFTS;
import static com.basariatpos.db.generated.Tables.USERS; // For joining to get username

public class ShiftRepositoryImpl implements ShiftRepository {

    private static final Logger logger = AppLogger.getLogger(ShiftRepositoryImpl.class);

    @Override
    public Optional<ShiftDTO> findById(int shiftId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            // Join with USERS table to get the username
            return Optional.ofNullable(
                dsl.select(
                        SHIFTS.asterisk(), // Select all columns from SHIFTS
                        USERS.USERNAME // And the username from USERS
                   )
                   .from(SHIFTS)
                   .join(USERS).on(SHIFTS.STARTED_BY_USER_ID.eq(USERS.USER_ID))
                   .where(SHIFTS.SHIFT_ID.eq(shiftId))
                   .fetchOne(this::mapRecordToDtoWithUsername) // Use a mapper that handles the joined record
            );
        } catch (DataAccessException e) {
            logger.error("Error finding shift by ID {}: {}", shiftId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ShiftDTO> findActiveOrPausedShiftByUserId(int userId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return Optional.ofNullable(
                dsl.select(SHIFTS.asterisk(), USERS.USERNAME)
                   .from(SHIFTS)
                   .join(USERS).on(SHIFTS.STARTED_BY_USER_ID.eq(USERS.USER_ID))
                   .where(SHIFTS.STARTED_BY_USER_ID.eq(userId)
                         .and(SHIFTS.STATUS.in("Active", "Paused")))
                   .orderBy(SHIFTS.START_TIME.desc()) // Get the most recent one if multiple (should not happen)
                   .limit(1)
                   .fetchOne(this::mapRecordToDtoWithUsername)
            );
        } catch (DataAccessException e) {
            logger.error("Error finding active/paused shift for user ID {}: {}", userId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public int startShift(int userId, BigDecimal openingFloat) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Startshift startShiftRoutine = new Startshift();
            startShiftRoutine.setPUserId(userId);
            startShiftRoutine.setPOpeningFloat(openingFloat);

            startShiftRoutine.execute(dsl.configuration()); // Execute the procedure

            Integer newShiftId = startShiftRoutine.getReturnValue(); // Get the output shift_id
            if (newShiftId == null) {
                // This might happen if the procedure doesn't return a value as expected
                // or if it's designed to throw an exception handled by PostgreSQL.
                // Check procedure definition for error handling (e.g. raising exceptions in PL/pgSQL)
                logger.error("StartShift procedure did not return a shift ID for user ID {}.", userId);
                throw new DataAccessException("Failed to start shift: procedure did not return new shift ID.");
            }
            logger.info("Shift started for user ID {} with opening float {}. New Shift ID: {}", userId, openingFloat, newShiftId);
            return newShiftId;
        } catch (DataAccessException e) {
            // This will catch SQL exceptions raised by PostgreSQL if procedure fails (e.g. user already has active shift)
            logger.error("Error starting shift for user ID {}: {}", userId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void pauseShift(int shiftId, int userId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Pauseshift pauseShiftRoutine = new Pauseshift();
            pauseShiftRoutine.setPShiftId(shiftId);
            pauseShiftRoutine.setPUserId(userId); // For audit/validation within procedure

            pauseShiftRoutine.execute(dsl.configuration());
            logger.info("Shift ID {} paused by user ID {}.", shiftId, userId);
        } catch (DataAccessException e) {
            logger.error("Error pausing shift ID {}: {}", shiftId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void resumeShift(int shiftId, int userId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Resumeshift resumeShiftRoutine = new Resumeshift();
            resumeShiftRoutine.setPShiftId(shiftId);
            resumeShiftRoutine.setPUserId(userId); // For audit/validation

            resumeShiftRoutine.execute(dsl.configuration());
            logger.info("Shift ID {} resumed by user ID {}.", shiftId, userId);
        } catch (DataAccessException e) {
            logger.error("Error resuming shift ID {}: {}", shiftId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    // Mapper that handles the joined Record (ShiftsRecord + username)
    private ShiftDTO mapRecordToDtoWithUsername(org.jooq.Record record) {
        if (record == null) return null;

        ShiftsRecord shiftsRecord = record.into(SHIFTS); // Extract ShiftsRecord part
        String username = record.get(USERS.USERNAME); // Get username from the joined part

        return new ShiftDTO(
            shiftsRecord.getShiftId(),
            shiftsRecord.getStartedByUserId(),
            username, // Username from Users table
            shiftsRecord.getStartTime(),
            shiftsRecord.getEndTime(),
            shiftsRecord.getStatus(),
            shiftsRecord.getOpeningFloat()
        );
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                dslContext.close();
            } catch (Exception e) {
                logger.warn("Failed to close DSLContext.", e);
            }
        }
    }
}
