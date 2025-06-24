package com.basariatpos.service;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.exception.WhatsAppNotificationException;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class WhatsAppNotificationServiceImpl implements WhatsAppNotificationService {

    private static final Logger logger = AppLogger.getLogger(WhatsAppNotificationServiceImpl.class);

    private final ApplicationSettingsService applicationSettingsService;
    private final LocaleManager localeManager;
    private final CenterProfileService centerProfileService;

    // Basic regex: optional +, followed by digits. Min length 7 (e.g. 1234567), Max 15.
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");


    public WhatsAppNotificationServiceImpl(ApplicationSettingsService applicationSettingsService,
                                           LocaleManager localeManager,
                                           CenterProfileService centerProfileService) {
        this.applicationSettingsService = applicationSettingsService;
        this.localeManager = localeManager;
        this.centerProfileService = centerProfileService;
    }

    @Override
    public String generateClickToChatLink(String phoneNumber, String messageContextType, Map<String, Object> contextData)
            throws WhatsAppNotificationException {

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new WhatsAppNotificationException("Phone number cannot be empty.");
        }
        String cleanedPhoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // Remove non-digits
        if (cleanedPhoneNumber.isEmpty() || !PHONE_NUMBER_PATTERN.matcher(phoneNumber.trim()).matches()) { // Validate original or cleaned
            throw new WhatsAppNotificationException("Invalid phone number format: " + phoneNumber);
        }
        // For wa.me links, it's often best to remove leading '+' if present, and ensure only digits.
        // Some countries might need their leading '0' removed if country code is implied or added separately.
        // For simplicity, we'll assume cleanedPhoneNumber is mostly ready or international format.


        String currentLang = localeManager.getCurrentLocale().getLanguage();
        String templateKey = String.format("whatsapp.template.%s_%s",
                                           messageContextType.toLowerCase(),
                                           currentLang);

        Optional<String> templateOpt = applicationSettingsService.getStringValue(templateKey);
        if (templateOpt.isEmpty()) {
            logger.error("WhatsApp template not found for key: {}", templateKey);
            throw new WhatsAppNotificationException(
                MessageProvider.getString("salesorder.notify.error.templateMissing", currentLang)
            );
        }
        String messageTemplate = templateOpt.get();

        // Add CenterName to context if not already present
        if (!contextData.containsKey("[CenterName]")) {
            try {
                Optional<CenterProfileDTO> profileOpt = centerProfileService.getCenterProfile();
                if (profileOpt.isPresent()) {
                    contextData.put("[CenterName]", profileOpt.get().getCenterName());
                } else {
                    contextData.put("[CenterName]", "Our Center"); // Fallback
                }
            } catch (Exception e) {
                logger.warn("Could not fetch center name for WhatsApp template, using fallback.", e);
                contextData.put("[CenterName]", "Our Center");
            }
        }

        String message = replacePlaceholders(messageTemplate, contextData);

        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            // Ensure phone number for wa.me link is digits only, typically without leading '+' or '00'
            // Some systems might require the country code. This might need refinement based on specific number formats.
            String waPhoneNumber = cleanedPhoneNumber; // Use the digit-only version

            String link = String.format("https://wa.me/%s?text=%s", waPhoneNumber, encodedMessage);
            logger.info("Generated WhatsApp link for phone {}: {}", waPhoneNumber, link);
            return link;
        } catch (UnsupportedEncodingException e) {
            logger.error("Error URL encoding WhatsApp message: {}", e.getMessage(), e);
            throw new WhatsAppNotificationException("Failed to generate notification link due to encoding error.", e);
        }
    }

    private String replacePlaceholders(String template, Map<String, Object> contextData) {
        String result = template;
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            String placeholder = entry.getKey(); // e.g., "[PatientName]"
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
