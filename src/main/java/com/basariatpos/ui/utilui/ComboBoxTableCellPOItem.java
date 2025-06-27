package com.basariatpos.ui.utilui;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.StringConverter;

public class ComboBoxTableCellPOItem extends ComboBoxTableCell<PurchaseOrderItemDTO, InventoryItemDTO> {

    public ComboBoxTableCellPOItem(ObservableList<InventoryItemDTO> items) {
        super(new StringConverter<InventoryItemDTO>() {
            @Override
            public String toString(InventoryItemDTO item) {
                if (item == null) {
                    return null;
                }
                // Display a comprehensive name, perhaps code + specific name + brand
                StringBuilder sb = new StringBuilder();
                if (item.getProductCode() != null && !item.getProductCode().isEmpty()) {
                    sb.append("[").append(item.getProductCode()).append("] ");
                }
                sb.append(item.getDisplayFullNameEn()); // Assumes DTO has a good display name
                if (item.getBrandName() != null && !item.getBrandName().isEmpty()){
                    sb.append(" (").append(item.getBrandName()).append(")");
                }
                return sb.toString();
            }

            @Override
            public InventoryItemDTO fromString(String string) {
                // Not used if ComboBox is not editable directly for new entries
                return null;
            }
        }, items);
    }

    @Override
    public void updateItem(InventoryItemDTO item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // The StringConverter handles the text for the ComboBox when editing.
            // When not editing, this sets the cell's text.
            // We want to display the same representation.
            setText(super.getConverter().toString(item));
        }
    }
}
