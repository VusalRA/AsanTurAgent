package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class EmailNotValidException extends RuntimeException {
    public EmailNotValidException() {
        super("Email not valid");
    }
}
