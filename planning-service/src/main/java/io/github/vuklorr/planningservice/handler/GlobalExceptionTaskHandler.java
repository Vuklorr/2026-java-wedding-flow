package io.github.vuklorr.planningservice.handler;

import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionTaskHandler {

    @ExceptionHandler(WeddingTaskNotFoundException.class)
    public ProblemDetail handleWeddingNotFound (WeddingTaskNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

}
