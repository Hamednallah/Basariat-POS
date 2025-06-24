package com.basariatpos.service;

import com.basariatpos.service.exception.WhatsAppNotificationException;
import java.util.Map;

public interface WhatsAppNotificationService {

    /**
     * Generates a WhatsApp "click to chat" link with a pre-filled message.
     *
     * @param phoneNumber The recipient's phone number (should be in international format, e.g., +1XXXXXXXXXX or XXXXXXXXXX which will be prefixed).
     * @param messageContextType A key to identify the message template (e.g., "ORDER_READY", "APPOINTMENT_REMINDER").
     * @param contextData A map containing data to replace placeholders in the template (e.g., "[PatientName]", "[OrderID]").
     * @return The generated https://wa.me/ link.
     * @throws WhatsAppNotificationException if the phone number is invalid, template is missing, or an error occurs during link generation.
     */
    String generateClickToChatLink(String phoneNumber, String messageContextType, Map<String, Object> contextData)
            throws WhatsAppNotificationException;
}
