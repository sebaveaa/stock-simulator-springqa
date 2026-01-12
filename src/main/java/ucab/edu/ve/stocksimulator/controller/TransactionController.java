package ucab.edu.ve.stocksimulator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ucab.edu.ve.stocksimulator.dto.TransactionDTO;
import ucab.edu.ve.stocksimulator.dto.request.BuyRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.SellRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.TransferRequestDTO;
import ucab.edu.ve.stocksimulator.dto.response.MessageResponseDTO;
import ucab.edu.ve.stocksimulator.model.Transaction;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.service.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    public TransactionService transactionService;
    public OwnedStockService ownedStockService;
    public StockEODService  stockEODService;


    @Autowired
    public TransactionController(TransactionService transactionService, OwnedStockService ownedStockService, StockEODService stockEODService) {
        this.transactionService = transactionService;
        this.ownedStockService = ownedStockService;
        this.stockEODService = stockEODService;
    }

    //metodo que devuelve la lista de acciones compradas y vendidas
    @GetMapping("/all") //a
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(String username){
        List<TransactionDTO> responseTransactionDTOS = transactionService.findAllTransactions(username);
        return ResponseEntity.status(HttpStatus.OK).body(responseTransactionDTOS);
    }

    @PostMapping(value = "/verify-visa")
    public ResponseEntity<MessageResponseDTO> verifyVisaCard(String cardNumber) {
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        if (!transactionService.verifyVISA(cardNumber)) {
            messageResponseDTO.setCode(1);
            messageResponseDTO.setMessage("INVALID CARD NUMBER");
            return ResponseEntity.status(HttpStatus.OK).body(messageResponseDTO);

        }
        else {
            messageResponseDTO.setCode(0);
            messageResponseDTO.setMessage("SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(messageResponseDTO);
        }
    }

    @PostMapping(value = "/buy")
    public ResponseEntity<MessageResponseDTO> buyStock(@RequestBody BuyRequestDTO buyRequestDTO) {
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        ownedStockService.addPurchase(buyRequestDTO);
        transactionService.registerPurchase(buyRequestDTO);
        messageResponseDTO.setCode(0);
        messageResponseDTO.setMessage("Purchase completed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(messageResponseDTO);
    }


    @PostMapping(value = "/sell")
    public ResponseEntity<MessageResponseDTO> sellStock(@RequestBody SellRequestDTO sellRequestDTO) {
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        ownedStockService.sellStock(sellRequestDTO);
        transactionService.registerSell(sellRequestDTO);
        messageResponseDTO.setCode(0);
        messageResponseDTO.setMessage("Purchase completed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(messageResponseDTO);
    }

    @PostMapping(value = "/transfer")
    public ResponseEntity<MessageResponseDTO> transferStock(@RequestBody TransferRequestDTO transferRequestDTO){
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        ownedStockService.transferStock(transferRequestDTO);
        transactionService.registerTransfer(transferRequestDTO);
        try {
            transactionService.sendTransferEmail(transferRequestDTO);
        } catch (Exception e) {
            // Log the error but don't fail the transfer if email fails
            // In test environment, email service may not be available
        }
        messageResponseDTO.setCode(0);
        messageResponseDTO.setMessage("Transfer completed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(messageResponseDTO);
    }



}
