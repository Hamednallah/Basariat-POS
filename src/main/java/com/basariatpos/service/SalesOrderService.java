package com.basariatpos.service;

import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.service.exception.*; // Import all custom exceptions

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesOrderService {

    /**
     * Creates a new sales order.
     * Validates patient, items, and ensures an active shift.
     * @param orderDto The SalesOrderDTO containing header and initial items.
     * @return The created SalesOrderDTO, fully populated with IDs and calculated fields.
     * @throws SalesOrderValidationException if order data is invalid.
     * @throws NoActiveShiftException if no shift is active for the current user.
     * @throws PatientNotFoundException if patientId is provided but patient does not exist.
     * @throws InventoryItemNotFoundException if an inventory item in the order is not found.
     * @throws ProductNotFoundException if a service product in the order is not found.
     * @throws SalesOrderServiceException for other service or repository level errors.
     */
    SalesOrderDTO createSalesOrder(SalesOrderDTO orderDto)
        throws SalesOrderValidationException, NoActiveShiftException, PatientNotFoundException,
               InventoryItemNotFoundException, ProductNotFoundException, SalesOrderServiceException;

    /**
     * Updates the header information of an existing sales order.
     * (e.g., patient, remarks). Does not modify items, status, or financial fields directly.
     * @param orderDto The SalesOrderDTO with updated header information. salesOrderId must be valid.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderValidationException if header data is invalid.
     * @throws SalesOrderNotFoundException if the order with salesOrderId does not exist.
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO updateSalesOrderHeader(SalesOrderDTO orderDto)
        throws SalesOrderValidationException, SalesOrderNotFoundException, SalesOrderServiceException;

    /**
     * Adds a new item to an existing sales order.
     * Recalculates order totals.
     * @param salesOrderId The ID of the sales order to add the item to.
     * @param itemDto The SalesOrderItemDTO to add.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderNotFoundException if the sales order is not found.
     * @throws SalesOrderValidationException if item data is invalid (e.g., quantity <= 0, price < 0).
     * @throws InventoryItemNotFoundException if itemDto refers to an inventory item that does not exist or is inactive.
     * @throws ProductNotFoundException if itemDto refers to a service product that does not exist or is inactive.
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO addOrderItemToOrder(int salesOrderId, SalesOrderItemDTO itemDto)
        throws SalesOrderNotFoundException, SalesOrderValidationException,
               InventoryItemNotFoundException, ProductNotFoundException, SalesOrderServiceException;

    /**
     * Updates an existing item on a sales order.
     * Recalculates order totals.
     * @param salesOrderId The ID of the sales order.
     * @param itemDto The SalesOrderItemDTO with updated details. soItemId must be valid.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderNotFoundException if the sales order is not found.
     * @throws SalesOrderItemNotFoundException if the soItemId does not exist on the order.
     * @throws SalesOrderValidationException if item data is invalid.
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO updateOrderItemOnOrder(int salesOrderId, SalesOrderItemDTO itemDto)
        throws SalesOrderNotFoundException, SalesOrderItemNotFoundException, SalesOrderValidationException, SalesOrderServiceException;

    /**
     * Removes an item from a sales order.
     * Recalculates order totals.
     * @param salesOrderId The ID of the sales order.
     * @param soItemId The ID of the sales order item to remove.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderNotFoundException if the sales order is not found.
     * @throws SalesOrderItemNotFoundException if the soItemId does not exist on the order.
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO removeOrderItemFromOrder(int salesOrderId, int soItemId)
        throws SalesOrderNotFoundException, SalesOrderItemNotFoundException, SalesOrderServiceException;

    /**
     * Applies a discount to the sales order.
     * Recalculates order totals.
     * @param salesOrderId The ID of the sales order.
     * @param discountAmount The discount amount to apply.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderNotFoundException if the sales order is not found.
     * @throws PermissionDeniedException if the current user does not have permission to give discounts.
     * @throws SalesOrderValidationException if the discount amount is invalid (e.g., negative, or exceeds total).
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO applyDiscountToOrder(int salesOrderId, BigDecimal discountAmount)
        throws SalesOrderNotFoundException, PermissionDeniedException, SalesOrderValidationException, SalesOrderServiceException;

    /**
     * Changes the status of a sales order.
     * If status changes to "Completed", triggers stock deduction.
     * @param salesOrderId The ID of the sales order.
     * @param newStatus The new status string.
     * @return The updated SalesOrderDTO.
     * @throws SalesOrderNotFoundException if the sales order is not found.
     * @throws SalesOrderValidationException if the status transition is invalid or balance is due for "Completed" status.
     * @throws InventoryItemServiceException if stock deduction fails.
     * @throws SalesOrderServiceException for other errors.
     */
    SalesOrderDTO changeOrderStatus(int salesOrderId, String newStatus)
        throws SalesOrderNotFoundException, SalesOrderValidationException, InventoryItemServiceException, SalesOrderServiceException;

    /**
     * Retrieves the full details of a sales order by its ID.
     * @param salesOrderId The ID of the sales order.
     * @return Optional of SalesOrderDTO.
     * @throws SalesOrderServiceException for repository level errors.
     */
    Optional<SalesOrderDTO> getSalesOrderDetails(int salesOrderId) throws SalesOrderServiceException;

    /**
     * Finds sales orders based on specified criteria.
     * @param fromDate Start date of the search range.
     * @param toDate End date of the search range.
     * @param statusFilter Optional filter by order status.
     * @param patientQuery Optional filter by patient name or system ID.
     * @return List of SalesOrderDTO summaries.
     * @throws SalesOrderServiceException for repository level errors.
     */
    List<SalesOrderDTO> findSalesOrders(LocalDate fromDate, LocalDate toDate, String statusFilter, String patientQuery)
        throws SalesOrderServiceException;
}
