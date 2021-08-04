package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class RequestException extends RuntimeException {
    public RequestException() {
        super("You cannot send a new request to the archive");
    }
}