package ucab.edu.ve.stocksimulator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucab.edu.ve.stocksimulator.dto.request.BuyRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.SellRequestDTO;
import ucab.edu.ve.stocksimulator.model.Transaction;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.repository.TransactionRepo;
import ucab.edu.ve.stocksimulator.repository.UserRepo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para TransactionService
 * ISO/IEC 29119 - Test Level 1 (White Box Testing)
 *
 * Valida la lógica de negocio del servicio de transacciones de forma aislada
 * utilizando mocks para las dependencias.
 *
 * Características de calidad ISO/IEC 25010 validadas:
 * - Funcionalidad: Correctitud de operaciones de negocio
 * - Fiabilidad: Validación de entrada y manejo de datos
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private EmailSenderService emailSenderService;

    @InjectMocks
    private TransactionService transactionService;

    /**
     * UT-001: Prueba unitaria - Registrar compra exitosa
     *
     * Objetivo: Validar que el método registerPurchase crea y guarda correctamente una transacción de compra
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Operación de compra
     */
    @Test
    @DisplayName("UT-001: Registrar compra - Crea transacción correctamente")
    void testRegisterPurchase_Success() {
        // Given: Usuario y datos de compra válidos
        String username = "testuser1";
        String ticker = "AAPL";
        String stockName = "Apple Inc.";
        int quantity = 10;
        float amount = 1500.00f;

        User mockUser = new User();
        mockUser.setUsername(username);

        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = username;
        buyRequest.ticker = ticker;
        buyRequest.name = stockName;
        buyRequest.quantity = quantity;
        buyRequest.amount = amount;

        // Configurar mocks
        when(userRepo.findByUsername(username)).thenReturn(mockUser);
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Se registra la compra
        transactionService.registerPurchase(buyRequest);

        // Then: Verificar que se guardó la transacción con los datos correctos
        verify(transactionRepo, times(1)).save(argThat(transaction -> {
            assertEquals(mockUser, transaction.getIssuer(), "El emisor debe ser el usuario");
            assertEquals(quantity, transaction.getQuantity(), "La cantidad debe coincidir");
            assertEquals(stockName, transaction.getNameStock(), "El nombre de la acción debe coincidir");
            assertEquals("buy", transaction.getType(), "El tipo debe ser 'buy'");
            assertEquals(amount, transaction.getAmount(), "El monto debe coincidir");
            assertNull(transaction.getReceptor(), "El receptor debe ser null en compras");
            assertNotNull(transaction.getDate(), "La fecha debe estar establecida");
            return true;
        }));

        verify(userRepo, times(1)).findByUsername(username);
    }

    /**
     * UT-002: Prueba unitaria - Registrar venta exitosa
     *
     * Objetivo: Validar que el método registerSell crea y guarda correctamente una transacción de venta
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Operación de venta
     */
    @Test
    @DisplayName("UT-002: Registrar venta - Crea transacción correctamente")
    void testRegisterSell_Success() {
        // Given: Usuario y datos de venta válidos
        String username = "testuser1";
        String ticker = "GOOGL";
        String stockName = "Alphabet Inc.";
        int quantity = 5;
        float amount = 750.00f;

        User mockUser = new User();
        mockUser.setUsername(username);

        SellRequestDTO sellRequest = new SellRequestDTO();
        sellRequest.username = username;
        sellRequest.ticker = ticker;
        sellRequest.name = stockName;
        sellRequest.quantity = quantity;
        sellRequest.amount = amount;

        // Configurar mocks
        when(userRepo.findByUsername(username)).thenReturn(mockUser);
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Se registra la venta
        transactionService.registerSell(sellRequest);

        // Then: Verificar que se guardó la transacción con los datos correctos
        verify(transactionRepo, times(1)).save(argThat(transaction -> {
            assertEquals(mockUser, transaction.getIssuer(), "El emisor debe ser el usuario");
            assertEquals(quantity, transaction.getQuantity(), "La cantidad debe coincidir");
            assertEquals(stockName, transaction.getNameStock(), "El nombre de la acción debe coincidir");
            assertEquals("sell", transaction.getType(), "El tipo debe ser 'sell'");
            assertEquals(amount, transaction.getAmount(), "El monto debe coincidir");
            assertNull(transaction.getReceptor(), "El receptor debe ser null en ventas");
            assertNotNull(transaction.getDate(), "La fecha debe estar establecida");
            return true;
        }));

        verify(userRepo, times(1)).findByUsername(username);
    }

    /**
     * UT-003: Prueba unitaria - Verificar tarjeta VISA válida
     *
     * Objetivo: Validar que el método verifyVISA acepta tarjetas VISA válidas usando el algoritmo de Luhn
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Validación de entrada
     */
    @Test
    @DisplayName("UT-003: Verificar VISA válida - Acepta tarjeta correcta")
    void testVerifyVISA_Valid() {
        // Given: Números de tarjeta VISA válidos
        String validVisa1 = "4532015112830366"; // VISA válida de 16 dígitos
        String validVisa2 = "4916338506082832"; // VISA válida de 16 dígitos
        String validVisa3 = "4024007198964305"; // VISA válida de 16 dígitos

        // When/Then: Verificar que todas las tarjetas válidas son aceptadas
        assertTrue(transactionService.verifyVISA(validVisa1),
                "La tarjeta VISA válida 1 debe ser aceptada");
        assertTrue(transactionService.verifyVISA(validVisa2),
                "La tarjeta VISA válida 2 debe ser aceptada");
        assertTrue(transactionService.verifyVISA(validVisa3),
                "La tarjeta VISA válida 3 debe ser aceptada");
    }

    /**
     * UT-004: Prueba unitaria - Verificar tarjeta VISA inválida
     *
     * Objetivo: Validar que el método verifyVISA rechaza tarjetas inválidas
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Validación de entrada incorrecta
     */
    @Test
    @DisplayName("UT-004: Verificar VISA inválida - Rechaza tarjetas incorrectas")
    void testVerifyVISA_Invalid() {
        // Given: Números de tarjeta inválidos
        String invalidVisa1 = "4532015112830367"; // Dígito de verificación incorrecto (Luhn falla)
        String invalidVisa2 = "3532015112830366"; // No comienza con 4 (no es VISA)
        String invalidVisa3 = "123456789012345";  // Longitud incorrecta y no comienza con 4
        String invalidVisa4 = "";                 // Tarjeta vacía
        String invalidVisa5 = "4532-0151-1283-0366"; // Formato con guiones (no válido)

        // When/Then: Verificar que todas las tarjetas inválidas son rechazadas
        assertFalse(transactionService.verifyVISA(invalidVisa1),
                "Tarjeta con dígito de verificación incorrecto debe ser rechazada");
        assertFalse(transactionService.verifyVISA(invalidVisa2),
                "Tarjeta que no comienza con 4 debe ser rechazada");
        assertFalse(transactionService.verifyVISA(invalidVisa3),
                "Tarjeta con longitud incorrecta debe ser rechazada");
        assertFalse(transactionService.verifyVISA(invalidVisa4),
                "Tarjeta vacía debe ser rechazada");
        assertFalse(transactionService.verifyVISA(invalidVisa5),
                "Tarjeta con formato incorrecto debe ser rechazada");
    }
}
