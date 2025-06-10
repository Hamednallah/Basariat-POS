package com.basariatpos.i18n;

import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
// Optional: For JavaFX property binding, uncomment these
// import javafx.beans.property.ObjectProperty;
// import javafx.beans.property.SimpleObjectProperty;

public class LocaleManager {

    private static final Logger logger = AppLogger.getLogger(LocaleManager.class);

    public static final Locale ARABIC = new Locale("ar");
    public static final Locale ENGLISH = Locale.ENGLISH; // More standard way to reference English

    public static final Locale DEFAULT_LOCALE = ARABIC;
    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(ARABIC, ENGLISH);

    private static Locale currentLocale = DEFAULT_LOCALE;

    // Optional: JavaFX property for binding UI elements dynamically
    // private static final ObjectProperty<Locale> currentLocaleProperty = new SimpleObjectProperty<>(DEFAULT_LOCALE);

    private LocaleManager() {
        // Private constructor to prevent instantiation
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    // public static ObjectProperty<Locale> currentLocaleProperty() {
    //     return currentLocaleProperty;
    // }

    public static void setCurrentLocale(Locale locale) {
        if (locale == null) {
            logger.warn("Attempted to set null locale. Retaining current locale: {}", currentLocale);
            return;
        }
        if (SUPPORTED_LOCALES.contains(locale)) {
            if (!currentLocale.equals(locale)) {
                currentLocale = locale;
                // currentLocaleProperty.set(locale); // Update JavaFX property
                logger.info("Application locale changed to: {}", locale.toLanguageTag());
                // Here, you might trigger an event if using an event bus for UI updates
            } else {
                logger.debug("Locale {} is already set. No change.", locale.toLanguageTag());
            }
        } else {
            logger.warn("Attempted to set an unsupported locale: {}. Supported locales are: {}. Retaining current locale: {}",
                        locale.toLanguageTag(), SUPPORTED_LOCALES, currentLocale.toLanguageTag());
        }
    }

    public static Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    public static List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    /**
     * Helper to get a supported locale by its language code.
     * @param languageCode e.g., "en", "ar"
     * @return The Locale object if supported, otherwise null.
     */
    public static Locale getLocaleByLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return null;
        }
        for (Locale locale : SUPPORTED_LOCALES) {
            if (locale.getLanguage().equalsIgnoreCase(languageCode)) {
                return locale;
            }
        }
        return null;
    }
}
