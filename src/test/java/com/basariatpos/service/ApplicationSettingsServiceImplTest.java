package com.basariatpos.service;

import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.repository.ApplicationSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSettingsServiceImplTest {

    @Mock
    private ApplicationSettingsRepository mockSettingsRepository;

    @InjectMocks
    private ApplicationSettingsServiceImpl settingsService;

    private ApplicationSettingDTO testSettingDto;

    @BeforeEach
    void setUp() {
        testSettingDto = new ApplicationSettingDTO("app.name", "Basariat POS", "Application Name");
    }

    @Test
    void constructor_nullRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ApplicationSettingsServiceImpl(null));
    }

    // --- getApplicationSetting ---
    @Test
    void getApplicationSetting_keyExists_returnsDto() {
        when(mockSettingsRepository.findByKey("app.name")).thenReturn(Optional.of(testSettingDto));
        Optional<ApplicationSettingDTO> result = settingsService.getApplicationSetting("app.name");
        assertTrue(result.isPresent());
        assertEquals("Basariat POS", result.get().getSettingValue());
    }

    // --- getSettingValue ---
    @Test
    void getSettingValue_keyExists_returnsValue() {
        when(mockSettingsRepository.findByKey("app.name")).thenReturn(Optional.of(testSettingDto));
        String value = settingsService.getSettingValue("app.name", "Default");
        assertEquals("Basariat POS", value);
    }

    @Test
    void getSettingValue_keyNotExists_returnsDefaultValue() {
        when(mockSettingsRepository.findByKey("nonexistent.key")).thenReturn(Optional.empty());
        String value = settingsService.getSettingValue("nonexistent.key", "DefaultValue");
        assertEquals("DefaultValue", value);
    }

    // --- getIntSettingValue ---
    @Test
    void getIntSettingValue_validInt_returnsIntValue() {
        ApplicationSettingDTO intSetting = new ApplicationSettingDTO("items.per.page", "25", "");
        when(mockSettingsRepository.findByKey("items.per.page")).thenReturn(Optional.of(intSetting));
        int value = settingsService.getIntSettingValue("items.per.page", 10);
        assertEquals(25, value);
    }

    @Test
    void getIntSettingValue_invalidInt_returnsDefault() {
        ApplicationSettingDTO invalidIntSetting = new ApplicationSettingDTO("items.per.page", "not-an-int", "");
        when(mockSettingsRepository.findByKey("items.per.page")).thenReturn(Optional.of(invalidIntSetting));
        int value = settingsService.getIntSettingValue("items.per.page", 10);
        assertEquals(10, value);
    }

    // --- getBooleanSettingValue ---
    @Test
    void getBooleanSettingValue_validTrue_returnsTrue() {
        ApplicationSettingDTO boolSetting = new ApplicationSettingDTO("feature.enabled", "true", "");
        when(mockSettingsRepository.findByKey("feature.enabled")).thenReturn(Optional.of(boolSetting));
        boolean value = settingsService.getBooleanSettingValue("feature.enabled", false);
        assertTrue(value);
    }
     @Test
    void getBooleanSettingValue_anyNonTrueString_returnsFalse() {
        ApplicationSettingDTO boolSetting = new ApplicationSettingDTO("feature.enabled", "Enabled", ""); // Not "true"
        when(mockSettingsRepository.findByKey("feature.enabled")).thenReturn(Optional.of(boolSetting));
        boolean value = settingsService.getBooleanSettingValue("feature.enabled", true); // Default true
        assertFalse(value); // Boolean.parseBoolean("Enabled") is false
    }


    // --- updateSettingValue ---
    @Test
    void updateSettingValue_keyExists_updatesAndSaves() throws Exception {
        when(mockSettingsRepository.findByKey("app.name")).thenReturn(Optional.of(testSettingDto));
        when(mockSettingsRepository.save(any(ApplicationSettingDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        settingsService.updateSettingValue("app.name", "New App Name");

        ArgumentCaptor<ApplicationSettingDTO> captor = ArgumentCaptor.forClass(ApplicationSettingDTO.class);
        verify(mockSettingsRepository).save(captor.capture());
        assertEquals("New App Name", captor.getValue().getSettingValue());
        assertEquals("app.name", captor.getValue().getSettingKey());
    }

    @Test
    void updateSettingValue_keyNotExists_throwsSettingNotFoundException() {
        when(mockSettingsRepository.findByKey("unknown.key")).thenReturn(Optional.empty());
        assertThrows(SettingNotFoundException.class, () -> {
            settingsService.updateSettingValue("unknown.key", "anyValue");
        });
    }

    @Test
    void updateSettingValue_valueTooLong_throwsValidationException() {
        String longValue = "a".repeat(2000); // Assuming max 1024
        when(mockSettingsRepository.findByKey("app.name")).thenReturn(Optional.of(testSettingDto));

        assertThrows(ValidationException.class, () -> {
            settingsService.updateSettingValue("app.name", longValue);
        });
    }


    // --- getAllApplicationSettings ---
    @Test
    void getAllApplicationSettings_callsRepositoryFindAll() {
        List<ApplicationSettingDTO> list = new ArrayList<>();
        list.add(testSettingDto);
        when(mockSettingsRepository.findAll()).thenReturn(list);

        List<ApplicationSettingDTO> result = settingsService.getAllApplicationSettings();

        assertEquals(1, result.size());
        verify(mockSettingsRepository).findAll();
    }

    // --- saveApplicationSetting ---
    @Test
    void saveApplicationSetting_validDto_callsRepositorySave() throws Exception {
        ApplicationSettingDTO newSetting = new ApplicationSettingDTO("new.key", "new.value", "desc");
        when(mockSettingsRepository.save(newSetting)).thenReturn(newSetting);

        ApplicationSettingDTO result = settingsService.saveApplicationSetting(newSetting);

        assertNotNull(result);
        assertEquals("new.key", result.getSettingKey());
        verify(mockSettingsRepository).save(newSetting);
    }
}
