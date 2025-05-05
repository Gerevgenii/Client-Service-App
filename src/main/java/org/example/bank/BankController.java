package org.example.bank;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService service;
    public BankController(BankService service) {
        this.service = service;
    }

    @PostMapping("/clients")
    public Client createClient(@RequestParam String name) {
        return service.createClient(name);
    }

    @PostMapping("/clients/{id}/accounts")
    public Account createAccount(
            @PathVariable Long id,
            @RequestParam String currency,
            @RequestParam BigDecimal initial,
            @RequestParam String number
    ) {
        return service.createAccount(id, currency, initial, number);
    }

    @GetMapping("/clients/{id}/accounts")
    public List<Account> getAccounts(@PathVariable Long id) {
        return service.getAccounts(id);
    }

    @PostMapping("/transfer")
    public void transfer(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestParam Long clientId
    ) {
        service.transfer(from, to, amount, clientId);
    }
}