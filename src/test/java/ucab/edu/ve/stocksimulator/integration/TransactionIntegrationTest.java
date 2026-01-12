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
import ucab.edu.ve.stocksimulator.dto.request.SellRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.TransferRequestDTO;
import ucab.edu.ve.stocksimulator.model.OwnedStock;
import ucab.edu.ve.stocksimulator.model.Transaction;
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
 * Pruebas de Integración para el Módulo de Transacciones
 * ISO/IEC 29119 - Test Level 2 (Gray Box Testing)
 *
 * Valida el flujo completo: Controller → Service → Repository → Database
 * Utiliza Testcontainers con PostgreSQL real para pruebas de integración.
 *
 * Características de calidad ISO/IEC 25010 validadas:
 * - Fiabilidad: Madurez y Recuperabilidad
 * - Eficiencia del Desempeño: Comportamiento Temporal
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionIntegrationTest {

    @LocalServerPort
    private int port;

    /*
     * @Container
     * static PostgreSQLContainer<?> postgres = new
     * PostgreSQLContainer<>("postgres:15-alpine")
     * .withDatabaseName("stocksimulator_test")
     * .withUsername("test")
     * .withPassword("test");
     * 
     * @DynamicPropertySource
     * static void configureProperties(DynamicPropertyRegistry registry) {
     * registry.add("spring.datasource.url", postgres::getJdbcUrl);
     * registry.add("spring.datasource.username", postgres::getUsername);
     * registry.add("spring.datasource.password", postgres::getPassword);
     * }
     */

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private OwnedStockRepo ownedStockRepo;

    @Autowired
    private StockRepo stockRepo;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        // Limpiar datos antes de cargar nuevos datos de prueba
        transactionRepo.deleteAll();
        ownedStockRepo.deleteAll();
        
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
     * IT-001: Prueba de integración - Compra de acciones de extremo a extremo
     *
     * Objetivo: Validar el flujo completo de compra desde la API hasta la
     * persistencia en BD
     * ISO/IEC 25010: Fiabilidad (Madurez) - Transacciones ACID
     */
    @Test
    @Order(1)
    @DisplayName("IT-001: Compra de acciones - Flujo completo de extremo a extremo")
    void testBuyStock_EndToEnd() throws Exception {
        // Given: Usuario y acción existen en la base de datos
        String username = "testuser1";
        String ticker = "AAPL";

        // When: Se realiza una compra a través de la API
        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = username;
        buyRequest.ticker = ticker;
        buyRequest.name = "Apple Inc.";
        buyRequest.quantity = 10;
        buyRequest.amount = 1500.00f;

        given()
                .contentType(ContentType.JSON)
                .body(buyRequest)
                .when()
                .post("/transaction/buy")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("code", equalTo(0))
                .body("message", containsString("successfully"));

        // Then: Verificar persistencia en la base de datos

        // 1. Verificar que se creó la transacción
        List<Transaction> transactions = transactionRepo.findAll();
        assertFalse(transactions.isEmpty(), "Debe existir al menos una transacción");

        Transaction transaction = transactions.get(0);
        assertEquals("buy", transaction.getType(), "El tipo debe ser 'buy'");
        assertEquals(ticker, transaction.getTicker(), "El ticker debe coincidir");
        assertEquals(10, transaction.getQuantity(), "La cantidad debe ser 10");
        assertEquals(1500.00f, transaction.getAmount(), 0.01f, "El monto debe coincidir");

        // 2. Verificar que se creó/actualizó owned_stock
        List<OwnedStock> ownedStocks = ownedStockRepo.findByUser(userRepo.findByUsername(username));
        assertFalse(ownedStocks.isEmpty(), "El usuario debe tener acciones");

        OwnedStock ownedStock = ownedStocks.stream()
                .filter(os -> os.getTicker().equals(ticker))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Debe existir owned_stock para " + ticker));

        assertEquals(10, ownedStock.getQuantity(), "La cantidad de acciones debe ser 10");

        // 3. Verificar tiempo de respuesta < 500ms (ISO/IEC 25010)
        long startTime = System.currentTimeMillis();
        given()
                .contentType(ContentType.JSON)
                .body(buyRequest)
                .when()
                .post("/transaction/buy");
        long responseTime = System.currentTimeMillis() - startTime;

        assertTrue(responseTime < 500, "El tiempo de respuesta debe ser < 500ms, fue: " + responseTime + "ms");
    }

    /**
     * IT-002: Prueba de integración - Venta de acciones de extremo a extremo
     *
     * Objetivo: Validar el flujo completo de venta y actualización de owned_stock
     * ISO/IEC 25010: Fiabilidad (Madurez) - Integridad de datos
     */
    @Test
    @Order(2)
    @DisplayName("IT-002: Venta de acciones - Reducción de cantidad y eliminación si llega a cero")
    void testSellStock_EndToEnd() throws Exception {
        // Given: Usuario con acciones compradas
        String username = "testuser1";
        String ticker = "GOOGL";

        // Primero comprar 20 acciones
        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = username;
        buyRequest.ticker = ticker;
        buyRequest.name = "Alphabet Inc.";
        buyRequest.quantity = 20;
        buyRequest.amount = 3000.00f;

        given()
                .contentType(ContentType.JSON)
                .body(buyRequest)
                .when()
                .post("/transaction/buy")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // When: Se venden 10 acciones
        SellRequestDTO sellRequest = new SellRequestDTO();
        sellRequest.username = username;
        sellRequest.ticker = ticker;
        sellRequest.name = "Alphabet Inc.";
        sellRequest.quantity = 10;
        sellRequest.amount = 1500.00f;

        given()
                .contentType(ContentType.JSON)
                .body(sellRequest)
                .when()
                .post("/transaction/sell")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("code", equalTo(0));

        // Then: Verificar que owned_stock se redujo correctamente
        OwnedStock ownedStock = ownedStockRepo.findByUserAndTicker(
                userRepo.findByUsername(username), ticker);

        assertNotNull(ownedStock, "Debe existir owned_stock");
        assertEquals(10, ownedStock.getQuantity(), "La cantidad debe ser 10 (20 - 10)");

        // When: Se venden las 10 acciones restantes
        sellRequest.quantity = 10;

        given()
                .contentType(ContentType.JSON)
                .body(sellRequest)
                .when()
                .post("/transaction/sell")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // Then: Verificar que owned_stock se eliminó (cantidad = 0)
        OwnedStock deletedStock = ownedStockRepo.findByUserAndTicker(
                userRepo.findByUsername(username), ticker);

        assertNull(deletedStock, "owned_stock debe eliminarse cuando quantity = 0");

        // Verificar que las transacciones se registraron
        List<Transaction> transactions = transactionRepo.findAllByIssuer(userRepo.findByUsername(username));
        long sellTransactions = transactions.stream()
                .filter(t -> "sell".equals(t.getType()))
                .count();

        assertEquals(2, sellTransactions, "Deben existir 2 transacciones de venta");
    }

    /**
     * IT-003: Prueba de integración - Consulta de transacciones
     *
     * Objetivo: Validar filtrado correcto de transacciones por usuario
     * ISO/IEC 25010: Eficiencia del Desempeño (Comportamiento Temporal)
     */
    @Test
    @Order(3)
    @DisplayName("IT-003: Consulta de transacciones - Filtrado correcto por usuario")
    void testQueryTransactions_EndToEnd() throws Exception {
        // Limpiar transacciones antes de este test para asegurar datos limpios
        transactionRepo.deleteAll();
        ownedStockRepo.deleteAll();
        
        // Given: Múltiples transacciones para diferentes usuarios
        String user1 = "testuser1";
        String user2 = "testuser2";

        // Crear 3 transacciones para user1
        for (int i = 0; i < 3; i++) {
            BuyRequestDTO buyRequest = new BuyRequestDTO();
            buyRequest.username = user1;
            buyRequest.ticker = "MSFT";
            buyRequest.name = "Microsoft Corp.";
            buyRequest.quantity = 5;
            buyRequest.amount = 500.00f;

            given()
                    .contentType(ContentType.JSON)
                    .body(buyRequest)
                    .when()
                    .post("/transaction/buy");
        }

        // Crear 2 transacciones para user2
        for (int i = 0; i < 2; i++) {
            BuyRequestDTO buyRequest = new BuyRequestDTO();
            buyRequest.username = user2;
            buyRequest.ticker = "TSLA";
            buyRequest.name = "Tesla Inc.";
            buyRequest.quantity = 3;
            buyRequest.amount = 300.00f;

            given()
                    .contentType(ContentType.JSON)
                    .body(buyRequest)
                    .when()
                    .post("/transaction/buy");
        }

        // When: Se consultan transacciones para user1
        given()
                .queryParam("user", user1)
                .when()
                .get("/transaction/all")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].issuerUsername", equalTo(user1));

        // Then: Verificar en la base de datos
        List<Transaction> user1Transactions = transactionRepo.findAllByIssuer(
                userRepo.findByUsername(user1));

        assertEquals(3, user1Transactions.size(), "User1 debe tener 3 transacciones");

        List<Transaction> user2Transactions = transactionRepo.findAllByIssuer(
                userRepo.findByUsername(user2));

        assertEquals(2, user2Transactions.size(), "User2 debe tener 2 transacciones");
    }

    /**
     * IT-004: Prueba de integración - Transferencia de acciones entre usuarios
     *
     * Objetivo: Validar transferencia correcta y actualización de owned_stock para
     * ambos usuarios
     * ISO/IEC 25010: Fiabilidad (Madurez) - Consistencia de datos
     */
    @Test
    @Order(4)
    @DisplayName("IT-004: Transferencia de acciones - Actualización correcta para emisor y receptor")
    void testTransferStock_EndToEnd() throws Exception {
        // Given: user1 tiene 15 acciones de AMZN
        String issuer = "testuser1";
        String receptor = "testuser2";
        String ticker = "AMZN";

        // user1 compra 15 acciones
        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = issuer;
        buyRequest.ticker = ticker;
        buyRequest.name = "Amazon.com Inc.";
        buyRequest.quantity = 15;
        buyRequest.amount = 2000.00f;

        given()
                .contentType(ContentType.JSON)
                .body(buyRequest)
                .when()
                .post("/transaction/buy")
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // When: user1 transfiere 5 acciones a user2
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.issuerUsername = issuer;
        transferRequest.receptorUsername = receptor;
        transferRequest.ticker = ticker;
        transferRequest.name = "Amazon.com Inc.";
        transferRequest.quantity = 5;
        transferRequest.amount = 700.00f;

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/transaction/transfer")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("code", equalTo(0));

        // Then: Verificar owned_stock del emisor (debe tener 10)
        OwnedStock issuerStock = ownedStockRepo.findByUserAndTicker(
                userRepo.findByUsername(issuer), ticker);

        assertNotNull(issuerStock, "El emisor debe tener owned_stock");
        assertEquals(10, issuerStock.getQuantity(), "El emisor debe tener 10 acciones (15 - 5)");

        // Verificar owned_stock del receptor (debe tener 5)
        OwnedStock receptorStock = ownedStockRepo.findByUserAndTicker(
                userRepo.findByUsername(receptor), ticker);

        assertNotNull(receptorStock, "El receptor debe tener owned_stock");
        assertEquals(5, receptorStock.getQuantity(), "El receptor debe tener 5 acciones");

        // Verificar que se creó la transacción de tipo "transfer"
        List<Transaction> transactions = transactionRepo.findAll();
        Transaction transferTransaction = transactions.stream()
                .filter(t -> "transfer".equals(t.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Debe existir transacción de tipo transfer"));

        assertEquals(issuer, transferTransaction.getIssuer().getUsername(), "El emisor debe coincidir");
        assertEquals(receptor, transferTransaction.getReceptor().getUsername(), "El receptor debe coincidir");
        assertEquals(5, transferTransaction.getQuantity(), "La cantidad debe ser 5");
    }
}
