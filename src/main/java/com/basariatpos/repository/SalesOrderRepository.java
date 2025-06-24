package com.basariatpos.repository;

import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository {

    /**
     * Saves the sales order header (Salesorders table).
     * If orderDto.getSalesOrderId() is 0, it's an insert, otherwise an update.
     * This method typically does NOT save/update items; that's handled by saveOrderItem or a more comprehensive method.
     * @param orderDto The SalesOrderDTO with header information.
     * @return The saved SalesOrderDTO, updated with a generated ID if it was an insert.
     */
    SalesOrderDTO saveOrderHeader(SalesOrderDTO orderDto);

    /**
     * Saves a sales order item (Salesorderitems table).
     * If itemDto.getSoItemId() is 0, it's an insert, otherwise an update.
     * Requires salesOrderId to be set in itemDto.
     * @param itemDto The SalesOrderItemDTO to save.
     * @return The saved SalesOrderItemDTO, updated with a generated ID if it was an insert.
     */
    SalesOrderItemDTO saveOrderItem(SalesOrderItemDTO itemDto);

    /**
     * Deletes a specific sales order item by its ID.
     * @param soItemId The ID of the sales order item to delete.
     */
    void deleteOrderItem(int soItemId);

    /**
     * Deletes all items associated with a given sales order ID.
     * Useful when replacing all items in an order.
     * @param salesOrderId The ID of the sales order whose items are to be deleted.
     */
    void deleteOrderItemsBySalesOrderId(int salesOrderId);

    /**
     * Finds a sales order by its ID, including all its items and related display names.
     * @param salesOrderId The ID of the sales order.
     * @return Optional of SalesOrderDTO, fully populated.
     */
    Optional<SalesOrderDTO> findById(int salesOrderId);

    /**
     * Finds all sales orders within a date range, optionally filtered by status and patient name/ID.
     * This typically returns a summary (header info) and not necessarily full item details for performance.
     * @param fromDate Start date for the order search.
     * @param toDate End date for the order search.
     * @param statusFilter Optional filter for order status (e.g., "Pending", "Completed"). Null or empty for no status filter.
     * @param patientQuery Optional filter for patient name or system ID. Null or empty for no patient filter.
     * @return List of matching SalesOrderDTOs (summaries).
     */
    List<SalesOrderDTO> findAllOrderSummaries(LocalDate fromDate, LocalDate toDate, String statusFilter, String patientQuery);

    /**
     * Updates the status of a specific sales order.
     * @param salesOrderId The ID of the sales order.
     * @param newStatus The new status string.
     */
    void updateOrderStatus(int salesOrderId, String newStatus);

    /**
     * Updates the discount amount for a specific sales order.
     * The database (via procedure or trigger) should ideally recalculate total_amount and balance_due.
     * @param salesOrderId The ID of the sales order.
     * @param discountAmount The new discount amount.
     */
    void updateOrderDiscount(int salesOrderId, BigDecimal discountAmount);

    /**
     * Calls the stored procedure RecalculateSalesOrderSubtotal in the database.
     * This procedure is expected to update subtotal_amount, total_amount, and balance_due on the Salesorders table.
     * @param salesOrderId The ID of the sales order to recalculate.
     */
    void callRecalculateSalesOrderSubtotalProcedure(int salesOrderId);

    // Potentially add a method for a full save (header + items in one transaction) if complex logic is needed here,
    // though often such orchestration is handled in the service layer.
    // SalesOrderDTO saveFullOrder(SalesOrderDTO orderDto);
}
