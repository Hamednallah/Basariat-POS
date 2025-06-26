package com.basariatpos.service;

import com.basariatpos.model.PaymentDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.PaymentRepository;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.PaymentException;
import com.basariatpos.service.exception.PaymentValidationException;
import com.basariatpos.service.exception.SalesOrderNotFoundException;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = AppLogger.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final UserSessionService userSessionService;
    private final SalesOrderService salesOrderService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              UserSessionService userSessionService,
                              SalesOrderService salesOrderService) {
        if (paymentRepository == null) throw new NullPointerException("PaymentRepository cannot be null.");
        if (userSessionService == null) throw new NullPointerException("UserSessionService cannot be null.");
        if (salesOrderService == null) throw new NullPointerException("SalesOrderService cannot be null.");

        this.paymentRepository = paymentRepository;
        this.userSessionService = userSessionService;
        this.salesOrderService = salesOrderService;
    }

    @Override
    public PaymentDTO recordPayment(PaymentDTO paymentDto)
            throws PaymentValidationException, NoActiveShiftException, SalesOrderNotFoundException, PaymentException {

        logger.info("Attempting to record payment for sales order ID: {}", paymentDto.getSalesOrderId());
        validateActiveShift();

        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
            throw new PaymentException("No authenticated user found for recording payment.");
        }

        List<String> errors = new ArrayList<>();
        if (paymentDto.getSalesOrderId() <= 0) {
            errors.add("Sales Order ID is required.");
        }
        if (paymentDto.getAmount() == null || paymentDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Payment amount must be positive.");
        }
        if (paymentDto.getPaymentMethod() == null || paymentDto.getPaymentMethod().trim().isEmpty()) {
            errors.add("Payment method is required.");
        }

        SalesOrderDTO order = salesOrderService.getSalesOrderDetails(paymentDto.getSalesOrderId())
            .orElseThrow(() -> new SalesOrderNotFoundException(paymentDto.getSalesOrderId()));

        if (paymentDto.getAmount().compareTo(order.getBalanceDue()) > 0) {
            errors.add("Payment amount (" + paymentDto.getAmount() +
                       ") cannot exceed balance due (" + order.getBalanceDue() + ").");
        }

        String paymentMethod = paymentDto.getPaymentMethod();
        if ("Bank Transfer".equalsIgnoreCase(paymentMethod) ||
            "Card".equalsIgnoreCase(paymentMethod) ||
            "Cheque".equalsIgnoreCase(paymentMethod)) {
            if (paymentDto.getBankNameId() == null || paymentDto.getBankNameId() <= 0) {
                errors.add("Bank name is required for " + paymentMethod + ".");
            }
            if (paymentDto.getTransactionId() == null || paymentDto.getTransactionId().trim().isEmpty()) {
                errors.add("Transaction ID/Reference is required for " + paymentMethod + ".");
            }
        }

        if (!errors.isEmpty()) {
            throw new PaymentValidationException(errors);
        }

        paymentDto.setReceivedByUserId(currentUser.getUserId());
        paymentDto.setShiftId(userSessionService.getActiveShift().getShiftId());
        if (paymentDto.getPaymentDate() == null) {
            paymentDto.setPaymentDate(OffsetDateTime.now());
        }

        try {
            return paymentRepository.save(paymentDto);
        } catch (Exception e) {
            logger.error("Error recording payment for order ID {}: {}", paymentDto.getSalesOrderId(), e.getMessage(), e);
            throw new PaymentException("Could not record payment due to a data access error.", e);
        }
    }

    @Override
    public List<PaymentDTO> getPaymentsForOrder(int salesOrderId) throws SalesOrderNotFoundException, PaymentException {
        logger.debug("Fetching payments for order ID: {}", salesOrderId);
        salesOrderService.getSalesOrderDetails(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));
        try {
            return paymentRepository.findBySalesOrderId(salesOrderId);
        } catch (Exception e) {
            logger.error("Error retrieving payments for order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw new PaymentException("Could not retrieve payments.", e);
        }
    }

    @Override
    public Optional<PaymentDTO> getPaymentById(int paymentId) throws PaymentException {
        logger.debug("Fetching payment by ID: {}", paymentId);
        try {
            return paymentRepository.findById(paymentId);
        } catch (Exception e) {
            logger.error("Error retrieving payment by ID {}: {}", paymentId, e.getMessage(), e);
            throw new PaymentException("Could not retrieve payment by ID.", e);
        }
    }

    private void validateActiveShift() throws NoActiveShiftException {
        if (userSessionService.getCurrentUser() == null || !userSessionService.isShiftActive()) {
            String username = (userSessionService.getCurrentUser() != null) ? userSessionService.getCurrentUser().getUsername() : "None";
            logger.warn("Operation failed: No active shift for current user (User: {}, Shift Active: {}).",
                         username, userSessionService.isShiftActive());
            throw new NoActiveShiftException("Operation requires an active shift for current user.");
        }
    }
}
