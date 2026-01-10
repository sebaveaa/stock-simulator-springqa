package ucab.edu.ve.stocksimulator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucab.edu.ve.stocksimulator.controller.StockController;
import ucab.edu.ve.stocksimulator.dto.OwnedStockDTO;
import ucab.edu.ve.stocksimulator.dto.StockDTO;
import ucab.edu.ve.stocksimulator.dto.response.MessageResponseDTO;
import ucab.edu.ve.stocksimulator.dto.response.StockListResponseDTO;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;
import ucab.edu.ve.stocksimulator.service.StockEODService;
import ucab.edu.ve.stocksimulator.service.StockService;
import ucab.edu.ve.stocksimulator.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para StockController
 * ISO/IEC 29119 - Test Level 1 (White Box Testing)
 *
 * Valida el comportamiento del controlador de acciones de forma aislada
 * utilizando mocks para las dependencias de servicio.
 *
 * Características de calidad ISO/IEC 25010 validadas:
 * - Funcionalidad: Correctitud de endpoints REST
 * - Usabilidad: Respuestas HTTP apropiadas
 */
@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockEODService stockEODService;

    @Mock
    private StockService stockService;

    @Mock
    private OwnedStockService ownedStockService;

    @Mock
    private UserService userService;

    @InjectMocks
    private StockController stockController;

    @Test
    void getLatestStockEODData_ShouldReturnOkAndData() {
        // Arrange
        String ticker = "AAPL";
        Object expectedData = new Object(); // Mocked response object
        when(stockEODService.getLatestStockEODData(ticker)).thenReturn(expectedData);

        // Act
        ResponseEntity<Object> response = stockController.getLatestStockEODData(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedData, response.getBody());
        verify(stockEODService, times(1)).getLatestStockEODData(ticker);
    }

    @Test
    void getLastMonthStockEODData_ShouldReturnOkAndData() {
        // Arrange
        String ticker = "GOOGL";
        Object[] expectedData = new Object[0]; // Mocked response object
        when(stockEODService.getLastMonthStockEODData(ticker)).thenReturn(expectedData);

        // Act
        ResponseEntity<Object> response = stockController.getLastMonthStockEODData(ticker);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedData, response.getBody());
        verify(stockEODService, times(1)).getLastMonthStockEODData(ticker);
    }

    /**
     * UT-005: Prueba unitaria - Obtener todas las acciones disponibles
     *
     * Objetivo: Validar que el endpoint /api/stock/all retorna correctamente la lista de acciones
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Consulta de acciones disponibles
     */
    @Test
    @DisplayName("UT-005: Obtener acciones disponibles - Retorna lista completa")
    void testGetAvailableStocks_ReturnsAll() {
        // Given: Lista de acciones disponibles
        StockListResponseDTO expectedResponse = new StockListResponseDTO();
        expectedResponse.stocks = new ArrayList<>();

        // Simular 3 acciones en la respuesta
        StockDTO stock1 = new StockDTO();
        stock1.ticker = "AAPL";
        stock1.name = "Apple Inc.";
        stock1.description = "Technology company";
        expectedResponse.stocks.add(stock1);

        StockDTO stock2 = new StockDTO();
        stock2.ticker = "GOOGL";
        stock2.name = "Alphabet Inc.";
        stock2.description = "Search and advertising";
        expectedResponse.stocks.add(stock2);

        StockDTO stock3 = new StockDTO();
        stock3.ticker = "MSFT";
        stock3.name = "Microsoft Corp.";
        stock3.description = "Software company";
        expectedResponse.stocks.add(stock3);

        when(stockService.findAll()).thenReturn(expectedResponse);

        // When: Se consultan las acciones disponibles
        ResponseEntity<StockListResponseDTO> response = stockController.getAvailableStocks();

        // Then: Verificar respuesta exitosa
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals(3, response.getBody().stocks.size(), "Debe retornar 3 acciones");
        assertEquals("AAPL", response.getBody().stocks.get(0).ticker, "El primer ticker debe ser AAPL");

        verify(stockService, times(1)).findAll();
    }

    /**
     * UT-006: Prueba unitaria - Obtener acciones adquiridas por usuario
     *
     * Objetivo: Validar que el endpoint retorna las acciones que posee un usuario
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Consulta de acciones por usuario
     */
    @Test
    @DisplayName("UT-006: Obtener acciones adquiridas - Retorna acciones del usuario")
    void testGetOwnedStocksByUser_ReturnsUserStocks() {
        // Given: Usuario con acciones
        String username = "testuser1";
        List<OwnedStockDTO> ownedStocks = new ArrayList<>();

        OwnedStockDTO stock1 = new OwnedStockDTO();
        stock1.ticker = "AAPL";
        stock1.name = "Apple Inc.";
        stock1.quantity = 10;
        ownedStocks.add(stock1);

        OwnedStockDTO stock2 = new OwnedStockDTO();
        stock2.ticker = "GOOGL";
        stock2.name = "Alphabet Inc.";
        stock2.quantity = 5;
        ownedStocks.add(stock2);

        when(ownedStockService.getOwnedStocksByUser(username)).thenReturn(ownedStocks);

        // When: Se consultan las acciones del usuario
        ResponseEntity<List<OwnedStockDTO>> response = stockController.getOwnedStocksByUser(username);

        // Then: Verificar respuesta exitosa con las acciones
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals(2, response.getBody().size(), "El usuario debe tener 2 acciones");
        assertEquals("AAPL", response.getBody().get(0).ticker, "El primer ticker debe ser AAPL");
        assertEquals(10, response.getBody().get(0).quantity, "AAPL debe tener 10 unidades");

        verify(ownedStockService, times(1)).getOwnedStocksByUser(username);
    }

    /**
     * UT-007: Prueba unitaria - Obtener acciones de usuario sin compras
     *
     * Objetivo: Validar que el endpoint retorna lista vacía para usuarios sin acciones
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Manejo de casos vacíos
     */
    @Test
    @DisplayName("UT-007: Obtener acciones de usuario sin compras - Retorna lista vacía")
    void testGetOwnedStocksByUser_EmptyList() {
        // Given: Usuario sin acciones
        String username = "newuser";
        List<OwnedStockDTO> emptyList = new ArrayList<>();

        when(ownedStockService.getOwnedStocksByUser(username)).thenReturn(emptyList);

        // When: Se consultan las acciones del usuario
        ResponseEntity<List<OwnedStockDTO>> response = stockController.getOwnedStocksByUser(username);

        // Then: Verificar respuesta exitosa con lista vacía
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals(0, response.getBody().size(), "La lista debe estar vacía");

        verify(ownedStockService, times(1)).getOwnedStocksByUser(username);
    }

    /**
     * UT-008: Prueba unitaria - Crear acciones (endpoint de administrador)
     *
     * Objetivo: Validar que el endpoint /admin/create crea correctamente múltiples acciones
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Operación de creación administrativa
     */
    @Test
    @DisplayName("UT-008: Crear acciones - Crea múltiples acciones correctamente")
    void testCreateStocks_Success() {
        // Given: Array de acciones a crear
        StockDTO[] stockArray = new StockDTO[2];

        StockDTO stock1 = new StockDTO();
        stock1.ticker = "TSLA";
        stock1.name = "Tesla Inc.";
        stock1.description = "Electric vehicles";
        stockArray[0] = stock1;

        StockDTO stock2 = new StockDTO();
        stock2.ticker = "NVDA";
        stock2.name = "NVIDIA Corp.";
        stock2.description = "GPU manufacturer";
        stockArray[1] = stock2;

        // Simular que el servicio no retorna nada (operación exitosa)
        doNothing().when(userService).createAdmin();

        // When: Se crean las acciones
        ResponseEntity<MessageResponseDTO> response = stockController.createStocks(stockArray);

        // Then: Verificar respuesta exitosa
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "El código de estado debe ser 201 CREATED");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals(0, response.getBody().code, "El código de respuesta debe ser 0 (éxito)");
        assertEquals("Acciones creadas", response.getBody().message, "El mensaje debe indicar éxito");

        // Verificar que se llamó al servicio para guardar cada acción
        verify(stockService, times(2)).save(any());
        verify(userService, times(1)).createAdmin();
    }
}
