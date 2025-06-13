package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.PurchaseOrderRepository;
import com.basariatpos.service.exception.*; // Assuming all PO exceptions are here
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final Logger logger = AppLogger.getLogger(PurchaseOrderServiceImpl.class);
    private final PurchaseOrderRepository poRepository;
    private final InventoryItemService inventoryItemService; // To validate inventoryItemId
    private final UserSessionService userSessionService;     // For createdByUserId

    public PurchaseOrderServiceImpl(PurchaseOrderRepository poRepository,
                                    InventoryItemService inventoryItemService,
                                    UserSessionService userSessionService) {
        this.poRepository = poRepository;
        this.inventoryItemService = inventoryItemService;
        this.userSessionService = userSessionService;
    }

    @Override
    public PurchaseOrderDTO createNewPurchaseOrder(PurchaseOrderDTO orderDto)
        throws PurchaseOrderValidationException, PurchaseOrderException {
        validatePoHeader(orderDto, true); // true for create
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new PurchaseOrderValidationException("Purchase order must have at least one item.", List.of("PO Items empty"));
        }
        for (PurchaseOrderItemDTO item : orderDto.getItems()) {
            validatePoItem(item, true); // true for create
        }

        try {
            UserDTO currentUser = userSessionService.getCurrentUser();
            if (currentUser == null) {
                throw new PurchaseOrderException("No logged-in user to create purchase order.");
            }
            orderDto.setCreatedByUserId(currentUser.getUserId());
            // createdByName will be populated by repository join or can be set here if needed for DTO before save

            return poRepository.saveNewOrderWithItems(orderDto);
        } catch (Exception e) {
            logger.error("Error creating new purchase order for supplier '{}': {}", orderDto.getSupplierName(), e.getMessage(), e);
            throw new PurchaseOrderException("Could not create purchase order.", e);
        }
    }

    @Override
    public PurchaseOrderDTO updatePurchaseOrderHeader(PurchaseOrderDTO orderDto)
        throws PurchaseOrderValidationException, PurchaseOrderNotFoundException, PurchaseOrderException {
        if (orderDto.getPurchaseOrderId() <= 0) {
            throw new PurchaseOrderValidationException("Valid Purchase Order ID is required for update.", List.of("Invalid PO ID"));
        }
        validatePoHeader(orderDto, false); // false for update

        try {
            poRepository.findById(orderDto.getPurchaseOrderId())
                .orElseThrow(() -> new PurchaseOrderNotFoundException(orderDto.getPurchaseOrderId()));
            // Further checks: e.g., cannot update header if PO is "Received" or "Cancelled"
            // For now, simple update.
            return poRepository.updateOrderHeader(orderDto);
        } catch (PurchaseOrderNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error updating PO header for ID {}: {}", orderDto.getPurchaseOrderId(), e.getMessage(), e);
            throw new PurchaseOrderException("Could not update PO header.", e);
        }
    }

    @Override
    public PurchaseOrderItemDTO addOrUpdateItemOnOrder(PurchaseOrderItemDTO itemDto)
        throws PurchaseOrderValidationException, InventoryItemNotFoundException, PurchaseOrderNotFoundException, PurchaseOrderException {
        validatePoItem(itemDto, itemDto.getPoItemId() == 0); // isCreate based on poItemId
        if (itemDto.getPurchaseOrderId() <= 0) {
            throw new PurchaseOrderValidationException("Purchase Order ID must be specified for item.", List.of("Missing PO ID on item"));
        }

        try {
            // Ensure parent PO exists and is in a modifiable state
            PurchaseOrderDTO parentPO = poRepository.findById(itemDto.getPurchaseOrderId())
                .orElseThrow(() -> new PurchaseOrderNotFoundException(itemDto.getPurchaseOrderId()));
            if ("Received".equalsIgnoreCase(parentPO.getStatus()) || "Cancelled".equalsIgnoreCase(parentPO.getStatus())) {
                throw new PurchaseOrderException("Cannot modify items on a PO that is already " + parentPO.getStatus());
            }

            // Ensure inventory item exists
            inventoryItemService.getInventoryItemById(itemDto.getInventoryItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(itemDto.getInventoryItemId()));

            return poRepository.saveOrderItem(itemDto);
            // Note: totalAmount of PO header should be updated by DB trigger or another service call.
        } catch (InventoryItemNotFoundException | PurchaseOrderNotFoundException | PurchaseOrderException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving PO item for PO ID {}: {}", itemDto.getPurchaseOrderId(), e.getMessage(), e);
            throw new PurchaseOrderException("Could not save PO item.", e);
        }
    }

    @Override
    public void removeItemFromOrder(int poItemId, int purchaseOrderId)
        throws PurchaseOrderNotFoundException, PurchaseOrderException {
         try {
            PurchaseOrderDTO parentPO = poRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId));
            if ("Received".equalsIgnoreCase(parentPO.getStatus()) || "Cancelled".equalsIgnoreCase(parentPO.getStatus())) {
                throw new PurchaseOrderException("Cannot remove items from a PO that is already " + parentPO.getStatus());
            }
            // Optional: Verify item belongs to this PO before deleting, though poItemId should be globally unique.
            poRepository.deleteOrderItem(poItemId);
            // Note: totalAmount of PO header should be updated by DB trigger or another service call.
        } catch (PurchaseOrderNotFoundException | PurchaseOrderException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error removing PO item ID {}: {}", poItemId, e.getMessage(), e);
            throw new PurchaseOrderException("Could not remove PO item.", e);
        }
    }

    @Override
    public void receiveStockForItem(int poItemId, int purchaseOrderId, int quantityJustReceived, BigDecimal newPurchasePricePerUnit)
        throws StockReceivingException, PurchaseOrderNotFoundException, InventoryItemNotFoundException, ValidationException, PurchaseOrderException {

        if (quantityJustReceived <= 0) {
            throw new ValidationException("Quantity received must be positive.", List.of("Invalid received quantity"));
        }
        if (newPurchasePricePerUnit == null || newPurchasePricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Purchase price must be non-negative.", List.of("Invalid purchase price"));
        }

        try {
            PurchaseOrderDTO po = poRepository.findById(purchaseOrderId)
                                  .orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId));
            if ("Cancelled".equalsIgnoreCase(po.getStatus()) || "Received".equalsIgnoreCase(po.getStatus())) {
                 throw new StockReceivingException("Cannot receive stock for a PO that is already " + po.getStatus());
            }

            PurchaseOrderItemDTO item = po.getItems().stream()
                                       .filter(i -> i.getPoItemId() == poItemId)
                                       .findFirst()
                                       .orElseThrow(() -> new PurchaseOrderNotFoundException(poItemId, "on PO " + purchaseOrderId));

            if (item.getQuantityReceived() + quantityJustReceived > item.getQuantityOrdered()) {
                throw new StockReceivingException("Cannot receive more than ordered. Ordered: " + item.getQuantityOrdered() +
                                                  ", Already Received: " + item.getQuantityReceived() +
                                                  ", Attempting to Receive: " + quantityJustReceived);
            }

            // Ensure inventory item exists (though it should if it's on PO)
            inventoryItemService.getInventoryItemById(item.getInventoryItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(item.getInventoryItemId()));

            poRepository.updateOrderItemReceivedQuantityAndPrice(poItemId, item.getQuantityReceived() + quantityJustReceived, newPurchasePricePerUnit);
            // DB Triggers are expected to:
            // 1. Update InventoryItem.quantity_on_hand
            // 2. Update InventoryItem.cost_price (e.g., weighted average or last price)
            // 3. Update PurchaseOrder.status (e.g., to 'Partial' or 'Received')
            // 4. Update PurchaseOrder.total_amount if subtotal of item changed due to price change on receive.

            logger.info("Stock received for PO Item ID {}: {} units. Price per unit: {}", poItemId, quantityJustReceived, newPurchasePricePerUnit);

        } catch (PurchaseOrderNotFoundException | InventoryItemNotFoundException | StockReceivingException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error receiving stock for PO item ID {}: {}", poItemId, e.getMessage(), e);
            throw new PurchaseOrderException("Could not receive stock.", e);
        }
    }

    @Override
    public void updatePurchaseOrderStatus(int purchaseOrderId, String newStatus)
        throws PurchaseOrderNotFoundException, ValidationException, PurchaseOrderException {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new ValidationException("New status cannot be empty.", List.of("Status required"));
        }
        // Add more validation for allowed status transitions if needed
        try {
            poRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId));
            poRepository.updateOrderStatus(purchaseOrderId, newStatus);
        } catch (PurchaseOrderNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating status for PO ID {}: {}", purchaseOrderId, e.getMessage(), e);
            throw new PurchaseOrderException("Could not update PO status.", e);
        }
    }


    @Override
    public List<PurchaseOrderDTO> getAllPurchaseOrderSummaries() throws PurchaseOrderException {
        try {
            return poRepository.findAllSummaries();
        } catch (Exception e) {
            logger.error("Error retrieving all PO summaries: {}", e.getMessage(), e);
            throw new PurchaseOrderException("Could not retrieve PO summaries.", e);
        }
    }

    @Override
    public Optional<PurchaseOrderDTO> getPurchaseOrderDetails(int purchaseOrderId) throws PurchaseOrderException {
        try {
            return poRepository.findById(purchaseOrderId);
        } catch (Exception e) {
            logger.error("Error retrieving PO details for ID {}: {}", purchaseOrderId, e.getMessage(), e);
            throw new PurchaseOrderException("Could not retrieve PO details.", e);
        }
    }

    // --- Validation Helpers ---
    private void validatePoHeader(PurchaseOrderDTO dto, boolean isCreate) throws PurchaseOrderValidationException {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Purchase Order data cannot be null.");
            throw new PurchaseOrderValidationException(errors);
        }
        if (dto.getOrderDate() == null) errors.add("Order date is required.");
        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) errors.add("Supplier name is required.");
        if (isCreate && dto.getCreatedByUserId() <= 0) errors.add("Valid creator user ID is required.");
        // Status validation could be more complex (enum, allowed transitions)
        if (dto.getStatus() != null && dto.getStatus().length() > 50) errors.add("Status text too long.");

        if (!errors.isEmpty()) throw new PurchaseOrderValidationException(errors);
    }

    private void validatePoItem(PurchaseOrderItemDTO item, boolean isCreate) throws PurchaseOrderValidationException {
        List<String> errors = new ArrayList<>();
        if (item == null) {
            errors.add("Purchase Order Item data cannot be null.");
            throw new PurchaseOrderValidationException(errors);
        }
        if (item.getInventoryItemId() <= 0) errors.add("Valid Inventory Item ID is required for each order item.");
        if (item.getQuantityOrdered() <= 0) errors.add("Quantity ordered must be positive.");
        if (item.getPurchasePricePerUnit() == null || item.getPurchasePricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Purchase price per unit must be non-negative.");
        }
        if (!isCreate && item.getPoItemId() <= 0) errors.add("Valid PO Item ID is required for item update.");
        // quantityReceived validation might be needed if directly updatable through this method

        if (!errors.isEmpty()) throw new PurchaseOrderValidationException(errors);
    }
}
