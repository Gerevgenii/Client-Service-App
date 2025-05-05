package org.example.bank.jpaRepository;

import org.example.bank.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {}