package edu.ironhack.bankaccounttransaction.repository;

import edu.ironhack.bankaccounttransaction.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByIBAN(String iban);
}