package com.basariatpos.service;

import com.basariatpos.model.PaymentDTO;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.PaymentException;
import com.basariatpos.service.exception.PaymentValidationException;
import com.basariatpos.service.exception.SalesOrderNotFoundException;
// Consider adding a SalesOrderUpdateException if the procedure can signal specific update failures on SalesOrder
// For now, PaymentException can cover general issues.

import java.util.List;

public interface PaymentService {

    /**
     * Records a payment for a sales order.
     *
     * @param paymentDto The DTO containing payment details.
     * @return The recorded PaymentDTO, updated with generated ID and potentially other fields from DB.
     * @throws PaymentValidationException if payment data is invalid (e.g., amount, method, bank details).
     * @throws NoActiveShiftException if no shift is active for the current user (especially for cash).
     * @throws SalesOrderNotFoundException if the associated sales order is not found.
     * @throws PaymentException for other payment processing errors or issues updating the sales order.
     */
    PaymentDTO recordPayment(PaymentDTO paymentDto)
        throws PaymentValidationException, NoActiveShiftException, SalesOrderNotFoundException, PaymentException;

    /**
     * Retrieves all payments recorded for a specific sales order.
     *
     * @param salesOrderId The ID of the sales order.
     * @return A list of PaymentDTOs.
     * @throws SalesOrderNotFoundException if the sales order itself is not found (optional, could also return empty list).
     * @throws PaymentException for other data access errors.
     */
    List<PaymentDTO> getPaymentsForOrder(int salesOrderId)
        throws SalesOrderNotFoundException, PaymentException;

    /**
     * Retrieves a specific payment by its ID.
     *
     * @param paymentId The ID of the payment.
     * @return An Optional containing the PaymentDTO if found.
     * @throws PaymentException for data access errors.
     */
    Optional<PaymentDTO> getPaymentById(int paymentId) throws PaymentException;
}
