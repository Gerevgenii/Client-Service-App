package org.example.bank;

import org.example.bank.jpaRepository.AccountRepository;
import org.example.bank.jpaRepository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankService {

    private final ClientRepository clientRepo;
    private final AccountRepository accountRepo;

    public BankService(ClientRepository clientRepo, AccountRepository accountRepo) {
        this.clientRepo = clientRepo;
        this.accountRepo = accountRepo;
    }

    public Client createClient(String name) {
        return clientRepo.save(new Client(name));
    }

    public Account createAccount(Long clientId, String currency, BigDecimal initial, String number) {
        final Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return accountRepo.save(new Account(client, currency, initial, number));
    }

    public List<Account> getAccounts(Long clientId) {
        if (!clientRepo.existsById(clientId))
            throw new IllegalArgumentException("Client not found");
        return accountRepo.findAllByClientId(clientId);
    }

    @Transactional
    public void transfer(String fromNum, String toNum, BigDecimal amount, Long callerClientId) {
        if (fromNum.equals(toNum))
            throw new IllegalArgumentException("Cannot transfer to the same account");

        final Account from = accountRepo.findByAccountNumberForUpdate(fromNum)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        final Account to = accountRepo.findByAccountNumberForUpdate(toNum)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (!from.getCurrency().equals(to.getCurrency()))
            throw new IllegalArgumentException("Different currencies");
        if (!from.getClient().getId().equals(callerClientId))
            throw new SecurityException("Cannot transfer from another client's account");
        if (from.getAmount().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient funds");

        from.setAmount(from.getAmount().subtract(amount));
        to.setAmount(to.getAmount().add(amount));
    }
}