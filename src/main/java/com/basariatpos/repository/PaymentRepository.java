package com.basariatpos.repository;

import com.basariatpos.model.PaymentDTO;
import java.util.List;

public interface PaymentRepository {

    /**
     * Saves a payment record by calling the RecordPaymentAndUpdateSalesOrder database procedure.
     * The procedure is expected to insert into the Payments table and update related fields
     * in the Salesorders table (like amount_paid, balance_due).
     *
     * @param paymentDto The PaymentDTO containing details of the payment to be recorded.
     *                   The salesOrderId, amount, paymentMethod, and receivedByUserId are mandatory.
     *                   bankNameId and transactionId may be required depending on the paymentMethod.
     *                   shiftId might be set by the procedure based on context or taken from DTO.
     * @return The saved PaymentDTO, potentially updated with generated paymentId, actualPaymentDate,
     *         and actualShiftId from the database procedure's output.
     *         If the procedure does not return these, the DTO might be returned as is with only the input ID,
     *         or a subsequent fetch might be needed (handled by service if so).
     */
    PaymentDTO save(PaymentDTO paymentDto);

    /**
     * Finds all payments associated with a specific sales order.
     * Includes details like bank name (if applicable) and receiving user's name.
     *
     * @param salesOrderId The ID of the sales order.
     * @return A list of PaymentDTOs for the given order, ordered by paymentDate.
     */
    List<PaymentDTO> findBySalesOrderId(int salesOrderId);

    /**
     * Finds a single payment by its primary key.
     * Includes details like bank name (if applicable) and receiving user's name.
     *
     * @param paymentId The ID of the payment.
     * @return An Optional containing the PaymentDTO if found.
     */
    Optional<PaymentDTO> findById(int paymentId);
}
