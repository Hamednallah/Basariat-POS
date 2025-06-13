package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.repository.InventoryItemRepository;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemServiceException;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.ProductNotFoundException; // From ProductService
import com.basariatpos.util.AppLogger;
import com.google.gson.JsonParser; // Using Gson for basic JSON validation
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryItemServiceImpl implements InventoryItemService {

    private static final Logger logger = AppLogger.getLogger(InventoryItemServiceImpl.class);
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductService productService;
    private final AuditLogRepository auditLogRepository; // Added
    private final UserSessionService userSessionService; // Added

    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository,
                                    ProductService productService,
                                    AuditLogRepository auditLogRepository,
                                    UserSessionService userSessionService) {
        if (inventoryItemRepository == null) {
            throw new IllegalArgumentException("InventoryItemRepository cannot be null.");
        }
        if (productService == null) {
            throw new IllegalArgumentException("ProductService cannot be null.");
        }
        if (auditLogRepository == null) {
            throw new IllegalArgumentException("AuditLogRepository cannot be null.");
        }
        if (userSessionService == null) {
            throw new IllegalArgumentException("UserSessionService cannot be null.");
        }
        this.inventoryItemRepository = inventoryItemRepository;
        this.productService = productService;
        this.auditLogRepository = auditLogRepository;
        this.userSessionService = userSessionService;
    }

    @Override
    public InventoryItemDTO saveInventoryItem(InventoryItemDTO itemDto)
        throws InventoryItemValidationException, ProductNotFoundException, InventoryItemServiceException {

        validateInventoryItemDto(itemDto);

        try {
            // Validate productId exists
            productService.getProductById(itemDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemDto.getProductId()));

            return inventoryItemRepository.save(itemDto);
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving inventory item for product ID {}: {}", itemDto.getProductId(), e.getMessage(), e);
            throw new InventoryItemServiceException("Could not save inventory item.", e);
        }
    }

    @Override
    public void toggleActiveStatus(int inventoryItemId)
        throws InventoryItemNotFoundException, InventoryItemServiceException {
        try {
            InventoryItemDTO item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));

            inventoryItemRepository.setActiveStatus(inventoryItemId, !item.isActive());
            logger.info("Toggled active status for inventory item ID {}. New status: {}", inventoryItemId, !item.isActive());
        } catch (InventoryItemNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error toggling status for inventory item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw new InventoryItemServiceException("Could not toggle inventory item status.", e);
        }
    }

    @Override
    public Optional<InventoryItemDTO> getInventoryItemById(int id) throws InventoryItemServiceException {
        try {
            return inventoryItemRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving inventory item by ID {}: {}", id, e.getMessage(), e);
            throw new InventoryItemServiceException("Error retrieving inventory item by ID.", e);
        }
    }

    @Override
    public List<InventoryItemDTO> getInventoryItemsByProduct(int productId, boolean includeInactive) throws InventoryItemServiceException {
         try {
            return inventoryItemRepository.findByProductId(productId, includeInactive);
        } catch (Exception e) {
            logger.error("Error retrieving inventory items for product ID {}: {}", productId, e.getMessage(), e);
            throw new InventoryItemServiceException("Error retrieving inventory items by product.", e);
        }
    }

    @Override
    public List<InventoryItemDTO> getAllInventoryItems(boolean includeInactive) throws InventoryItemServiceException {
        try {
            return inventoryItemRepository.findAll(includeInactive);
        } catch (Exception e) {
            logger.error("Error retrieving all inventory items: {}", e.getMessage(), e);
            throw new InventoryItemServiceException("Error retrieving all inventory items.", e);
        }
    }

    @Override
    public List<InventoryItemDTO> searchInventoryItems(String query, boolean includeInactive) throws InventoryItemServiceException {
         if (query == null || query.trim().isEmpty()) {
            return getAllInventoryItems(includeInactive);
        }
        try {
            return inventoryItemRepository.searchItems(query, includeInactive);
        } catch (Exception e) {
            logger.error("Error searching inventory items with query '{}': {}", query, e.getMessage(), e);
            throw new InventoryItemServiceException("Error during inventory item search.", e);
        }
    }

    @Override
    public List<InventoryItemDTO> getLowStockItemsReport() throws InventoryItemServiceException {
        try {
            return inventoryItemRepository.getLowStockItems();
        } catch (Exception e) {
            logger.error("Error retrieving low stock items report: {}", e.getMessage(), e);
            throw new InventoryItemServiceException("Could not retrieve low stock items report.", e);
        }
    }

    // --- Helper Methods ---
    private void validateInventoryItemDto(InventoryItemDTO dto) throws InventoryItemValidationException {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Inventory item data cannot be null.");
            throw new InventoryItemValidationException(errors);
        }
        if (dto.getProductId() <= 0) {
            errors.add("Product ID is required.");
        }
        if (dto.getItemSpecificNameEn() == null || dto.getItemSpecificNameEn().trim().isEmpty()) {
            errors.add("Item Specific Name (English) is required.");
        }
        if (dto.getSellingPrice() == null || dto.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Selling price must be a non-negative value.");
        }
         if (dto.getCostPrice() != null && dto.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Cost price, if provided, must be a non-negative value.");
        }
        if (dto.getQuantityOnHand() < 0) {
            errors.add("Quantity on hand cannot be negative.");
        }
        if (dto.getMinStockLevel() < 0) {
            errors.add("Minimum stock level cannot be negative.");
        }
        if (dto.getUnitOfMeasure() == null || dto.getUnitOfMeasure().trim().isEmpty()){
             errors.add("Unit of measure is required.");
        }

        // Basic JSON validation for attributes if not empty
        if (dto.getAttributes() != null && !dto.getAttributes().trim().isEmpty()) {
            try {
                JsonParser.parseString(dto.getAttributes());
            } catch (JsonSyntaxException e) {
                errors.add("Attributes field does not contain valid JSON: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new InventoryItemValidationException(errors);
        }
    }

    @Override
    public void performStockAdjustment(int inventoryItemId, int quantityChange, String reason)
        throws InventoryItemNotFoundException, InventoryItemValidationException, InventoryItemServiceException {

        if (reason == null || reason.trim().isEmpty()) {
            throw new InventoryItemValidationException("Reason for stock adjustment is required.", List.of("Reason required."));
        }
        if (quantityChange == 0) {
            // Or just log and return if zero change is not an error.
            throw new InventoryItemValidationException("Adjustment quantity cannot be zero.", List.of("Quantity is zero."));
        }

        try {
            InventoryItemDTO itemDto = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));

            int oldQty = itemDto.getQuantityOnHand();
            int newQty = oldQty + quantityChange;

            if (newQty < 0) {
                throw new InventoryItemValidationException("Resulting quantity on hand cannot be negative. Current: " + oldQty + ", Change: " + quantityChange, List.of("QOH would be negative."));
            }

            boolean success = inventoryItemRepository.adjustStockQuantity(inventoryItemId, quantityChange);

            if (success) {
                UserDTO currentUser = userSessionService.getCurrentUser();
                Integer userId = (currentUser != null) ? currentUser.getUserId() : null;

                // Construct a meaningful item name for the log
                String itemNameForLog = itemDto.getProductNameEn() != null ? itemDto.getProductNameEn() : "Item ID: " + itemDto.getInventoryItemId();
                if (itemDto.getItemSpecificNameEn() != null && !itemDto.getItemSpecificNameEn().isEmpty()) {
                    itemNameForLog += " - " + itemDto.getItemSpecificNameEn();
                }

                auditLogRepository.logStockAdjustment(inventoryItemId, itemNameForLog, quantityChange, oldQty, newQty, reason, userId);
                logger.info("Stock adjustment for item ID {} (Change: {}) processed successfully. Reason: {}", inventoryItemId, quantityChange, reason);
            } else {
                // This might indicate the item was deleted between fetch and update, or another concurrency issue.
                logger.error("Stock adjustment for item ID {} failed to update repository (item possibly not found or no change made).", inventoryItemId);
                throw new InventoryItemServiceException("Stock adjustment failed to apply in repository.");
            }

        } catch (InventoryItemNotFoundException | InventoryItemValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error performing stock adjustment for item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw new InventoryItemServiceException("Could not perform stock adjustment.", e);
        }
    }
}
