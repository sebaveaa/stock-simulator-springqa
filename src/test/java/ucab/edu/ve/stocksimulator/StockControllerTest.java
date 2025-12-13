package ucab.edu.ve.stocksimulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucab.edu.ve.stocksimulator.controller.StockController;
import ucab.edu.ve.stocksimulator.service.StockEODService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockEODService stockEODService;

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
}
