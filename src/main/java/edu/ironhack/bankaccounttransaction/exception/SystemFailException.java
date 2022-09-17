package edu.ironhack.bankaccounttransaction.exception;

public class SystemFailException extends RuntimeException {
    public SystemFailException(String message) {
        super(message);
    }
}
