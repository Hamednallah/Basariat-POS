package com.basariatpos.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

class LocaleManagerTest {

    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        // Store original locale to restore it after tests
        originalLocale = LocaleManager.getCurrentLocale();
        // Ensure a known state before each test if necessary, e.g., reset to default
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
    }

    @Test
    void getDefaultLocale_shouldReturnArabic() {
        assertEquals(LocaleManager.ARABIC, LocaleManager.getDefaultLocale(), "Default locale should be Arabic.");
    }

    @Test
    void getCurrentLocale_shouldInitiallyBeDefaultLocale() {
        assertEquals(LocaleManager.DEFAULT_LOCALE, LocaleManager.getCurrentLocale(), "Current locale should initially be the default locale.");
    }

    @Test
    void setCurrentLocale_shouldChangeCurrentLocale_whenSupportedLocaleIsSet() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale(), "Current locale should be updated to English.");

        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        assertEquals(LocaleManager.ARABIC, LocaleManager.getCurrentLocale(), "Current locale should be updated to Arabic.");
    }

    @Test
    void setCurrentLocale_shouldNotChangeCurrentLocale_whenUnsupportedLocaleIsSet() {
        Locale unsupportedLocale = new Locale("fr", "FR");
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Known state

        LocaleManager.setCurrentLocale(unsupportedLocale);
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale(),
                     "Current locale should not change when an unsupported locale is set.");
    }

    @Test
    void setCurrentLocale_shouldNotChangeCurrentLocale_whenNullIsSet() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Known state
        LocaleManager.setCurrentLocale(null);
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale(),
                     "Current locale should not change when null is set.");
    }

    @Test
    void setCurrentLocale_shouldNotTriggerChange_whenSameLocaleIsSet() {
        // This test implies checking for side effects like logging or event firing,
        // which is harder without inspecting logs or mocking.
        // For now, we just ensure the locale remains the same.
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale());

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Set the same locale again
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale());
        // To properly test "no change notification", one might need an observable property or an event bus.
    }


    @Test
    void getSupportedLocales_shouldContainArabicAndEnglish() {
        assertTrue(LocaleManager.getSupportedLocales().contains(LocaleManager.ARABIC), "Supported locales should include Arabic.");
        assertTrue(LocaleManager.getSupportedLocales().contains(Locale.ENGLISH), "Supported locales should include English.");
        assertEquals(2, LocaleManager.getSupportedLocales().size(), "There should be exactly two supported locales.");
    }

    @Test
    void getLocaleByLanguageCode_shouldReturnCorrectLocale() {
        assertEquals(LocaleManager.ARABIC, LocaleManager.getLocaleByLanguageCode("ar"));
        assertEquals(Locale.ENGLISH, LocaleManager.getLocaleByLanguageCode("en"));
        assertEquals(LocaleManager.ARABIC, LocaleManager.getLocaleByLanguageCode("AR")); // Case-insensitivity
        assertEquals(Locale.ENGLISH, LocaleManager.getLocaleByLanguageCode("EN"));
    }

    @Test
    void getLocaleByLanguageCode_shouldReturnNullForUnsupportedCode() {
        assertNull(LocaleManager.getLocaleByLanguageCode("fr"));
    }

    @Test
    void getLocaleByLanguageCode_shouldReturnNullForNullOrEmptyCode() {
        assertNull(LocaleManager.getLocaleByLanguageCode(null));
        assertNull(LocaleManager.getLocaleByLanguageCode(""));
    }

    @AfterEach
    void tearDown() {
        // Restore original locale
        LocaleManager.setCurrentLocale(originalLocale);
    }
}
