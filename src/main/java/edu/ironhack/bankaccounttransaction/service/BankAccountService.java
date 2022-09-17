package edu.ironhack.bankaccounttransaction.service;

import edu.ironhack.bankaccounttransaction.exception.InvalidIBANException;
import edu.ironhack.bankaccounttransaction.exception.NotEnoughBalanceException;
import edu.ironhack.bankaccounttransaction.exception.SystemFailException;
import edu.ironhack.bankaccounttransaction.model.BankAccount;
import edu.ironhack.bankaccounttransaction.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public void addBalanceAmount(String iban, BigDecimal amount) throws InvalidIBANException {
        Optional<BankAccount> optBankAccountSource = bankAccountRepository.findByIBAN(iban);

        if (optBankAccountSource.isEmpty()) {
            throw new InvalidIBANException("The account bank with IBAN " + iban + " doesn't exists");
        }

        BankAccount bankAccount = optBankAccountSource.get();

        // Si es transaccional da igual el orden
        bankAccount.setBalance(bankAccount.getBalance().add(amount));

        bankAccountRepository.save(bankAccount);
    }

    public void transfer(@NotNull(message = "IBAN mustn't be empty") String ibanSource,
                         @NotNull(message = "IBAN mustn't be empty") String ibanDestination,
                         @DecimalMin(message = "The amount must be above zero", value = "0.01") BigDecimal amount,
                         boolean isSuccessfulEnd)
            throws InvalidIBANException, NotEnoughBalanceException, SystemFailException {

        Optional<BankAccount> optBankAccountSource = bankAccountRepository.findByIBAN(ibanSource);
        Optional<BankAccount> optBankAccountDestination = bankAccountRepository.findByIBAN(ibanDestination);

        if (optBankAccountSource.isEmpty()) {
            throw new InvalidIBANException("The account bank with IBAN " + ibanSource + " doesn't exists");
        }

        if (optBankAccountDestination.isEmpty()) {
            throw new InvalidIBANException("The account bank with IBAN " + ibanDestination + " doesn't exists");
        }

        BankAccount source = optBankAccountSource.get();
        BankAccount destination = optBankAccountDestination.get();

        if (source.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughBalanceException("The balance is not enough to do this operation");
        }

        // Si es transaccional da igual el orden
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        if (isSuccessfulEnd) {
            transferSuccess(source, destination);
        }
        else {
            transferFail(source);
        }
    }

    public void transferSuccess(BankAccount source, BankAccount destination) {
        bankAccountRepository.save(source);
        bankAccountRepository.save(destination);
    }

    public void transferFail(BankAccount source) throws SystemFailException {
        bankAccountRepository.save(source);
        throw new SystemFailException("Simulo que de repente falla el sistema");
        //bankAccountRepository.save(destination);
    }

    public boolean isTransactional() {
        for(Annotation annotation : getClass().getAnnotations()){
            if(annotation instanceof Transactional){
                return true;
            }
        }
        return false;
    }
}
