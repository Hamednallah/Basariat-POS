package com.basariatpos.service;

// import com.basariatpos.model.SalesOrderDTO; // Future DTO placeholder
// import com.basariatpos.model.SalesOrderItemDTO; // Future DTO placeholder
// import java.util.List; // For list of items

/**
 * Service interface for managing sales orders.
 * Methods will be defined in later sprints.
 */
public interface SalesOrderService {

    /**
     * Creates a new sales order.
     * This is a placeholder method signature. Actual parameters will be defined later.
     * For example, it might take a list of items, customer ID, payment details, etc.
     *
     * @param customerId Optional ID of the customer.
     * @param items List of items in the order.
     * @param discountApplied Discount amount or percentage.
     * @return The created SalesOrderDTO.
     * @throws com.basariatpos.service.exception.NoActiveShiftException if no shift is active for the current user.
     * @throws ValidationException if order data is invalid.
     * @throws ProductNotFoundException if a product in the order is not found or insufficient stock.
     * @throws SalesOrderException for other sales order related errors.
     */
    // SalesOrderDTO createSalesOrder(Integer customerId, List<SalesOrderItemDTO> items, java.math.BigDecimal discountApplied)
    //    throws com.basariatpos.service.exception.NoActiveShiftException, ValidationException, ProductNotFoundException, SalesOrderException;

    // Other methods like:
    // Optional<SalesOrderDTO> getSalesOrderById(int orderId);
    // List<SalesOrderDTO> findSalesOrdersByCriteria(/* criteria */);
    // void cancelSalesOrder(int orderId);
}

// Placeholder for custom exceptions related to SalesOrder if needed later
// class SalesOrderException extends RuntimeException {
//     public SalesOrderException(String message) { super(message); }
//     public SalesOrderException(String message, Throwable cause) { super(message, cause); }
// }
// class ProductNotFoundException extends SalesOrderException {
//    public ProductNotFoundException(String message) { super(message); }
//}
