package com.basariatpos.i18n;

import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageProvider {

    private static final Logger logger = AppLogger.getLogger(MessageProvider.class);
    public static final String RESOURCE_BUNDLE_BASE_NAME = "com.basariatpos.i18n.messages";

    private MessageProvider() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the string for the given key from the resource bundle for the current locale.
     *
     * @param key The key for the desired string.
     * @return The string for the given key, or a placeholder like "!key!" if not found.
     */
    public static String getString(String key) {
        return getString(key, LocaleManager.getCurrentLocale());
    }

    /**
     * Gets the string for the given key from the resource bundle for the specified locale.
     *
     * @param key    The key for the desired string.
     * @param locale The locale for which to get the string.
     * @return The string for the given key, or a placeholder like "!key!" if not found.
     */
    public static String getString(String key, Locale locale) {
        if (key == null) {
            logger.warn("Attempted to get message for a null key.");
            return "!NULL_KEY!";
        }
        if (locale == null) {
            logger.warn("Attempted to get message for key '{}' with a null locale. Using default locale.", key);
            locale = LocaleManager.getDefaultLocale();
        }

        try {
            ResourceBundle bundle = getBundle(locale);
            if (bundle == null) { // Should not happen if getBundle handles its errors
                return "!" + key + " (Bundle Error)!";
            }
            String value = bundle.getString(key);
            // Properties files are ISO-8859-1 by default by ResourceBundle.
            // If your .properties files are UTF-8, and you're not using Java 9+ with PropertiesResourceBundleControl,
            // you might need manual conversion. However, modern IDEs and Java versions handle UTF-8 better.
            // Let's assume UTF-8 is handled correctly by the environment or ResourceBundle.Control if ever implemented.
            // For Java 8, if issues arise, one might need a custom ResourceBundle.Control.
            return value;
        } catch (MissingResourceException e) {
            logger.error("Missing resource for key '{}' in locale '{}'. Bundle base name: {}", key, locale.toLanguageTag(), RESOURCE_BUNDLE_BASE_NAME, e);
            return "!" + key + "!";
        } catch (Exception e) {
            logger.error("Error retrieving string for key '{}' in locale '{}': {}", key, locale.toLanguageTag(), e.getMessage(), e);
            return "!" + key + " (Error)!";
        }
    }

    /**
     * Gets the resource bundle for the current locale.
     *
     * @return The resource bundle for the current locale.
     */
    public static ResourceBundle getBundle() {
        return getBundle(LocaleManager.getCurrentLocale());
    }

    /**
     * Gets the resource bundle for the specified locale.
     *
     * @param locale The locale for which to get the resource bundle.
     * @return The resource bundle for the specified locale.
     * @throws MissingResourceException if the bundle cannot be found.
     */
    public static ResourceBundle getBundle(Locale locale) throws MissingResourceException {
        if (locale == null) {
            logger.warn("Attempted to get bundle with a null locale. Using default locale.");
            locale = LocaleManager.getDefaultLocale();
        }
        try {
            // Standard Java 8 ResourceBundle loads properties files in ISO-8859-1.
            // If properties files are UTF-8, and you are on Java 9+, it's handled automatically.
            // For Java 8, to correctly load UTF-8 properties files, a custom Control is needed.
            // For simplicity in Sprint 0, we'll rely on the native2ascii-converted characters (\uXXXX)
            // or assume the environment handles UTF-8 correctly.
            // If characters show up garbled, a ResourceBundle.Control would be the fix for Java 8.
            return ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, locale);
        } catch (MissingResourceException e) {
            logger.error("Resource bundle not found for base name '{}' and locale '{}'.", RESOURCE_BUNDLE_BASE_NAME, locale.toLanguageTag(), e);
            throw e; // Re-throw to allow caller to handle, e.g., by returning a placeholder
        }
    }

    // Example of how one might use a UTF-8 Control (Java 8 specific, for Java 9+ it's automatic)
    // This would replace ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, locale)
    // with ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, locale, new UTF8Control())
    /*
    public static class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Read properties files by UTF-8 encoding
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
    */
}
