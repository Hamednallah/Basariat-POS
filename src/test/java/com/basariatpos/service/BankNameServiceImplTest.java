package com.basariatpos.service;

import com.basariatpos.model.BankNameDTO;
import com.basariatpos.repository.BankNameRepository;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankNameServiceImplTest {

    @Mock
    private BankNameRepository mockBankNameRepository;

    @InjectMocks
    private BankNameServiceImpl bankNameService;

    private BankNameDTO testBankDto;

    @BeforeEach
    void setUp() {
        testBankDto = new BankNameDTO(1, "Test Bank EN", "Test Bank AR", true);
    }

    @Test
    void constructor_nullRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new BankNameServiceImpl(null));
    }

    // --- getBankNameById ---
    @Test
    void getBankNameById_exists_returnsDto() {
        when(mockBankNameRepository.findById(1)).thenReturn(Optional.of(testBankDto));
        Optional<BankNameDTO> result = bankNameService.getBankNameById(1);
        assertTrue(result.isPresent());
        assertEquals(testBankDto.getBankNameEn(), result.get().getBankNameEn());
    }

    @Test
    void getBankNameById_notExists_returnsEmpty() {
        when(mockBankNameRepository.findById(99)).thenReturn(Optional.empty());
        Optional<BankNameDTO> result = bankNameService.getBankNameById(99);
        assertFalse(result.isPresent());
    }

    // --- getAllBankNames ---
    @Test
    void getAllBankNames_callsRepositoryFindAll() {
        List<BankNameDTO> list = new ArrayList<>();
        list.add(testBankDto);
        when(mockBankNameRepository.findAll(true)).thenReturn(list);

        List<BankNameDTO> result = bankNameService.getAllBankNames(true);

        assertEquals(1, result.size());
        verify(mockBankNameRepository).findAll(true);
    }

    // --- getActiveBankNames ---
    @Test
    void getActiveBankNames_callsRepositoryFindAllWithFalse() {
        List<BankNameDTO> list = new ArrayList<>();
        list.add(testBankDto); // Assume this would be filtered by repo if it were inactive
        when(mockBankNameRepository.findAll(false)).thenReturn(list);

        List<BankNameDTO> result = bankNameService.getActiveBankNames();

        assertEquals(1, result.size());
        verify(mockBankNameRepository).findAll(false);
    }


    // --- saveBankName ---
    @Test
    void saveBankName_newValidBank_returnsSavedDto() throws Exception {
        BankNameDTO newBank = new BankNameDTO("New EN", "New AR", true);
        BankNameDTO savedBank = new BankNameDTO(100, "New EN", "New AR", true);

        when(mockBankNameRepository.findByNameEn("New EN")).thenReturn(Optional.empty());
        when(mockBankNameRepository.findByNameAr("New AR")).thenReturn(Optional.empty());
        when(mockBankNameRepository.save(any(BankNameDTO.class))).thenReturn(savedBank);

        BankNameDTO result = bankNameService.saveBankName(newBank);

        assertNotNull(result);
        assertEquals(100, result.getBankNameId());
        verify(mockBankNameRepository).save(newBank);
    }

    @Test
    void saveBankName_editExistingBank_returnsSavedDto() throws Exception {
        testBankDto.setBankNameEn("Updated EN"); // User changes English name

        // Mock finds this DTO by ID, but not by the "Updated EN" name yet
        when(mockBankNameRepository.findByNameEn("Updated EN")).thenReturn(Optional.empty());
        // Mock finds this DTO by Arabic name (assuming it's not changed, or if it is, mock that too)
        when(mockBankNameRepository.findByNameAr(testBankDto.getBankNameAr())).thenReturn(Optional.of(testBankDto));

        when(mockBankNameRepository.save(testBankDto)).thenReturn(testBankDto); // Save returns the same DTO (or one with same ID)

        BankNameDTO result = bankNameService.saveBankName(testBankDto);

        assertNotNull(result);
        assertEquals(testBankDto.getBankNameId(), result.getBankNameId());
        assertEquals("Updated EN", result.getBankNameEn());
        verify(mockBankNameRepository).save(testBankDto);
    }


    @Test
    void saveBankName_englishNameExists_throwsBankNameAlreadyExistsException() {
        BankNameDTO newBank = new BankNameDTO("Test Bank EN", "Unique AR", true);
        when(mockBankNameRepository.findByNameEn("Test Bank EN")).thenReturn(Optional.of(testBankDto)); // testBankDto has ID 1

        assertThrows(BankNameAlreadyExistsException.class, () -> {
            bankNameService.saveBankName(newBank); // newBank has ID 0
        });
    }

    @Test
    void saveBankName_arabicNameExists_throwsBankNameAlreadyExistsException() {
        BankNameDTO newBank = new BankNameDTO("Unique EN", "Test Bank AR", true);
        when(mockBankNameRepository.findByNameEn("Unique EN")).thenReturn(Optional.empty());
        when(mockBankNameRepository.findByNameAr("Test Bank AR")).thenReturn(Optional.of(testBankDto));

        assertThrows(BankNameAlreadyExistsException.class, () -> {
            bankNameService.saveBankName(newBank);
        });
    }


    @Test
    void saveBankName_emptyEnglishName_throwsValidationException() {
        BankNameDTO invalidBank = new BankNameDTO("", "Valid AR", true);
        // MessageProvider might not be available here, so check for generic ValidationException
        assertThrows(ValidationException.class, () -> {
            bankNameService.saveBankName(invalidBank);
        });
    }

    // --- toggleBankNameStatus ---
    @Test
    void toggleBankNameStatus_bankExists_togglesStatus() throws Exception {
        when(mockBankNameRepository.findById(1)).thenReturn(Optional.of(testBankDto)); // Initially active
        doNothing().when(mockBankNameRepository).setActiveStatus(1, false); // Expect to be set to inactive

        bankNameService.toggleBankNameStatus(1);

        verify(mockBankNameRepository).setActiveStatus(1, false);
    }

    @Test
    void toggleBankNameStatus_bankNotExists_throwsBankNameNotFoundException() {
        when(mockBankNameRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(BankNameNotFoundException.class, () -> {
            bankNameService.toggleBankNameStatus(99);
        });
    }
}
