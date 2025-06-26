package com.basariatpos.service;

import com.basariatpos.model.PaymentDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.PaymentRepository;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.PaymentException;
import com.basariatpos.service.exception.PaymentValidationException;
import com.basariatpos.service.exception.SalesOrderNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository mockPaymentRepository;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private SalesOrderService mockSalesOrderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UserDTO testUser;
    private ShiftDTO testShift;
    private SalesOrderDTO testSalesOrder;
    private PaymentDTO inputPaymentDto;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setUserId(1);
        testUser.setUsername("testuser");

        testShift = new ShiftDTO();
        testShift.setShiftId(100);
        testShift.setStatus("Active");
        testShift.setUserId(testUser.getUserId());

        testSalesOrder = new SalesOrderDTO();
        testSalesOrder.setSalesOrderId(1);
        testSalesOrder.setBalanceDue(new BigDecimal("100.00"));
        testSalesOrder.setStatus("Pending");

        inputPaymentDto = new PaymentDTO();
        inputPaymentDto.setSalesOrderId(1);
        inputPaymentDto.setAmount(new BigDecimal("50.00"));
        inputPaymentDto.setPaymentMethod("Cash");
    }

    private void setupActiveSession() {
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUser);
        when(mockUserSessionService.isShiftActive()).thenReturn(true);
        when(mockUserSessionService.getActiveShift()).thenReturn(testShift);
    }

    @Test
    void constructor_nullPaymentRepository_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, // Or IllegalArgumentException if explicitly checked
            () -> new PaymentServiceImpl(null, mockUserSessionService, mockSalesOrderService));
    }

    @Test
    void constructor_nullUserSessionService_throwsIllegalArgumentException() {
         assertThrows(NullPointerException.class, // Or IllegalArgumentException
            () -> new PaymentServiceImpl(mockPaymentRepository, null, mockSalesOrderService));
    }

    @Test
    void constructor_nullSalesOrderService_throwsIllegalArgumentException() {
         assertThrows(NullPointerException.class, // Or IllegalArgumentException
            () -> new PaymentServiceImpl(mockPaymentRepository, mockUserSessionService, null));
    }


    @Test
    void recordPayment_validPayment_success() throws Exception {
        setupActiveSession();
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.of(testSalesOrder));
        when(mockPaymentRepository.save(any(PaymentDTO.class))).thenAnswer(inv -> {
            PaymentDTO dto = inv.getArgument(0);
            dto.setPaymentId(1001); // Simulate DB generating ID
            dto.setPaymentDate(OffsetDateTime.now()); // Simulate DB setting date
            dto.setShiftId(testShift.getShiftId()); // Simulate DB/procedure setting shift
            return dto;
        });

        PaymentDTO result = paymentService.recordPayment(inputPaymentDto);

        assertNotNull(result);
        assertEquals(1001, result.getPaymentId());
        assertEquals(testUser.getUserId(), result.getReceivedByUserId());
        assertEquals(testShift.getShiftId(), result.getShiftId());
        verify(mockPaymentRepository).save(inputPaymentDto);
    }

    @Test
    void recordPayment_noActiveShift_throwsNoActiveShiftException() {
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUser);
        when(mockUserSessionService.isShiftActive()).thenReturn(false); // No active shift

        assertThrows(NoActiveShiftException.class, () -> {
            paymentService.recordPayment(inputPaymentDto);
        });
        verify(mockPaymentRepository, never()).save(any());
    }

    @Test
    void recordPayment_salesOrderNotFound_throwsSalesOrderNotFoundException() throws Exception {
        setupActiveSession();
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () -> {
            paymentService.recordPayment(inputPaymentDto);
        });
    }

    @Test
    void recordPayment_amountExceedsBalanceDue_throwsPaymentValidationException() throws Exception {
        setupActiveSession();
        testSalesOrder.setBalanceDue(new BigDecimal("30.00")); // Balance is less than payment amount
        inputPaymentDto.setAmount(new BigDecimal("50.00"));
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.of(testSalesOrder));

        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> {
            paymentService.recordPayment(inputPaymentDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("cannot exceed balance due")));
    }

    @Test
    void recordPayment_paymentMethodBank_bankNameRequired_throwsPaymentValidationException() throws Exception {
        setupActiveSession();
        inputPaymentDto.setPaymentMethod("Bank Transfer");
        inputPaymentDto.setBankNameId(null); // Missing bank name
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.of(testSalesOrder));

        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> {
            paymentService.recordPayment(inputPaymentDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Bank name is required")));
    }

    @Test
    void recordPayment_paymentMethodCard_transactionIdRequired_throwsPaymentValidationException() throws Exception {
        setupActiveSession();
        inputPaymentDto.setPaymentMethod("Card");
        inputPaymentDto.setBankNameId(1);
        inputPaymentDto.setTransactionId(null); // Missing transaction ID
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.of(testSalesOrder));

        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> {
            paymentService.recordPayment(inputPaymentDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Transaction ID/Reference is required")));
    }


    @Test
    void getPaymentsForOrder_orderExists_returnsPayments() throws Exception {
        setupActiveSession(); // Not strictly needed for this method, but good for consistency
        List<PaymentDTO> payments = new ArrayList<>();
        payments.add(new PaymentDTO());
        when(mockSalesOrderService.getSalesOrderDetails(1)).thenReturn(Optional.of(testSalesOrder)); // Order exists
        when(mockPaymentRepository.findBySalesOrderId(1)).thenReturn(payments);

        List<PaymentDTO> result = paymentService.getPaymentsForOrder(1);
        assertFalse(result.isEmpty());
        verify(mockPaymentRepository).findBySalesOrderId(1);
    }

    @Test
    void getPaymentsForOrder_orderNotFound_throwsSalesOrderNotFoundException() throws Exception {
        setupActiveSession(); // Not strictly needed
        when(mockSalesOrderService.getSalesOrderDetails(99)).thenReturn(Optional.empty()); // Order does not exist

        assertThrows(SalesOrderNotFoundException.class, () -> {
            paymentService.getPaymentsForOrder(99);
        });
        verify(mockPaymentRepository, never()).findBySalesOrderId(anyInt());
    }

    @Test
    void getPaymentById_paymentExists_returnsPayment() throws Exception {
        setupActiveSession(); // Not strictly necessary but good for consistency
        PaymentDTO mockPayment = new PaymentDTO();
        mockPayment.setPaymentId(1);
        mockPayment.setAmount(BigDecimal.TEN);
        when(mockPaymentRepository.findById(1)).thenReturn(Optional.of(mockPayment));

        Optional<PaymentDTO> result = paymentService.getPaymentById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getPaymentId());
        verify(mockPaymentRepository).findById(1);
    }

    @Test
    void getPaymentById_paymentNotExists_returnsEmptyOptional() throws Exception {
        setupActiveSession(); // Not strictly necessary
        when(mockPaymentRepository.findById(99)).thenReturn(Optional.empty());

        Optional<PaymentDTO> result = paymentService.getPaymentById(99);

        assertFalse(result.isPresent());
        verify(mockPaymentRepository).findById(99);
    }

    @Test
    void getPaymentById_repositoryThrowsException_throwsPaymentException() throws Exception {
        setupActiveSession(); // Not strictly necessary
        when(mockPaymentRepository.findById(anyInt())).thenThrow(new RuntimeException("DB error")); // Simulate underlying repo error

        assertThrows(PaymentException.class, () -> {
            paymentService.getPaymentById(1);
        });
    }
}
