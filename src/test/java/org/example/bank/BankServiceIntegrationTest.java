package org.example.bank;

import org.example.bank.jpaRepository.AccountRepository;
import org.example.bank.jpaRepository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankServiceIntegrationTest {

    @Autowired
    private BankService bankService;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private AccountRepository accountRepo;

    @BeforeEach
    void cleanDatabase() {
        accountRepo.deleteAll();
        clientRepo.deleteAll();
    }

    @Test
    void testCreatePersistClient() {
        final Client client = bankService.createClient("Max");
        assertThat(client.getId()).isNotNull();
        assertThat(client.getName()).isEqualTo("Max");
    }

    @Test
    void testCreatePersistAccount() {
        final Client client = bankService.createClient("Alex");
        final Account account = bankService.createAccount(
                client.getId(), "USD", new BigDecimal("200.00"), "A"
        );
        assertThat(account.getId()).isNotNull();
        assertThat(account.getAmount()).isEqualByComparingTo("200.00");
        final List<Account> accounts = accountRepo.findAllByClientId(client.getId());
        assertThat(accounts).hasSize(1);
    }

    @Test
    void testGetAccountsAccess() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "EUR", new BigDecimal("100"), "A");
        bankService.createAccount(client.getId(), "EUR", new BigDecimal("150"), "B");

        final List<Account> accounts = bankService.getAccounts(client.getId());
        assertThat(accounts).hasSize(2);
    }

    @Test
    void testGetAccountsFailureId() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "EUR", new BigDecimal("100"), "A");

        assertThrows(IllegalArgumentException.class,
                () -> bankService.getAccounts(1212L)
        );
    }

    @Test
    void testTransferAccess() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("200"), "A");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("100"), "B");

        bankService.transfer("A", "B", new BigDecimal("50"), client.getId());

        final Account from = accountRepo.findByAccountNumber("A").orElseThrow();
        final Account to   = accountRepo.findByAccountNumber("B").orElseThrow();
        assertThat(from.getAmount()).isEqualByComparingTo("150.00");
        assertThat(to.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void testTransferDifferentCurrencies() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("100"), "A");
        bankService.createAccount(client.getId(), "EUR", new BigDecimal("100"), "B");

        assertThrows(IllegalArgumentException.class, () ->
                bankService.transfer("A", "B", new BigDecimal("10"), client.getId())
        );
    }

    @Test
    void testTransferInsufficientFunds() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("20"), "A");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("0"), "B");

        assertThrows(IllegalArgumentException.class, () ->
                bankService.transfer("Aa", "B", new BigDecimal("50"), client.getId())
        );
    }

    @Test
    void testTransferToTheSameAccount() {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("100"), "A");

        assertThrows(IllegalArgumentException.class, () ->
                bankService.transfer("A", "A", new BigDecimal("10"), client.getId())
        );
    }

    @Test
    void testTransferFromOtherClient() {
        final Client c1 = bankService.createClient("Alex");
        final Client c2 = bankService.createClient("Max");
        bankService.createAccount(c1.getId(), "USD", new BigDecimal("100"), "A");
        bankService.createAccount(c2.getId(), "USD", new BigDecimal("100"), "B");

        assertThrows(SecurityException.class, () ->
                bankService.transfer("A", "B", new BigDecimal("10"), c2.getId())
        );
    }

    @Test
    void testConcurrentTransfers() throws InterruptedException {
        final Client client = bankService.createClient("Alex");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("1000"), "A");
        bankService.createAccount(client.getId(), "USD", new BigDecimal("0"),    "B");

        final int threadsCount = 10;
        //noinspection resource
        final ExecutorService worker = Executors.newFixedThreadPool(threadsCount);
        final CountDownLatch latch = new CountDownLatch(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            worker.execute(() -> {
                try {
                    bankService.transfer("A", "B", new BigDecimal("50"), client.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        worker.shutdown();

        final Account from = accountRepo.findByAccountNumber("A").orElseThrow();
        final Account to   = accountRepo.findByAccountNumber("B").orElseThrow();
        assertThat(from.getAmount()).isEqualByComparingTo("500.00");
        assertThat(to.getAmount()).isEqualByComparingTo("500.00");
    }
}