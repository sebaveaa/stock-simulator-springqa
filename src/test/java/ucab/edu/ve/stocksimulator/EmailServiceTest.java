package ucab.edu.ve.stocksimulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucab.edu.ve.stocksimulator.dto.request.TransferRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.UserRequestDTO;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.repository.TransactionRepo;
import ucab.edu.ve.stocksimulator.repository.UserRepo;
import ucab.edu.ve.stocksimulator.service.EmailSenderService;
import ucab.edu.ve.stocksimulator.service.TransactionService;
import ucab.edu.ve.stocksimulator.service.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private EmailSenderService emailSenderService;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testSendConfirmationEmail_SendsEmail() {
        // Arrange
        // Mocking DTO since source wasn't provided, assuming getters exist as used in UserService
        UserRequestDTO userRequestDTO = mock(UserRequestDTO.class);
        when(userRequestDTO.getEmail()).thenReturn("newuser@test.com");
        when(userRequestDTO.getUsername()).thenReturn("newuser");
        when(userRequestDTO.getFirstName()).thenReturn("John");

        String confirmationCode = "12345";

        // Act
        userService.sendConfirmationEmail(userRequestDTO, confirmationCode);

        // Assert
        // Verify the email service is called exactly once with the correct recipient
        verify(emailSenderService, times(1)).sendEmail(
                eq("newuser@test.com"),
                anyString(), // Subject
                anyString()  // Body
        );
    }

    @Test
    void testTransfer_EmailFailure() {
        // Arrange
        TransferRequestDTO transferDTO = new TransferRequestDTO();
        transferDTO.receptorUsername = "receptorUser";
        transferDTO.issuerUsername = "issuerUser";
        transferDTO.name = "Apple Inc.";
        transferDTO.quantity = 5;
        transferDTO.amount = 500.0f;

        User mockReceptor = new User();
        mockReceptor.setUsername("receptorUser");
        mockReceptor.setEmail("receptor@test.com");

        // The service looks up the user to get the email address
        when(userRepo.findByUsername("receptorUser")).thenReturn(mockReceptor);

        // Simulate the EmailService throwing a RuntimeException (e.g., SMTP timeout)
        doThrow(new RuntimeException("Email server connection failed"))
                .when(emailSenderService).sendEmail(anyString(), anyString(), anyString());

        // Act & Assert
        // Since sendTransferEmail does not catch exceptions, we expect it to bubble up.
        assertThrows(RuntimeException.class, () -> {
            transactionService.sendTransferEmail(transferDTO);
        });
    }
}

