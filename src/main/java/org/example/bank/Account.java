package org.example.bank;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    private String currency;
    private BigDecimal amount;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    public Account() {}
    public Account(Client client, String currency, BigDecimal amount, String accountNumber) {
        this.client = client;
        this.currency = currency;
        this.amount = amount;
        this.accountNumber = accountNumber;
    }
    public Long getId() { return id; }
    public Client getClient() { return client; }
    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    public String getAccountNumber() { return accountNumber; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}