package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class EmailAlreadyConfirmedException extends RuntimeException {
    public EmailAlreadyConfirmedException() {
        super("Email already confirmed");
    }
}
