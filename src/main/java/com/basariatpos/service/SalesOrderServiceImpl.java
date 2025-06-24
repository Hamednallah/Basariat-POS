package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.model.PatientDTO; // For patient validation
import com.basariatpos.repository.SalesOrderRepository;
import com.basariatpos.service.exception.*;
import com.basariatpos.util.AppLogger;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Logger logger = AppLogger.getLogger(SalesOrderServiceImpl.class);

    private final SalesOrderRepository salesOrderRepository;
    private final UserSessionService userSessionService;
    private final InventoryItemService inventoryItemService;
    private final ProductService productService;
    private final PatientService patientService;

    public SalesOrderServiceImpl(SalesOrderRepository salesOrderRepository,
                                 UserSessionService userSessionService,
                                 InventoryItemService inventoryItemService,
                                 ProductService productService,
                                 PatientService patientService) {
        this.salesOrderRepository = salesOrderRepository;
        this.userSessionService = userSessionService;
        this.inventoryItemService = inventoryItemService;
        this.productService = productService;
        this.patientService = patientService;
    }

    @Override
    public SalesOrderDTO createSalesOrder(SalesOrderDTO orderDto)
            throws SalesOrderValidationException, NoActiveShiftException, PatientNotFoundException,
                   InventoryItemNotFoundException, ProductNotFoundException, SalesOrderServiceException {
        logger.info("Attempting to create sales order.");
        validateActiveShift();

        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) { // Should be caught by validateActiveShift, but good practice
            throw new SalesOrderServiceException("No authenticated user found.");
        }

        // Validate DTO
        List<String> errors = new ArrayList<>();
        if (orderDto == null) {
            errors.add("SalesOrderDTO cannot be null.");
            throw new SalesOrderValidationException(errors);
        }
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            errors.add("Sales order must have at least one item.");
        }

        // Validate discount amount early if present on DTO
        if (orderDto.getDiscountAmount() != null && orderDto.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(MessageProvider.getString("salesorder.validation.discountNonNegative"));
        }


        // Validate patient if patientId is provided
        if (orderDto.getPatientId() != null && orderDto.getPatientId() > 0) {
            patientService.getPatientById(orderDto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(orderDto.getPatientId()));
        } else {
            orderDto.setPatientId(null); // Ensure it's null if not provided or invalid
        }

        // Validate items
        for (SalesOrderItemDTO item : orderDto.getItems()) {
            validateSalesOrderItem(item, errors);
        }

        if (!errors.isEmpty()) {
            throw new SalesOrderValidationException(errors);
        }

        // Set server-side details
        orderDto.setCreatedByUserId(currentUser.getUserId());
        orderDto.setShiftId(userSessionService.getActiveShift().getShiftId()); // Assumes shift is active
        orderDto.setOrderDate(OffsetDateTime.now());
        orderDto.setStatus("Pending"); // Initial status

        try {
            // Save header first to get salesOrderId
            SalesOrderDTO savedHeader = salesOrderRepository.saveOrderHeader(orderDto);

            List<SalesOrderItemDTO> savedItems = new ArrayList<>();
            for (SalesOrderItemDTO item : orderDto.getItems()) {
                item.setSalesOrderId(savedHeader.getSalesOrderId());
                // Populate display names before saving item (if not already populated by client)
                if (item.getItemDisplayNameEn() == null || item.getItemDisplayNameEn().trim().isEmpty()) {
                    populateItemDisplayNames(item);
                }
                savedItems.add(salesOrderRepository.saveOrderItem(item));
            }
            savedHeader.setItems(savedItems);

            // Recalculate totals
            salesOrderRepository.callRecalculateSalesOrderSubtotalProcedure(savedHeader.getSalesOrderId());

            // Fetch the fully populated order
            return salesOrderRepository.findById(savedHeader.getSalesOrderId())
                .orElseThrow(() -> new SalesOrderServiceException("Failed to retrieve created sales order."));

        } catch (Exception e) {
            logger.error("Error creating sales order: {}", e.getMessage(), e);
            throw new SalesOrderServiceException("Could not create sales order.", e);
        }
    }

    private void populateItemDisplayNames(SalesOrderItemDTO item) throws InventoryItemNotFoundException, ProductNotFoundException {
        if (item.getInventoryItemId() != null && item.getInventoryItemId() > 0) {
            InventoryItemDTO invItem = inventoryItemService.getInventoryItemById(item.getInventoryItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(item.getInventoryItemId()));
            item.setItemDisplayNameEn(invItem.getProductNameEn()); // Main product name
            item.setItemDisplaySpecificNameEn(invItem.getItemSpecificNameEn()); // Specific variant name
        } else if (item.getServiceProductId() != null && item.getServiceProductId() > 0) {
            ProductDTO serviceProd = productService.getProductById(item.getServiceProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getServiceProductId()));
            item.setItemDisplayNameEn(serviceProd.getProductNameEn());
        }
    }


    @Override
    public SalesOrderDTO updateSalesOrderHeader(SalesOrderDTO orderDto)
            throws SalesOrderValidationException, SalesOrderNotFoundException, SalesOrderServiceException {
        logger.info("Updating sales order header for ID: {}", orderDto.getSalesOrderId());
        if (orderDto.getSalesOrderId() <= 0) {
            throw new SalesOrderValidationException("Valid Sales Order ID is required for update.", List.of("Invalid Sales Order ID."));
        }

        SalesOrderDTO existingOrder = salesOrderRepository.findById(orderDto.getSalesOrderId())
            .orElseThrow(() -> new SalesOrderNotFoundException(orderDto.getSalesOrderId()));

        // Update allowed fields: patientId, remarks
        if (orderDto.getPatientId() != null && orderDto.getPatientId() > 0) {
            try {
                patientService.getPatientById(orderDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(orderDto.getPatientId()));
                existingOrder.setPatientId(orderDto.getPatientId());
            } catch (PatientNotFoundException e) {
                 throw new SalesOrderValidationException("Invalid Patient ID provided for update.", List.of(e.getMessage()));
            }
        } else {
            existingOrder.setPatientId(null); // Allow unsetting patient
        }
        existingOrder.setRemarks(orderDto.getRemarks());

        try {
            salesOrderRepository.saveOrderHeader(existingOrder);
            return salesOrderRepository.findById(existingOrder.getSalesOrderId())
                 .orElseThrow(() -> new SalesOrderServiceException("Failed to retrieve updated sales order."));
        } catch (Exception e) {
            logger.error("Error updating sales order header for ID {}: {}", orderDto.getSalesOrderId(), e.getMessage(), e);
            throw new SalesOrderServiceException("Could not update sales order header.", e);
        }
    }

    private void validateSalesOrderItem(SalesOrderItemDTO item, List<String> errors)
        throws InventoryItemNotFoundException, ProductNotFoundException {
        if (item == null) {
            errors.add("Sales order item cannot be null.");
            return;
        }

        boolean isCustomQuote = MessageProvider.getString("salesorder.itemtype.customquote").equals(item.getItemDisplayNameEn());
        boolean isCustomLens = item.isCustomLenses(); // Custom lenses also have custom descriptions/prices but specific handling

        if (!isCustomQuote && !isCustomLens) { // Standard validation for non-custom items
            if ((item.getInventoryItemId() == null || item.getInventoryItemId() <= 0) &&
                (item.getServiceProductId() == null || item.getServiceProductId() <= 0)) {
                errors.add("Each non-custom item must have a valid inventory item ID or service product ID.");
            }
        }

        // This validation should apply to custom quote items too if they are not linked to product/inventory item
        if (isCustomQuote && (item.getDescription() == null || item.getDescription().trim().isEmpty())) {
            errors.add("Description is required for a custom quote service item.");
        }

        if (item.getInventoryItemId() != null && item.getInventoryItemId() > 0 &&
            item.getServiceProductId() != null && item.getServiceProductId() > 0) {
            errors.add("Order item cannot be both an inventory item and a service product simultaneously.");
        }
        if (item.getQuantity() <= 0) {
            errors.add("Item quantity must be greater than 0 for '" + item.getItemDisplayNameEn() + "'.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Item unit price must be non-negative for '" + item.getItemDisplayNameEn() + "'.");
        }
        // Check if item/product exists and is active
        if (item.getInventoryItemId() != null && item.getInventoryItemId() > 0) {
            InventoryItemDTO invItem = inventoryItemService.getInventoryItemById(item.getInventoryItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(item.getInventoryItemId()));
            if (!invItem.isActive()) errors.add("Inventory item '" + invItem.getDisplayFullNameEn() + "' is not active.");
        } else if (item.getServiceProductId() != null && item.getServiceProductId() > 0) {
            ProductDTO serviceProd = productService.getProductById(item.getServiceProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getServiceProductId()));
            if (!serviceProd.isActive()) errors.add("Service '" + serviceProd.getProductNameEn() + "' is not active.");
        }
    }


    @Override
    public SalesOrderDTO addOrderItemToOrder(int salesOrderId, SalesOrderItemDTO itemDto)
            throws SalesOrderNotFoundException, SalesOrderValidationException,
                   InventoryItemNotFoundException, ProductNotFoundException, SalesOrderServiceException {
        logger.info("Adding item to sales order ID: {}", salesOrderId);
        SalesOrderDTO order = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));

        List<String> errors = new ArrayList<>();
        validateSalesOrderItem(itemDto, errors);
        if (!errors.isEmpty()) {
            throw new SalesOrderValidationException(errors);
        }

        itemDto.setSalesOrderId(salesOrderId);
        itemDto.setSoItemId(0); // Ensure it's treated as new
        try {
            populateItemDisplayNames(itemDto); // Populate names before saving
            salesOrderRepository.saveOrderItem(itemDto);
            salesOrderRepository.callRecalculateSalesOrderSubtotalProcedure(salesOrderId);
            return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderServiceException("Order disappeared after adding item."));
        } catch (InventoryItemNotFoundException | ProductNotFoundException e) {
            throw e; // Propagate specific not found exceptions
        } catch (Exception e) {
            logger.error("Error adding item to order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not add item to order.", e);
        }
    }

    @Override
    public SalesOrderDTO updateOrderItemOnOrder(int salesOrderId, SalesOrderItemDTO itemDto)
            throws SalesOrderNotFoundException, SalesOrderItemNotFoundException,
                   SalesOrderValidationException, SalesOrderServiceException {
        logger.info("Updating item ID {} on sales order ID: {}", itemDto.getSoItemId(), salesOrderId);
        SalesOrderDTO order = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));

        boolean itemExistsOnOrder = order.getItems().stream().anyMatch(i -> i.getSoItemId() == itemDto.getSoItemId());
        if (!itemExistsOnOrder) {
            throw new SalesOrderItemNotFoundException(salesOrderId, itemDto.getSoItemId());
        }

        List<String> errors = new ArrayList<>();
        try {
            validateSalesOrderItem(itemDto, errors); // Validates item details and existence of product/inventory item
        } catch (InventoryItemNotFoundException | ProductNotFoundException e) {
             throw new SalesOrderValidationException(e.getMessage(), List.of(e.getMessage()));
        }
        if (!errors.isEmpty()) {
            throw new SalesOrderValidationException(errors);
        }

        itemDto.setSalesOrderId(salesOrderId); // Ensure correct salesOrderId
        try {
            populateItemDisplayNames(itemDto); // Re-populate names in case item ID changed
            salesOrderRepository.saveOrderItem(itemDto);
            salesOrderRepository.callRecalculateSalesOrderSubtotalProcedure(salesOrderId);
            return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderServiceException("Order disappeared after updating item."));
        } catch (InventoryItemNotFoundException | ProductNotFoundException e) {
            // This should ideally be caught by validateSalesOrderItem if item ID changed to non-existent
             throw new SalesOrderServiceException("Underlying product/item not found during update.", e);
        } catch (Exception e) {
            logger.error("Error updating item ID {} on order ID {}: {}", itemDto.getSoItemId(), salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not update item on order.", e);
        }
    }

    @Override
    public SalesOrderDTO removeOrderItemFromOrder(int salesOrderId, int soItemId)
            throws SalesOrderNotFoundException, SalesOrderItemNotFoundException, SalesOrderServiceException {
        logger.info("Removing item ID {} from sales order ID: {}", soItemId, salesOrderId);
        SalesOrderDTO order = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));

        boolean itemExistsOnOrder = order.getItems().stream().anyMatch(i -> i.getSoItemId() == soItemId);
        if (!itemExistsOnOrder) {
            throw new SalesOrderItemNotFoundException(salesOrderId, soItemId);
        }

        try {
            salesOrderRepository.deleteOrderItem(soItemId);
            salesOrderRepository.callRecalculateSalesOrderSubtotalProcedure(salesOrderId);
            return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderServiceException("Order disappeared after removing item."));
        } catch (Exception e) {
            logger.error("Error removing item ID {} from order ID {}: {}", soItemId, salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not remove item from order.", e);
        }
    }

    @Override
    public SalesOrderDTO applyDiscountToOrder(int salesOrderId, BigDecimal discountAmount)
            throws SalesOrderNotFoundException, PermissionDeniedException, SalesOrderValidationException, SalesOrderServiceException {
        logger.info("Applying discount to sales order ID: {}", salesOrderId);
        validateActiveShift(); // Ensure shift context for user permissions
        if (!userSessionService.hasPermission("CAN_GIVE_DISCOUNT")) {
            throw new PermissionDeniedException("Apply Discount", "User does not have CAN_GIVE_DISCOUNT permission.");
        }

        SalesOrderDTO order = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));

        if (discountAmount == null) { // Should not happen if UI defaults to 0.00, but defensive
             discountAmount = BigDecimal.ZERO;
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new SalesOrderValidationException(MessageProvider.getString("salesorder.validation.discountNonNegative"),
                                                    List.of(MessageProvider.getString("salesorder.validation.discountNonNegative")));
        }

        // Subtotal is read from the fetched order.
        if (order.getSubtotalAmount() == null) { // Should not happen for a saved order after recalc
            throw new SalesOrderServiceException("Order subtotal is not calculated, cannot validate discount for order ID: " + salesOrderId);
        }
        if (discountAmount.compareTo(order.getSubtotalAmount()) > 0) {
            throw new SalesOrderValidationException(MessageProvider.getString("salesorder.validation.discountExceedsSubtotal"),
                                                    List.of(MessageProvider.getString("salesorder.validation.discountExceedsSubtotal")));
        }

        try {
            salesOrderRepository.updateOrderDiscount(salesOrderId, discountAmount);
            // Recalculation is called within updateOrderDiscount in repo
            return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderServiceException("Order disappeared after applying discount."));
        } catch (Exception e) {
            logger.error("Error applying discount to order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not apply discount.", e);
        }
    }

    @Override
    public SalesOrderDTO changeOrderStatus(int salesOrderId, String newStatus)
            throws SalesOrderNotFoundException, SalesOrderValidationException, InventoryItemServiceException, SalesOrderServiceException {
        logger.info("Changing status for sales order ID: {} to {}", salesOrderId, newStatus);
        SalesOrderDTO order = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new SalesOrderNotFoundException(salesOrderId));

        // TODO: Implement robust status transition validation if needed (e.g., cannot go from "Completed" to "Pending")
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new SalesOrderValidationException("New status cannot be empty.", List.of("Status cannot be empty."));
        }

        // Business rule: If moving to "Completed", balance due must be zero or less (e.g. overpayment for later refund)
        if ("Completed".equalsIgnoreCase(newStatus)) {
            if (order.getBalanceDue() != null && order.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
                throw new SalesOrderValidationException(
                    "Order cannot be marked 'Completed' with an outstanding balance of " + order.getBalanceDue(),
                    List.of("Balance due must be <= 0 to complete order.")
                );
            }
        }

        try {
            if ("Completed".equalsIgnoreCase(newStatus) && !"Completed".equalsIgnoreCase(order.getStatus())) {
                inventoryItemService.processOrderCompletionStockUpdate(salesOrderId);
            }
            salesOrderRepository.updateOrderStatus(salesOrderId, newStatus);
            return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderServiceException("Order disappeared after changing status."));
        } catch (InventoryItemServiceException e) {
            logger.error("Stock deduction failed for order ID {} during completion: {}", salesOrderId, e.getMessage(), e);
            throw e; // Propagate
        } catch (Exception e) {
            logger.error("Error changing status for order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not change order status.", e);
        }
    }

    @Override
    public Optional<SalesOrderDTO> getSalesOrderDetails(int salesOrderId) throws SalesOrderServiceException {
        try {
            return salesOrderRepository.findById(salesOrderId);
        } catch (Exception e) {
            logger.error("Error retrieving details for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw new SalesOrderServiceException("Could not retrieve sales order details.", e);
        }
    }

    @Override
    public List<SalesOrderDTO> findSalesOrders(LocalDate fromDate, LocalDate toDate, String statusFilter, String patientQuery)
            throws SalesOrderServiceException {
        try {
            return salesOrderRepository.findAllOrderSummaries(fromDate, toDate, statusFilter, patientQuery);
        } catch (Exception e) {
            logger.error("Error finding sales orders: {}", e.getMessage(), e);
            throw new SalesOrderServiceException("Could not find sales orders.", e);
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
