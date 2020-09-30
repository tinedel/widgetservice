package ua.kiev.tinedel.widget.widgetservice.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.kiev.tinedel.widget.widgetservice.models.ValidationErrorResponse;
import ua.kiev.tinedel.widget.widgetservice.models.Violation;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {
        return ValidationErrorResponse.builder()
                .violations(e.getConstraintViolations().stream()
                        .map(violation ->
                                Violation.builder()
                                        .fieldName(violation.getPropertyPath().toString())
                                        .message(violation.getMessage())
                                        .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ValidationErrorResponse.builder()
                .violations(e.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> Violation.builder()
                                .fieldName(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
