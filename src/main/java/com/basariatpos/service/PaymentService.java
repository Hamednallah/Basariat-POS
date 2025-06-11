package com.basariatpos.service;

// import com.basariatpos.model.PaymentDTO; // Future DTO
// import java.math.BigDecimal;

/**
 * Service interface for managing payments.
 * Methods will be defined in later sprints.
 */
public interface PaymentService {

    /**
     * Records a payment for a sales order.
     * This is a placeholder method signature.
     *
     * @param orderId The ID of the sales order for which payment is being made.
     * @param amount The amount of the payment.
     * @param paymentMethod E.g., "Cash", "Card", "Mobile Money".
     * @param paymentReference Optional reference for the payment (e.g., transaction ID for card payments).
     * @return The created PaymentDTO.
     * @throws com.basariatpos.service.exception.NoActiveShiftException if no shift is active for the current user (especially for cash payments).
     * @throws ValidationException if payment data is invalid.
     * @throws SalesOrderNotFoundException if the orderId is invalid.
     * @throws PaymentException for other payment related errors.
     */
    // PaymentDTO recordPayment(int orderId, BigDecimal amount, String paymentMethod, String paymentReference)
    //    throws com.basariatpos.service.exception.NoActiveShiftException, ValidationException, SalesOrderNotFoundException, PaymentException;

    // Other methods like:
    // Optional<PaymentDTO> getPaymentById(int paymentId);
    // List<PaymentDTO> findPaymentsByOrderId(int orderId);
    // void refundPayment(int paymentId, BigDecimal refundAmount);
}

// Placeholder for custom exceptions related to Payment if needed later
// class PaymentException extends RuntimeException {
//     public PaymentException(String message) { super(message); }
//     public PaymentException(String message, Throwable cause) { super(message, cause); }
// }
// class SalesOrderNotFoundException extends PaymentException { // Or make it a general exception
//    public SalesOrderNotFoundException(String message) { super(message); }
//}
