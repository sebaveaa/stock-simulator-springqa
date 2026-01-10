package ucab.edu.ve.stocksimulator.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ucab.edu.ve.stocksimulator.dto.request.BuyRequestDTO;
import ucab.edu.ve.stocksimulator.model.OwnedStock;
import ucab.edu.ve.stocksimulator.model.Stock;
import ucab.edu.ve.stocksimulator.repository.OwnedStockRepo;
import ucab.edu.ve.stocksimulator.repository.StockRepo;
import ucab.edu.ve.stocksimulator.repository.TransactionRepo;
import ucab.edu.ve.stocksimulator.repository.UserRepo;
import ucab.edu.ve.stocksimulator.util.TestDataLoader;

import javax.sql.DataSource;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de Integración para el Módulo de Consultas de Acciones
 * ISO/IEC 29119 - Test Level 2 (Gray Box Testing)
 *
 * Valida el flujo completo: Controller → Service → Repository → Database
 * Utiliza Testcontainers con PostgreSQL real para pruebas de integración.
 *
 * Características de calidad ISO/IEC 25010 validadas:
 * - Funcionalidad: Correctitud de consultas
 * - Eficiencia del Desempeño: Tiempo de respuesta < 500ms
 * - Usabilidad: Respuestas JSON estructuradas correctamente
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StockQueryIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("stocksimulator_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StockRepo stockRepo;

    @Autowired
    private OwnedStockRepo ownedStockRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        // Cargar datos de prueba
        TestDataLoader.loadAllTestData(dataSource);
    }

    @AfterEach
    void tearDown() {
        // Limpiar datos después de cada prueba
        transactionRepo.deleteAll();
        ownedStockRepo.deleteAll();
    }

    /**
     * IT-005: Prueba de integración - Consultar todas las acciones disponibles
     *
     * Objetivo: Validar que el endpoint /api/stock/all devuelve todas las acciones del mercado
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Datos completos y precisos
     */
    @Test
    @Order(1)
    @DisplayName("IT-005: Consultar acciones disponibles - Retorna lista completa del mercado")
    void testGetAllAvailableStocks_ReturnsCompleteList() throws Exception {
        // When: Se consultan todas las acciones disponibles
        given()
        .when()
            .get("/stock/all")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("stocks", notNullValue())
            .body("stocks.size()", greaterThanOrEqualTo(10)); // Esperamos al menos 10 acciones del seed

        // Then: Verificar en la base de datos
        List<Stock> stocks = stockRepo.findAll();
        assertFalse(stocks.isEmpty(), "Debe haber acciones en la base de datos");
        assertTrue(stocks.size() >= 10, "Debe haber al menos 10 acciones del seed script");

        // Verificar que las acciones clave existen
        assertTrue(stocks.stream().anyMatch(s -> s.getTicker().equals("AAPL")), "Debe existir AAPL");
        assertTrue(stocks.stream().anyMatch(s -> s.getTicker().equals("GOOGL")), "Debe existir GOOGL");
        assertTrue(stocks.stream().anyMatch(s -> s.getTicker().equals("MSFT")), "Debe existir MSFT");

        // Verificar tiempo de respuesta < 500ms (ISO/IEC 25010)
        long startTime = System.currentTimeMillis();
        given()
        .when()
            .get("/stock/all");
        long responseTime = System.currentTimeMillis() - startTime;

        assertTrue(responseTime < 500, "El tiempo de respuesta debe ser < 500ms, fue: " + responseTime + "ms");
    }

    /**
     * IT-006: Prueba de integración - Consultar acciones adquiridas por usuario
     *
     * Objetivo: Validar que el endpoint retorna correctamente las acciones que posee un usuario
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Filtrado correcto por usuario
     */
    @Test
    @Order(2)
    @DisplayName("IT-006: Consultar acciones adquiridas - Filtrado correcto por usuario")
    void testGetOwnedStocksByUser_ReturnsUserStocks() throws Exception {
        // Given: Usuario con acciones compradas
        String username = "testuser1";

        // Comprar 3 tipos de acciones diferentes
        String[] tickers = {"AAPL", "GOOGL", "MSFT"};
        int[] quantities = {10, 15, 20};

        for (int i = 0; i < tickers.length; i++) {
            BuyRequestDTO buyRequest = new BuyRequestDTO();
            buyRequest.username = username;
            buyRequest.ticker = tickers[i];
            buyRequest.name = "Test Stock " + i;
            buyRequest.quantity = quantities[i];
            buyRequest.amount = 1000.00f * (i + 1);

            given()
                .contentType(ContentType.JSON)
                .body(buyRequest)
            .when()
                .post("/transaction/buy")
            .then()
                .statusCode(anyOf(is(200), is(201)));
        }

        // When: Se consultan las acciones adquiridas por el usuario
        given()
            .pathParam("user", username)
        .when()
            .get("/stock/ownedstocks/{user}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(3))
            .body("ticker", hasItems("AAPL", "GOOGL", "MSFT"))
            .body("[0].quantity", notNullValue());

        // Then: Verificar en la base de datos
        List<OwnedStock> ownedStocks = ownedStockRepo.findByUser(userRepo.findByUsername(username));
        assertEquals(3, ownedStocks.size(), "El usuario debe tener 3 tipos de acciones");

        // Verificar cantidades específicas
        OwnedStock aaplStock = ownedStocks.stream()
                .filter(os -> os.getTicker().equals("AAPL"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Debe existir AAPL"));
        assertEquals(10, aaplStock.getQuantity(), "AAPL debe tener 10 unidades");

        // Verificar tiempo de respuesta
        long startTime = System.currentTimeMillis();
        given()
            .pathParam("user", username)
        .when()
            .get("/stock/ownedstocks/{user}");
        long responseTime = System.currentTimeMillis() - startTime;

        assertTrue(responseTime < 500, "El tiempo de respuesta debe ser < 500ms, fue: " + responseTime + "ms");
    }

    /**
     * IT-007: Prueba de integración - Usuario sin acciones
     *
     * Objetivo: Validar que el endpoint retorna lista vacía para usuarios sin acciones
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Manejo correcto de casos vacíos
     */
    @Test
    @Order(3)
    @DisplayName("IT-007: Consultar acciones de usuario sin compras - Retorna lista vacía")
    void testGetOwnedStocksByUser_NoStocks_ReturnsEmptyList() throws Exception {
        // Given: Usuario sin acciones
        String username = "testuser2";

        // When: Se consultan las acciones adquiridas
        given()
            .pathParam("user", username)
        .when()
            .get("/stock/ownedstocks/{user}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(0));

        // Then: Verificar en la base de datos
        List<OwnedStock> ownedStocks = ownedStockRepo.findByUser(userRepo.findByUsername(username));
        assertTrue(ownedStocks.isEmpty(), "El usuario no debe tener acciones");
    }

    /**
     * IT-008: Prueba de integración - Consultar EOD data de acción específica
     *
     * Objetivo: Validar que el endpoint retorna datos EOD correctos para un ticker válido
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Datos precisos por ticker
     *
     * Nota: Este test puede requerir datos EOD precargados o un mock del servicio externo.
     * Si no hay datos EOD disponibles, el endpoint debe manejar esto correctamente.
     */
    @Test
    @Order(4)
    @DisplayName("IT-008: Consultar EOD data de ticker válido - Retorna datos o maneja ausencia correctamente")
    void testGetLatestStockEODData_ValidTicker() throws Exception {
        // Given: Ticker válido en la base de datos
        String ticker = "AAPL";

        // When: Se consultan los datos EOD más recientes
        // Then: El endpoint debe responder correctamente (200 OK)
        // Puede retornar datos EOD o un mensaje indicando que no hay datos disponibles
        given()
            .pathParam("ticker", ticker)
        .when()
            .get("/stock/{ticker}")
        .then()
            .statusCode(200);

        // Verificar que el ticker existe en la base de datos
        Stock stock = stockRepo.findByTicker(ticker);
        assertNotNull(stock, "El ticker AAPL debe existir en la base de datos");
        assertEquals("AAPL", stock.getTicker(), "El ticker debe ser AAPL");

        // Verificar tiempo de respuesta
        long startTime = System.currentTimeMillis();
        given()
            .pathParam("ticker", ticker)
        .when()
            .get("/stock/{ticker}");
        long responseTime = System.currentTimeMillis() - startTime;

        assertTrue(responseTime < 500, "El tiempo de respuesta debe ser < 500ms, fue: " + responseTime + "ms");
    }

    /**
     * IT-009: Prueba de integración - Múltiples usuarios con acciones diferentes
     *
     * Objetivo: Validar que el sistema mantiene correctamente la separación de acciones entre usuarios
     * ISO/IEC 25010: Fiabilidad (Madurez) - Integridad de datos por usuario
     */
    @Test
    @Order(5)
    @DisplayName("IT-009: Múltiples usuarios con acciones - Separación correcta de datos")
    void testMultipleUsersWithDifferentStocks_CorrectSeparation() throws Exception {
        // Given: Dos usuarios con diferentes acciones
        String user1 = "testuser1";
        String user2 = "testuser2";

        // User1 compra AAPL y GOOGL
        BuyRequestDTO buy1 = new BuyRequestDTO();
        buy1.username = user1;
        buy1.ticker = "AAPL";
        buy1.name = "Apple Inc.";
        buy1.quantity = 10;
        buy1.amount = 1500.00f;

        given()
            .contentType(ContentType.JSON)
            .body(buy1)
        .when()
            .post("/transaction/buy")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        buy1.ticker = "GOOGL";
        buy1.name = "Alphabet Inc.";
        given()
            .contentType(ContentType.JSON)
            .body(buy1)
        .when()
            .post("/transaction/buy")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // User2 compra solo MSFT
        BuyRequestDTO buy2 = new BuyRequestDTO();
        buy2.username = user2;
        buy2.ticker = "MSFT";
        buy2.name = "Microsoft Corp.";
        buy2.quantity = 20;
        buy2.amount = 2000.00f;

        given()
            .contentType(ContentType.JSON)
            .body(buy2)
        .when()
            .post("/transaction/buy")
        .then()
            .statusCode(anyOf(is(200), is(201)));

        // When/Then: Verificar que cada usuario tiene sus propias acciones
        given()
            .pathParam("user", user1)
        .when()
            .get("/stock/ownedstocks/{user}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("ticker", hasItems("AAPL", "GOOGL"))
            .body("ticker", not(hasItem("MSFT")));

        given()
            .pathParam("user", user2)
        .when()
            .get("/stock/ownedstocks/{user}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("ticker", hasItems("MSFT"))
            .body("ticker", not(hasItem("AAPL")))
            .body("ticker", not(hasItem("GOOGL")));

        // Then: Verificar en la base de datos
        List<OwnedStock> user1Stocks = ownedStockRepo.findByUser(userRepo.findByUsername(user1));
        List<OwnedStock> user2Stocks = ownedStockRepo.findByUser(userRepo.findByUsername(user2));

        assertEquals(2, user1Stocks.size(), "User1 debe tener 2 tipos de acciones");
        assertEquals(1, user2Stocks.size(), "User2 debe tener 1 tipo de acción");

        // Verificar que no hay mezcla de datos
        assertTrue(user1Stocks.stream().noneMatch(s -> s.getTicker().equals("MSFT")),
                "User1 no debe tener MSFT");
        assertTrue(user2Stocks.stream().noneMatch(s -> s.getTicker().equals("AAPL")),
                "User2 no debe tener AAPL");
    }

    /**
     * IT-010: Prueba de integración - Consultar datos EOD del último mes
     *
     * Objetivo: Validar que el endpoint de datos históricos responde correctamente
     * ISO/IEC 25010: Funcionalidad (Correctitud) - Datos históricos por periodo
     */
    @Test
    @Order(6)
    @DisplayName("IT-010: Consultar EOD data del último mes - Respuesta correcta")
    void testGetLastMonthStockEODData_ValidTicker() throws Exception {
        // Given: Ticker válido
        String ticker = "GOOGL";

        // When: Se consultan los datos EOD del último mes
        // Then: El endpoint debe responder correctamente
        given()
            .pathParam("ticker", ticker)
        .when()
            .get("/stock/{ticker}/month")
        .then()
            .statusCode(200);

        // Verificar que el ticker existe
        Stock stock = stockRepo.findByTicker(ticker);
        assertNotNull(stock, "El ticker GOOGL debe existir en la base de datos");

        // Verificar tiempo de respuesta
        long startTime = System.currentTimeMillis();
        given()
            .pathParam("ticker", ticker)
        .when()
            .get("/stock/{ticker}/month");
        long responseTime = System.currentTimeMillis() - startTime;

        assertTrue(responseTime < 500, "El tiempo de respuesta debe ser < 500ms, fue: " + responseTime + "ms");
    }
}
