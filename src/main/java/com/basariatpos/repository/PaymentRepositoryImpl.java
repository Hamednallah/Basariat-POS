package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.routines.Recordpaymentandupdatesalesorder;
import com.basariatpos.db.generated.tables.records.PaymentsRecord;
import com.basariatpos.model.PaymentDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp; // For mapping from procedure output if needed
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.*;

public class PaymentRepositoryImpl implements PaymentRepository {

    private static final Logger logger = AppLogger.getLogger(PaymentRepositoryImpl.class);

    private PaymentDTO mapRecordToPaymentDTO(Record r) {
        PaymentsRecord paymentRecord = r.into(PAYMENTS); // Base payment fields
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(paymentRecord.getPaymentId());
        dto.setSalesOrderId(paymentRecord.getSalesOrderId());
        dto.setPaymentDate(paymentRecord.getPaymentDate());
        dto.setAmount(paymentRecord.getAmount());
        dto.setPaymentMethod(paymentRecord.getPaymentMethod());
        dto.setBankNameId(paymentRecord.getBankNameId());
        dto.setTransactionId(paymentRecord.getTransactionId());
        dto.setReceivedByUserId(paymentRecord.getReceivedByUserId());
        dto.setShiftId(paymentRecord.getShiftId());
        dto.setNotes(paymentRecord.getNotes());

        // Joined fields
        if (r.field(USERS.USER_ID) != null) { // Check if USERS table was joined
             dto.setReceivedByUsername(r.get(USERS.FULL_NAME));
        }
        if (r.field(BANKNAMES.BANK_NAME_ID) != null && paymentRecord.getBankNameId() != null) { // Check if BANKNAMES table was joined
            dto.setBankNameDisplayEn(r.get(BANKNAMES.BANK_NAME_EN));
            dto.setBankNameDisplayAr(r.get(BANKNAMES.BANK_NAME_AR));
        }
        return dto;
    }


    @Override
    public PaymentDTO save(PaymentDTO paymentDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Recordpaymentandupdatesalesorder routine = new Recordpaymentandupdatesalesorder();
            routine.setPSalesOrderId(paymentDto.getSalesOrderId());
            routine.setPAmount(paymentDto.getAmount());
            routine.setPPaymentMethod(paymentDto.getPaymentMethod());
            routine.setPBankNameId(paymentDto.getBankNameId());
            routine.setPTransactionId(paymentDto.getTransactionId());
            routine.setPReceivedByUserId(paymentDto.getReceivedByUserId());
            // p_shift_id is an INOUT parameter, pass current if available, or let procedure derive.
            // For now, pass what's in DTO, or null if DTO's shiftId is null.
            routine.setPShiftId(paymentDto.getShiftId());
            routine.setPNotes(paymentDto.getNotes());
            // p_payment_date is also INOUT. If null in DTO, procedure will set it to NOW().
            // If provided, it will use that.
            routine.setPPaymentDate(paymentDto.getPaymentDate());


            // Execute the procedure
            routine.execute(dsl.configuration());

            // Update DTO with output parameters from the procedure
            paymentDto.setPaymentId(routine.getPPaymentId());
            // The procedure returns OffsetDateTime for p_payment_date and p_actual_shift_id for shift_id
            paymentDto.setPaymentDate(routine.getPPaymentDate());
            paymentDto.setShiftId(routine.getPActualShiftId());

            // The procedure itself updates the sales order, so we don't fetch it here.
            // The service layer will re-fetch the sales order if needed.
            // We return the PaymentDTO updated with procedure outputs.

            logger.info("Payment recorded successfully via procedure for sales order ID: {}. Payment ID: {}",
                        paymentDto.getSalesOrderId(), paymentDto.getPaymentId());
            return paymentDto;

        } catch (DataAccessException e) {
            logger.error("Error saving payment for sales order ID {}: {}", paymentDto.getSalesOrderId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<PaymentDTO> findBySalesOrderId(int salesOrderId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.select(PAYMENTS.asterisk(),
                              USERS.FULL_NAME,
                              BANKNAMES.BANK_NAME_EN,
                              BANKNAMES.BANK_NAME_AR)
                      .from(PAYMENTS)
                      .join(USERS).on(PAYMENTS.RECEIVED_BY_USER_ID.eq(USERS.USER_ID))
                      .leftOuterJoin(BANKNAMES).on(PAYMENTS.BANK_NAME_ID.eq(BANKNAMES.BANK_NAME_ID))
                      .where(PAYMENTS.SALES_ORDER_ID.eq(salesOrderId))
                      .orderBy(PAYMENTS.PAYMENT_DATE.asc())
                      .fetch(this::mapRecordToPaymentDTO);
        } catch (DataAccessException e) {
            logger.error("Error finding payments for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            // Connections are typically managed by DBManager or a connection pool
        }
    }

    @Override
    public Optional<PaymentDTO> findById(int paymentId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Record record = dsl.select(PAYMENTS.asterisk(),
                                     USERS.FULL_NAME,
                                     BANKNAMES.BANK_NAME_EN,
                                     BANKNAMES.BANK_NAME_AR)
                              .from(PAYMENTS)
                              .join(USERS).on(PAYMENTS.RECEIVED_BY_USER_ID.eq(USERS.USER_ID))
                              .leftOuterJoin(BANKNAMES).on(PAYMENTS.BANK_NAME_ID.eq(BANKNAMES.BANK_NAME_ID))
                              .where(PAYMENTS.PAYMENT_ID.eq(paymentId))
                              .fetchOne();

            return Optional.ofNullable(record).map(this::mapRecordToPaymentDTO);
        } catch (DataAccessException e) {
            logger.error("Error finding payment by ID {}: {}", paymentId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }
}
