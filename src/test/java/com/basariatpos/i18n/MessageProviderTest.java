package com.basariatpos.i18n;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class MessageProviderTest {

    private static final String TEST_BUNDLE_BASE_NAME = "com.basariatpos.i18n.test_messages";
    private static String originalBundleBaseName;
    private Locale originalLocale;


    @BeforeAll
    static void backupOriginalsAndSetTestBundle() throws Exception {
        // Backup original base name from MessageProvider
        Field bundleNameField = MessageProvider.class.getDeclaredField("RESOURCE_BUNDLE_BASE_NAME");
        bundleNameField.setAccessible(true);
        originalBundleBaseName = (String) bundleNameField.get(null); // Get static field value

        // Set to test bundle base name
        Field modifiersField = Field.class.getDeclaredField("modifiers"); // Need to remove "final" modifier
        modifiersField.setAccessible(true);
        modifiersField.setInt(bundleNameField, bundleNameField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

        bundleNameField.set(null, TEST_BUNDLE_BASE_NAME);
    }

    @AfterAll
    static void restoreOriginals() throws Exception {
        // Restore original base name
        if (originalBundleBaseName != null) {
            Field bundleNameField = MessageProvider.class.getDeclaredField("RESOURCE_BUNDLE_BASE_NAME");
            bundleNameField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(bundleNameField, bundleNameField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            bundleNameField.set(null, originalBundleBaseName);
        }
        // Restore original locale for LocaleManager to avoid side effects on other tests
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
    }

    @BeforeEach
    void setUp() {
        // Store and set locale for each test
        originalLocale = LocaleManager.getCurrentLocale();
        // Clear cache in ResourceBundle to ensure fresh loading for locale changes
        ResourceBundle.clearCache();
    }


    @Test
    void getString_shouldReturnCorrectString_forEnglishLocale() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertEquals("Hello Test", MessageProvider.getString("test.greeting"), "English greeting should be correct.");
        assertEquals("Goodbye Test", MessageProvider.getString("test.farewell"), "English farewell should be correct.");
    }

    @Test
    void getString_shouldReturnCorrectString_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        assertEquals("مرحبا اختبار", MessageProvider.getString("test.greeting"), "Arabic greeting should be correct.");
        assertEquals("وداعا اختبار", MessageProvider.getString("test.farewell"), "Arabic farewell should be correct.");
    }

    @Test
    void getString_shouldReturnPlaceholder_forMissingKey() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertEquals("!missing.key!", MessageProvider.getString("missing.key"), "Should return placeholder for missing key.");
    }

    @Test
    void getString_shouldHandleLocaleSpecificKey() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertEquals("This is only in English.", MessageProvider.getString("test.only.in.english"));
        assertEquals("!test.only.in.arabic!", MessageProvider.getString("test.only.in.arabic")); // Should not find Arabic key in English bundle

        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        assertEquals("هذا فقط باللغة العربية.", MessageProvider.getString("test.only.in.arabic"));
        assertEquals("!test.only.in.english!", MessageProvider.getString("test.only.in.english")); // Should not find English key in Arabic bundle
    }

    @Test
    void getString_shouldReturnPlaceholder_forNullKey() {
        assertEquals("!NULL_KEY!", MessageProvider.getString(null));
    }

    @Test
    void getBundle_shouldReturnCorrectBundle_forCurrentLocale() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        ResourceBundle bundleEn = MessageProvider.getBundle();
        assertNotNull(bundleEn);
        assertEquals("Hello Test", bundleEn.getString("test.greeting"));

        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        ResourceBundle bundleAr = MessageProvider.getBundle();
        assertNotNull(bundleAr);
        assertEquals("مرحبا اختبار", bundleAr.getString("test.greeting"));
    }

    @Test
    void getString_shouldUseDefaultLocale_ifCurrentLocaleIsNullInManager() {
        // Simulate LocaleManager returning null (though our current one doesn't allow setting null)
        // This test is more conceptual for robustness if LocaleManager could somehow have a null currentLocale.
        // For now, we test by explicitly passing null to getString(key, locale)
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Known state
        assertEquals("Hello Test", MessageProvider.getString("test.greeting", Locale.ENGLISH));

        // If MessageProvider.getString(key) was called and LocaleManager.currentLocale was null,
        // it should fall back to LocaleManager.DEFAULT_LOCALE (Arabic)
        // To test this path correctly, one would need to mock LocaleManager or ensure it can return null.
        // The current MessageProvider.getString(key, locale) handles null locale by using default.
        assertEquals("مرحبا اختبار", MessageProvider.getString("test.greeting", null),
                     "Should use default (Arabic) bundle if null locale is passed.");
    }

    @Test
    void getBundle_shouldThrowMissingResourceException_forInvalidBundleName() throws Exception {
        // Temporarily set an invalid bundle name to test this
        Field bundleNameField = MessageProvider.class.getDeclaredField("RESOURCE_BUNDLE_BASE_NAME");
        bundleNameField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(bundleNameField, bundleNameField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        bundleNameField.set(null, "invalid.bundle.name");

        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertThrows(MissingResourceException.class, () -> {
            MessageProvider.getBundle();
        }, "Should throw MissingResourceException for an invalid bundle name.");

        // Restore test bundle name for other tests
        bundleNameField.set(null, TEST_BUNDLE_BASE_NAME);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore original locale in LocaleManager
        if (originalLocale != null) {
            LocaleManager.setCurrentLocale(originalLocale);
        }
        ResourceBundle.clearCache(); // Clear cache again after test
    }
}
