package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmailNotFound extends RuntimeException {
    public EmailNotFound() {
        super("Email not found");
    }
}
