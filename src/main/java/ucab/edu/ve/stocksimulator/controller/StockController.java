package ucab.edu.ve.stocksimulator.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucab.edu.ve.stocksimulator.dto.OwnedStockDTO;
import ucab.edu.ve.stocksimulator.dto.StockDTO;
import ucab.edu.ve.stocksimulator.dto.response.MessageResponseDTO;
import ucab.edu.ve.stocksimulator.dto.response.StockListResponseDTO;
import ucab.edu.ve.stocksimulator.model.Stock;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;
import ucab.edu.ve.stocksimulator.service.StockEODService;
import ucab.edu.ve.stocksimulator.service.StockService;
import ucab.edu.ve.stocksimulator.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private  final StockService stockservice;
    private final StockEODService stockEODService;
    private final OwnedStockService ownedStockService;
    private final UserService userService;

    @Autowired
    public StockController(StockService stockservice, StockEODService stockEODService, OwnedStockService ownedStockService, UserService userService) {
        this.stockservice = stockservice;
        this.stockEODService = stockEODService;
        this.ownedStockService = ownedStockService;
        this.userService = userService;

    }

    @GetMapping("/all")
    public ResponseEntity<StockListResponseDTO> getAvailableStocks() {
        StockListResponseDTO responseStockDTO;
        responseStockDTO = stockservice.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(responseStockDTO);
    }
    @GetMapping("/{ticker}")
    public ResponseEntity<Object>getLatestStockEODData(@PathVariable String ticker){
        Object response = stockEODService.getLatestStockEODData(ticker);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/{ticker}/month")
    public ResponseEntity<Object>getLastMonthStockEODData(@PathVariable String ticker) {
        Object response = stockEODService.getLastMonthStockEODData(ticker);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ownedstocks/{user}")
    public ResponseEntity<List<OwnedStockDTO>> getOwnedStocksByUser(@PathVariable String user) {
        List<OwnedStockDTO> response = ownedStockService.getOwnedStocksByUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<MessageResponseDTO> createStocks(@RequestBody StockDTO[] stockDTOList) {
        for (StockDTO stockDTO : stockDTOList) {
            Stock stock = new Stock();
            stock.setDescription(stockDTO.description);
            stock.setName(stockDTO.name);
            stock.setTicker(stockDTO.ticker);
            this.stockservice.save(stock);
        }
        this.userService.createAdmin();
        MessageResponseDTO message = new MessageResponseDTO(0, "Acciones creadas");
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}