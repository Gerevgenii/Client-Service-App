package org.example.bank.jpaRepository;

import org.example.bank.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByClientId(Long clientId);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :number")
    Optional<Account> findByAccountNumberForUpdate(@Param("number") String number);
}