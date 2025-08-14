package com.stockmarketproject.exception;

public class ApiException extends RuntimeException {
    public ApiException(String msg) { super(msg); }
}
