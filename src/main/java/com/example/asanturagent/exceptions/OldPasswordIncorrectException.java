package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)

public class OldPasswordIncorrectException extends RuntimeException {
    public OldPasswordIncorrectException() {
        super("Old password is incorrect");
    }
}
