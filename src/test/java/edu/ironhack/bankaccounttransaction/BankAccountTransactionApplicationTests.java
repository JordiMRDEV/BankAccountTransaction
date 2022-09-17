package edu.ironhack.bankaccounttransaction;

import edu.ironhack.bankaccounttransaction.exception.InvalidIBANException;
import edu.ironhack.bankaccounttransaction.exception.NotEnoughBalanceException;
import edu.ironhack.bankaccounttransaction.exception.SystemFailException;
import edu.ironhack.bankaccounttransaction.model.BankAccount;
import edu.ironhack.bankaccounttransaction.repository.BankAccountRepository;
import edu.ironhack.bankaccounttransaction.service.BankAccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BankAccountTransactionApplicationTests {

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountRepository bankAccountRepository;

    private final static String IBAN_SOURCE = "ES1320952591908522383887";
    private final static String IBAN_DESTINATION = "ES3101284562156139923123";


    @BeforeEach
    public void setUp() {
        BankAccount source = new BankAccount();
        source.setIBAN(IBAN_SOURCE);
        bankAccountRepository.save(source);

        BankAccount destination = new BankAccount();
        destination.setIBAN(IBAN_DESTINATION);
        bankAccountRepository.save(destination);
    }

    @AfterEach
    public void setDown() {
        bankAccountRepository.deleteAll();
    }

    @Test
    void notEnoughBalanceTest() {
        assertThrows(NotEnoughBalanceException.class, () -> { bankAccountService.transfer(IBAN_SOURCE, IBAN_DESTINATION, BigDecimal.TEN, true); });
    }

    @Test
    void notValidIbanSourceTest() {
        assertThrows(InvalidIBANException.class, () -> { bankAccountService.transfer("ES1234567890", IBAN_DESTINATION, BigDecimal.TEN, true); });
    }

    @Test
    void notValidIbanDestinationTest() {
        assertThrows(InvalidIBANException.class, () -> { bankAccountService.transfer(IBAN_SOURCE, "ES1234567890", BigDecimal.TEN, true); });
    }

    @Test
    void successEndTest() {

        try {
            bankAccountService.addBalanceAmount(IBAN_SOURCE, new BigDecimal("100"));
            bankAccountService.transfer(IBAN_SOURCE, IBAN_DESTINATION, BigDecimal.TEN, true);

            BankAccount bankAccountSource = bankAccountRepository.findByIBAN(IBAN_SOURCE).get();
            BankAccount bankAccountDestination = bankAccountRepository.findByIBAN(IBAN_DESTINATION).get();

            assertEquals(new BigDecimal("90.00"), bankAccountSource.getBalance());
            assertEquals(new BigDecimal("10.00"), bankAccountDestination.getBalance());
        }
        catch (InvalidIBANException | NotEnoughBalanceException | SystemFailException e) {
            Assertions.fail();
        }
    }

    @Test
    void failEndTest() {
        // Comprobamos si la clase es transaccional @Transactional
        boolean isTransactional = bankAccountService.isTransactional();

        try {
            bankAccountService.addBalanceAmount(IBAN_SOURCE, new BigDecimal("100"));
            bankAccountService.transfer(IBAN_SOURCE, IBAN_DESTINATION, BigDecimal.TEN, false);
        }
        catch (InvalidIBANException | NotEnoughBalanceException | SystemFailException e) {
            assertEquals(e.getClass(), SystemFailException.class);

            BankAccount bankAccountSource = bankAccountRepository.findByIBAN(IBAN_SOURCE).get();
            BankAccount bankAccountDestination = bankAccountRepository.findByIBAN(IBAN_DESTINATION).get();

            if (!isTransactional) {
                // Cuando NO anotamos la clase del servicio como @Transactional la base de datos queda en un estado inconsistente.
                // Es decir, que la cuenta de origen ha quedado sin el saldo transferido y la cuenta de destino no ha recibido el saldo.
                assertEquals(new BigDecimal("90.00"), bankAccountSource.getBalance());
                assertEquals(new BigDecimal("0.00"), bankAccountDestination.getBalance());
            }
            else {
                // Cuando anotamos la clase del servicio como @Transactional la base de datos queda en un estado consistente al acabarse la ejecución de cualquier método.
                assertEquals(new BigDecimal("100.00"), bankAccountSource.getBalance());
                assertEquals(new BigDecimal("0.00"), bankAccountDestination.getBalance());
            }
        }
    }
}
