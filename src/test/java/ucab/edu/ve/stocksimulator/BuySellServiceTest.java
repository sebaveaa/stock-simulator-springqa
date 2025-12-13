package ucab.edu.ve.stocksimulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucab.edu.ve.stocksimulator.dto.request.BuyRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.SellRequestDTO;
import ucab.edu.ve.stocksimulator.model.OwnedStock;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.repository.OwnedStockRepo;
import ucab.edu.ve.stocksimulator.repository.UserRepo;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuySellServiceTest {

    @Mock
    private OwnedStockRepo ownedStockRepository;

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private OwnedStockService ownedStockService;

    @Test
    void testAddPurchase_NewStock() {
        // Arrange
        String username = "user1";
        String ticker = "AAPL";
        int quantityToBuy = 10;

        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = username;
        buyRequest.ticker = ticker;
        buyRequest.quantity = quantityToBuy;
        buyRequest.name = "Apple Inc.";

        User mockUser = new User();
        mockUser.setUsername(username);

        // Mock Repo returning Entity directly (not Optional)
        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        // Return null to simulate that the user does not own this stock yet
        when(ownedStockRepository.findByUserAndTicker(mockUser, ticker)).thenReturn(null);

        // Act
        ownedStockService.addPurchase(buyRequest);

        // Assert
        verify(ownedStockRepository).save(any(OwnedStock.class));
    }

    @Test
    void testAddPurchase_ExistingStock() {
        // Arrange
        String username = "user1";
        String ticker = "AAPL";
        int initialQuantity = 5;
        int quantityToBuy = 10;

        BuyRequestDTO buyRequest = new BuyRequestDTO();
        buyRequest.username = username;
        buyRequest.ticker = ticker;
        buyRequest.quantity = quantityToBuy;

        User mockUser = new User();
        OwnedStock existingStock = new OwnedStock();
        existingStock.setUser(mockUser);
        existingStock.setTicker(ticker);
        existingStock.setQuantity(initialQuantity);

        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        when(ownedStockRepository.findByUserAndTicker(mockUser, ticker)).thenReturn(existingStock);

        // Act
        ownedStockService.addPurchase(buyRequest);

        // Assert
        assertEquals(15, existingStock.getQuantity()); // 5 + 10
        verify(ownedStockRepository).save(existingStock);
    }

    @Test
    void testSellStock_Success_Partial() {
        // Arrange
        String username = "user1";
        String ticker = "AAPL";
        int quantityOwned = 20;
        int quantityToSell = 5;

        SellRequestDTO sellRequest = new SellRequestDTO();
        sellRequest.username = username;
        sellRequest.ticker = ticker;
        sellRequest.quantity = quantityToSell;

        User mockUser = new User();
        OwnedStock mockOwnedStock = new OwnedStock();
        mockOwnedStock.setQuantity(quantityOwned);
        mockOwnedStock.setUser(mockUser);
        mockOwnedStock.setTicker(ticker);

        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        when(ownedStockRepository.findByUserAndTicker(mockUser, ticker)).thenReturn(mockOwnedStock);

        // Act
        ownedStockService.sellStock(sellRequest);

        // Assert
        assertEquals(15, mockOwnedStock.getQuantity()); // 20 - 5
        verify(ownedStockRepository).save(mockOwnedStock);
        // Note: Balance check removed as User model lacks balance field
    }

    @Test
    void testSellStock_ZeroQuantity_DeletesRecord() {
        // Arrange
        String username = "user1";
        String ticker = "AAPL";
        int quantityOwned = 10;
        int quantityToSell = 10; // Selling everything

        SellRequestDTO sellRequest = new SellRequestDTO();
        sellRequest.username = username;
        sellRequest.ticker = ticker;
        sellRequest.quantity = quantityToSell;

        User mockUser = new User();
        OwnedStock mockOwnedStock = new OwnedStock();
        mockOwnedStock.setQuantity(quantityOwned);
        mockOwnedStock.setUser(mockUser);
        mockOwnedStock.setTicker(ticker);

        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        when(ownedStockRepository.findByUserAndTicker(mockUser, ticker)).thenReturn(mockOwnedStock);

        // Act
        ownedStockService.sellStock(sellRequest);

        // Assert
        verify(ownedStockRepository).delete(mockOwnedStock);
    }
}
