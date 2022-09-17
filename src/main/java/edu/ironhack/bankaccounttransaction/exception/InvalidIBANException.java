package edu.ironhack.bankaccounttransaction.exception;

public class InvalidIBANException extends Exception {
    public InvalidIBANException(String message) {
        super(message);
    }
}
