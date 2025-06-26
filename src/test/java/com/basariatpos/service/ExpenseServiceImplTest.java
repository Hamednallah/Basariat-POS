package com.basariatpos.service;

import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.ExpenseRepository;
import com.basariatpos.repository.RepositoryException;
import com.basariatpos.service.exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock private ExpenseRepository mockExpenseRepository;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private ExpenseCategoryService mockExpenseCategoryService;
    @Mock private BankNameService mockBankNameService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private UserDTO testUser;
    private ShiftDTO testShift;
    private ExpenseDTO inputExpenseDto;
    private ExpenseCategoryDTO testCategory;
    private BankNameDTO testBankName;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setUserId(1);
        testUser.setUsername("testuser");

        testShift = new ShiftDTO();
        testShift.setShiftId(100);
        testShift.setStatus("Active");
        testShift.setUserId(testUser.getUserId());

        testCategory = new ExpenseCategoryDTO(1, "Office Supplies EN", "Office Supplies AR", "Stationery, utilities, etc.", true);
        testBankName = new BankNameDTO(1, "Test Bank EN", "Test Bank AR", true);

        inputExpenseDto = new ExpenseDTO();
        inputExpenseDto.setExpenseDate(LocalDate.now());
        inputExpenseDto.setExpenseCategoryId(testCategory.getExpenseCategoryId());
        inputExpenseDto.setDescription("Test Expense");
        inputExpenseDto.setAmount(new BigDecimal("50.00"));
        inputExpenseDto.setPaymentMethod("Cash"); // Default to cash for many tests
    }

    private void setupActiveSession() {
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUser);
    }

    private void setupActiveCashSession() {
        setupActiveSession();
        when(mockUserSessionService.isShiftActive()).thenReturn(true);
        when(mockUserSessionService.getActiveShift()).thenReturn(testShift);
    }

    @Test
    void recordExpense_cashPayment_valid_success() throws Exception {
        setupActiveCashSession();
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockExpenseRepository.save(any(ExpenseDTO.class))).thenAnswer(inv -> {
            ExpenseDTO dto = inv.getArgument(0);
            dto.setExpenseId(1001); // Simulate DB generating ID
            return dto;
        });

        ExpenseDTO result = expenseService.recordExpense(inputExpenseDto);

        assertNotNull(result);
        assertEquals(1001, result.getExpenseId());
        assertEquals(testUser.getUserId(), result.getCreatedByUserId());
        assertEquals(testShift.getShiftId(), result.getShiftId()); // Shift ID set for cash
        assertNull(result.getBankNameId()); // Bank details null for cash
        assertNull(result.getTransactionIdRef());
        verify(mockExpenseRepository).save(inputExpenseDto);
    }

    @Test
    void recordExpense_bankPayment_valid_success() throws Exception {
        setupActiveSession(); // Active shift not strictly required for bank payment logic in service
        inputExpenseDto.setPaymentMethod("Bank Transaction");
        inputExpenseDto.setBankNameId(testBankName.getBankNameId());
        inputExpenseDto.setTransactionIdRef("TXN123");

        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockBankNameService.getBankNameById(testBankName.getBankNameId())).thenReturn(Optional.of(testBankName));
        when(mockExpenseRepository.save(any(ExpenseDTO.class))).thenAnswer(inv -> {
            ExpenseDTO dto = inv.getArgument(0);
            dto.setExpenseId(1002);
            return dto;
        });

        ExpenseDTO result = expenseService.recordExpense(inputExpenseDto);

        assertNotNull(result);
        assertEquals(1002, result.getExpenseId());
        assertEquals(testUser.getUserId(), result.getCreatedByUserId());
        assertNull(result.getShiftId()); // Shift ID should be null for bank transactions
        assertEquals(testBankName.getBankNameId(), result.getBankNameId());
        assertEquals("TXN123", result.getTransactionIdRef());
        verify(mockExpenseRepository).save(inputExpenseDto);
    }

    @Test
    void recordExpense_cashPayment_noActiveShift_throwsNoActiveShiftException() {
        setupActiveSession();
        // Simulate category check passing before shift check for this specific test path
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockUserSessionService.isShiftActive()).thenReturn(false); // No active shift

        assertThrows(NoActiveShiftException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
        verify(mockExpenseRepository, never()).save(any());
    }

    @Test
    void recordExpense_categoryNotFound_throwsCategoryNotFoundException() {
        setupActiveCashSession(); // Or just setupActiveSession() if payment method irrelevant for this check
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
    }

    @Test
    void recordExpense_bankPayment_bankNameNotFound_throwsBankNameNotFoundException() {
        setupActiveSession();
        inputExpenseDto.setPaymentMethod("Card");
        inputExpenseDto.setBankNameId(999); // Non-existent bank
        inputExpenseDto.setTransactionIdRef("TXN456");
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockBankNameService.getBankNameById(999)).thenReturn(Optional.empty());

        assertThrows(BankNameNotFoundException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
    }

    @Test
    void recordExpense_invalidDto_missingAmount_throwsExpenseValidationException() {
        setupActiveCashSession();
        inputExpenseDto.setAmount(null); // Invalid amount
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));

        ExpenseValidationException ex = assertThrows(ExpenseValidationException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Amount must be positive")));
    }

    @Test
    void recordExpense_bankPayment_missingTransactionId_throwsExpenseValidationException() {
        setupActiveSession();
        inputExpenseDto.setPaymentMethod("Bank Transaction");
        inputExpenseDto.setBankNameId(testBankName.getBankNameId());
        inputExpenseDto.setTransactionIdRef(null); // Missing transaction ID

        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockBankNameService.getBankNameById(testBankName.getBankNameId())).thenReturn(Optional.of(testBankName));

        ExpenseValidationException ex = assertThrows(ExpenseValidationException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Transaction ID/Reference is required")));
    }

    @Test
    void recordExpense_repositorySaveFails_throwsExpenseException() throws Exception {
        setupActiveCashSession();
        when(mockExpenseCategoryService.getExpenseCategoryById(testCategory.getExpenseCategoryId())).thenReturn(Optional.of(testCategory));
        when(mockExpenseRepository.save(any(ExpenseDTO.class))).thenThrow(new RepositoryException("DB save failed"));

        assertThrows(ExpenseException.class, () -> {
            expenseService.recordExpense(inputExpenseDto);
        });
    }


    @Test
    void findExpenses_delegatesToRepository() throws Exception {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        Integer categoryId = 1;
        List<ExpenseDTO> expectedExpenses = List.of(new ExpenseDTO());
        when(mockExpenseRepository.findAllFiltered(from, to, categoryId)).thenReturn(expectedExpenses);

        List<ExpenseDTO> result = expenseService.findExpenses(from, to, categoryId);

        assertEquals(expectedExpenses, result);
        verify(mockExpenseRepository).findAllFiltered(from, to, categoryId);
    }

    @Test
    void findExpenses_repositoryThrowsException_throwsExpenseException() throws Exception {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        when(mockExpenseRepository.findAllFiltered(any(), any(), any())).thenThrow(new RepositoryException("DB error"));

        assertThrows(ExpenseException.class, () -> {
            expenseService.findExpenses(from, to, null);
        });
    }

    @Test
    void getExpenseById_exists_returnsDto() throws Exception {
        int expenseId = 1;
        ExpenseDTO expectedExpense = new ExpenseDTO();
        expectedExpense.setExpenseId(expenseId);
        when(mockExpenseRepository.findById(expenseId)).thenReturn(Optional.of(expectedExpense));

        Optional<ExpenseDTO> result = expenseService.getExpenseById(expenseId);

        assertTrue(result.isPresent());
        assertEquals(expectedExpense, result.get());
        verify(mockExpenseRepository).findById(expenseId);
    }

    @Test
    void getExpenseById_notExists_returnsEmpty() throws Exception {
        int expenseId = 99;
        when(mockExpenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        Optional<ExpenseDTO> result = expenseService.getExpenseById(expenseId);

        assertFalse(result.isPresent());
        verify(mockExpenseRepository).findById(expenseId);
    }
}
