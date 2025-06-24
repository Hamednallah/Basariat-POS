package com.basariatpos.ui.utilui;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TextFormatters {

    /**
     * Applies a formatter to allow only integer input, including negative numbers.
     * @param textField The TextField to apply the formatter to.
     */
    public static void applyIntegerFormatter(TextField textField) {
        Pattern validEditingState = Pattern.compile("-?(([1-9]\\d*)|0)?");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return change;
            }
            return null;
        };
        StringConverter<Integer> converter = new StringConverter<>() {
            @Override
            public Integer fromString(String s) {
                if (s.isEmpty() || "-".equals(s)) {
                    return 0;
                } else {
                    return Integer.valueOf(s);
                }
            }
            @Override
            public String toString(Integer d) {
                return d.toString();
            }
        };
        TextFormatter<Integer> formatter = new TextFormatter<>(converter, 0, filter);
        textField.setTextFormatter(formatter);
    }

    /**
     * Applies a formatter to allow only integer input for multiple fields.
     * @param textFields The TextFields to apply the formatter to.
     */
    public static void applyIntegerFormatter(TextField... textFields) {
        for (TextField tf : textFields) {
            applyIntegerFormatter(tf);
        }
    }

    /**
     * Applies a formatter to allow decimal input (e.g., for prices, quantities with decimals).
     * Allows up to 2 decimal places.
     * @param textField The TextField to apply the formatter to.
     */
    public static void applyBigDecimalFormatter(TextField textField) {
        Pattern validEditingState = Pattern.compile("-?(([1-9]\\d*)|0)?(\\.\\d{0,2})?");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return change;
            }
            return null;
        };

        StringConverter<BigDecimal> converter = new StringConverter<>() {
            private final DecimalFormat df = new DecimalFormat("#,##0.00");

            @Override
            public String toString(BigDecimal value) {
                if (value == null) {
                    return "";
                }
                return df.format(value);
            }

            @Override
            public BigDecimal fromString(String value) {
                if (value == null || value.isEmpty() || "-".equals(value) || ".".equals(value) || "-.".equals(value)) {
                    return BigDecimal.ZERO;
                }
                try {
                    // Standard parsing for internal logic
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }
        };
        TextFormatter<BigDecimal> formatter = new TextFormatter<>(converter, BigDecimal.ZERO, filter);
        textField.setTextFormatter(formatter);
        // Set default text for alignment and visual cue if field is empty initially
        if (textField.getText() == null || textField.getText().isEmpty()){
             // textField.setText("0.00"); // Or handle this in prompt text
        }
    }

    /**
     * Applies a BigDecimal formatter to multiple TextFields.
     * @param textFields The TextFields to apply the formatter to.
     */
    public static void applyBigDecimalFormatter(TextField... textFields) {
        for (TextField tf : textFields) {
            applyBigDecimalFormatter(tf);
        }
    }

    /**
     * Parses a BigDecimal from a TextField's text, returning a default if parsing fails or text is empty.
     * @param text The string to parse.
     * @param defaultValue The value to return if parsing fails or text is empty.
     * @return The parsed BigDecimal or the defaultValue.
     */
    public static BigDecimal parseBigDecimal(String text, BigDecimal defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static BigDecimal parseBigDecimal(String text) {
        return parseBigDecimal(text, null); // Null default if not specified
    }


    /**
     * Parses an Integer from a TextField's text, returning null if parsing fails or text is empty.
     * @param text The string to parse.
     * @return The parsed Integer or null.
     */
    public static Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null; // Or throw, or return a default like 0, depending on requirements
        }
    }
}
