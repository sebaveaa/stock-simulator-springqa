package ucab.edu.ve.stocksimulator.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Utilidad para cargar datos de prueba desde archivos SQL.
 * Cumple con ISO/IEC 29119 - Test Data Management.
 *
 * Esta clase facilita la carga de datos de prueba consistentes
 * para pruebas de integración y carga.
 */
@Component
public class TestDataLoader {

    /**
     * Carga el script SQL de usuarios de prueba.
     *
     * @param dataSource DataSource de la base de datos de prueba
     * @throws SQLException si hay error al ejecutar el script
     */
    public static void loadTestUsers(DataSource dataSource) throws SQLException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("test-data/seed-users.sql"));
        populator.execute(dataSource);
    }

    /**
     * Carga el script SQL de acciones (stocks) de prueba.
     *
     * @param dataSource DataSource de la base de datos de prueba
     * @throws SQLException si hay error al ejecutar el script
     */
    public static void loadTestStocks(DataSource dataSource) throws SQLException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("test-data/seed-stocks.sql"));
        populator.execute(dataSource);
    }

    /**
     * Carga todos los datos de prueba (usuarios y acciones).
     *
     * @param dataSource DataSource de la base de datos de prueba
     * @throws SQLException si hay error al ejecutar los scripts
     */
    public static void loadAllTestData(DataSource dataSource) throws SQLException {
        loadTestStocks(dataSource);  // Cargar acciones primero
        loadTestUsers(dataSource);   // Luego usuarios
    }

    /**
     * Limpia todas las tablas para empezar con datos frescos.
     *
     * @param dataSource DataSource de la base de datos de prueba
     * @throws SQLException si hay error al limpiar las tablas
     */
    public static void cleanDatabase(DataSource dataSource) throws SQLException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSeparator(";");
        populator.execute(dataSource);
    }
}
