package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.model.form.LensAttributes;
import com.basariatpos.model.form.PrescriptionData;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.ui.utilui.TextFormatters; // Assuming a utility class for formatters
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
// Manual JSON handling imports (alternative to Jackson)
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.util.stream.Collectors;
// import java.util.Map;
// import java.util.HashMap;


public class CustomLensConfigDialogController {

    private static final Logger logger = AppLogger.getLogger(CustomLensConfigDialogController.class);

    @FXML private DialogPane dialogPane; // Used as root for RTL
    // Prescription Fields
    @FXML private TextField odSphField; @FXML private TextField odCylField; @FXML private TextField odAxisField; @FXML private TextField odAddField;
    @FXML private TextField osSphField; @FXML private TextField osCylField; @FXML private TextField osAxisField; @FXML private TextField osAddField;
    @FXML private TextField ipdField;

    // Lens Attributes
    @FXML private ComboBox<String> materialComboBox;
    @FXML private ComboBox<String> shadeComboBox;
    @FXML private ComboBox<String> reflectionTypeComboBox;
    @FXML private TextField unitPriceField;

    private Stage dialogStage;
    private SalesOrderItemDTO currentLensItem;
    private boolean okClicked = false;

    // Keys for ComboBox options
    private final String MATERIAL_GLASS = MessageProvider.getString("customlens.material.glass");
    private final String MATERIAL_PLASTIC = MessageProvider.getString("customlens.material.plastic");
    private final String MATERIAL_POLY = MessageProvider.getString("customlens.material.polycarbonate");
    private final String MATERIAL_BLUECUT = MessageProvider.getString("customlens.material.bluecut");

    private final String SHADE_WHITE = MessageProvider.getString("customlens.shade.white");
    private final String SHADE_PHOTO = MessageProvider.getString("customlens.shade.photochromic");
    private final String SHADE_TINTED = MessageProvider.getString("customlens.shade.tinted");

    private final String REFLECTION_UNCOATED = MessageProvider.getString("customlens.reflection.uncoated");
    private final String REFLECTION_AR = MessageProvider.getString("customlens.reflection.antireflective");

