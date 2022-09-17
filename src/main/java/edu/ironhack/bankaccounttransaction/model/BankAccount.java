package edu.ironhack.bankaccounttransaction.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String IBAN;
    private BigDecimal balance;

    public BankAccount() {
        balance = BigDecimal.ZERO;
    }
}
