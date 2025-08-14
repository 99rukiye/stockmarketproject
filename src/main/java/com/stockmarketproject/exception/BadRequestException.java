package com.stockmarketproject.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String msg) { super(msg); }
}