    public void initialize() { // Called by FXML loader
        populateComboBoxes();
        addNumericFormatters();

        materialComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (MATERIAL_BLUECUT.equals(newVal)) {
                reflectionTypeComboBox.setValue(REFLECTION_AR);
                reflectionTypeComboBox.setDisable(true);
            } else {
                reflectionTypeComboBox.setDisable(false);
            }
        });

        // Setup to handle OK button click from DialogPane
        dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, this::handleOkAction);
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (dialogPane != null) { // DialogPane is the root
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                dialogPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                dialogPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("customLensDialogPane (DialogPane root) is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void addNumericFormatters() {
        TextFormatters.applyBigDecimalFormatter(odSphField, osSphField, odCylField, osCylField, odAddField, osAddField, ipdField, unitPriceField);
        TextFormatters.applyIntegerFormatter(odAxisField, osAxisField);
    }


    public void initializeDialog(Stage stage, SalesOrderItemDTO lensItem) {
        this.dialogStage = stage; // Though DialogPane manages its own window, good to have reference if needed
        this.currentLensItem = lensItem;
        this.okClicked = false;

        updateNodeOrientation(); // Ensure orientation when dialog is fully set up

        if (lensItem.getPrescriptionDetails() != null && !lensItem.getPrescriptionDetails().isEmpty()) {
            parseAndSetPrescriptionDetailsGson(lensItem.getPrescriptionDetails());
        }
        if (lensItem.getUnitPrice() != null) {
            unitPriceField.setText(lensItem.getUnitPrice().toPlainString());
        } else {
            unitPriceField.setText("0.00");
        }
    }

    private void populateComboBoxes() {
        materialComboBox.setItems(FXCollections.observableArrayList(MATERIAL_GLASS, MATERIAL_PLASTIC, MATERIAL_POLY, MATERIAL_BLUECUT));
        shadeComboBox.setItems(FXCollections.observableArrayList(SHADE_WHITE, SHADE_PHOTO, SHADE_TINTED));
        reflectionTypeComboBox.setItems(FXCollections.observableArrayList(REFLECTION_UNCOATED, REFLECTION_AR));
    }

    private void parseAndSetPrescriptionDetailsGson(String jsonDetails) {
        try {
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(jsonDetails, JsonObject.class);

            if (root.has("rx")) {
                JsonObject rxJson = root.getAsJsonObject("rx");
                PrescriptionData rxData = gson.fromJson(rxJson, PrescriptionData.class);
                odSphField.setText(bdToString(rxData.getOdSph()));
                odCylField.setText(bdToString(rxData.getOdCyl()));
                odAxisField.setText(intToString(rxData.getOdAxis()));
                osSphField.setText(bdToString(rxData.getOsSph()));
                osCylField.setText(bdToString(rxData.getOsCyl()));
                osAxisField.setText(intToString(rxData.getOsAxis()));
                odAddField.setText(bdToString(rxData.getOdAdd()));
                osAddField.setText(bdToString(rxData.getOsAdd()));
                ipdField.setText(bdToString(rxData.getIpd()));
            }

            if (root.has("attrs")) {
                JsonObject attrsJson = root.getAsJsonObject("attrs");
                LensAttributes attrsData = gson.fromJson(attrsJson, LensAttributes.class);
                materialComboBox.setValue(attrsData.getMaterial());
                shadeComboBox.setValue(attrsData.getShade());
                reflectionTypeComboBox.setValue(attrsData.getReflectionType());
            }
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing prescription JSON with Gson: {}", jsonDetails, e);
            AlertUtil.showError("Data Error", "Could not parse existing lens details (JSON format error).");
        } catch (Exception e) {
            logger.error("Unexpected error parsing prescription JSON with Gson: {}", jsonDetails, e);
            AlertUtil.showError("Data Error", "Could not parse existing lens details.");
        }
    }

    private void handleOkAction(ActionEvent event) {
        if (!validateInputs()) {
            event.consume(); // Prevent dialog from closing
            return;
        }

        PrescriptionData rxData = new PrescriptionData();
        rxData.setOdSph(TextFormatters.parseBigDecimal(odSphField.getText()));
        rxData.setOdCyl(TextFormatters.parseBigDecimal(odCylField.getText()));
        rxData.setOdAxis(TextFormatters.parseInteger(odAxisField.getText()));
        rxData.setOdAdd(TextFormatters.parseBigDecimal(odAddField.getText()));
        rxData.setOsSph(TextFormatters.parseBigDecimal(osSphField.getText()));
        rxData.setOsCyl(TextFormatters.parseBigDecimal(osCylField.getText()));
        rxData.setOsAxis(TextFormatters.parseInteger(osAxisField.getText()));
        rxData.setOsAdd(TextFormatters.parseBigDecimal(osAddField.getText()));
        rxData.setIpd(TextFormatters.parseBigDecimal(ipdField.getText()));

        LensAttributes attributes = new LensAttributes();
        attributes.setMaterial(materialComboBox.getValue());
        attributes.setShade(shadeComboBox.getValue());
        attributes.setReflectionType(reflectionTypeComboBox.getValue());

        Gson gson = new Gson();
        JsonObject root = new JsonObject();
        root.add("rx", gson.toJsonTree(rxData));
        root.add("attrs", gson.toJsonTree(attributes));
        String fullJson = gson.toJson(root);

        currentLensItem.setPrescriptionDetails(fullJson);
        currentLensItem.setUnitPrice(TextFormatters.parseBigDecimal(unitPriceField.getText(), BigDecimal.ZERO));
        currentLensItem.setIsCustomLenses(true);
        currentLensItem.setDescription(MessageProvider.getString("salesorder.item.display.customLens") +
                                       " (" + (attributes.getMaterial() != null ? attributes.getMaterial() : "") + ")");
        currentLensItem.setItemDisplayNameEn(MessageProvider.getString("salesorder.itemtype.customlens"));

        okClicked = true;
    }

    private String bdToString(BigDecimal bd) { return bd == null ? "" : bd.toPlainString(); }
    private String intToString(Integer i) { return i == null ? "" : i.toString(); }
    // private String strToString(String s) { return s == null ? "" : s; } // Not needed with Gson handling nulls

    private boolean validateInputs() {
        List<String> errors = new ArrayList<>();
        // Validate Rx Fields (ensure they are valid numbers if not empty)
        validateNumericField(odSphField, "OD SPH", errors, false);
        validateNumericField(odCylField, "OD CYL", errors, false);
        validateIntegerField(odAxisField, "OD AXIS", errors, false);
        // ... (similar for OS and ADD fields) ...
        validateNumericField(ipdField, "IPD", errors, false);

        if (materialComboBox.getValue() == null) {
            errors.add(MessageProvider.getString("customlens.error.materialRequired"));
        }

        BigDecimal price = null;
        try {
            price = new BigDecimal(unitPriceField.getText());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(MessageProvider.getString("customlens.error.priceInvalid"));
            }
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("customlens.error.priceInvalid"));
        }

        if (!errors.isEmpty()) {
            AlertUtil.showValidationError(errors);
            return false;
        }
        return true;
    }

    private void validateNumericField(TextField field, String fieldName, List<String> errors, boolean required) {
        String content = field.getText();
        if (required && (content == null || content.trim().isEmpty())) {
            errors.add(fieldName + " is required.");
            return;
        }
        if (content != null && !content.trim().isEmpty()) {
            try {
                new BigDecimal(content);
            } catch (NumberFormatException e) {
                errors.add(fieldName + " must be a valid number.");
            }
        }
    }
    private void validateIntegerField(TextField field, String fieldName, List<String> errors, boolean required) {
         String content = field.getText();
        if (required && (content == null || content.trim().isEmpty())) {
            errors.add(fieldName + " is required.");
            return;
        }
        if (content != null && !content.trim().isEmpty()) {
            try {
                Integer.parseInt(content);
            } catch (NumberFormatException e) {
                errors.add(fieldName + " must be a valid whole number.");
            }
        }
    }


    public SalesOrderItemDTO getUpdatedLensItem() {
        return okClicked ? currentLensItem : null;
    }
}
