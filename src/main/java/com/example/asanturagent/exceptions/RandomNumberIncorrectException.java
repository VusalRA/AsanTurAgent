package com.example.asanturagent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RandomNumberIncorrectException extends RuntimeException {
    public RandomNumberIncorrectException() {
        super("Random number is not correct");
    }
}
