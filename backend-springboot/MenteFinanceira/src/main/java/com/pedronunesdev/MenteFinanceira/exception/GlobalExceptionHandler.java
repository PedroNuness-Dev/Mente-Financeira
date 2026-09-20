package com.pedronunesdev.MenteFinanceira.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Todas as exceções tratadas de forma genérica
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllException(Exception ex, WebRequest request){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response,httpStatus);
    }

    // Exceções de recursos não encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response,httpStatus);
    }

    // Exceções de argumentos ilegais
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request){
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response,httpStatus);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<ExceptionResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request){
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                "Usuário ou senha inválidos",
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, httpStatus);
    }

    // Exceções de argumentos ilegais
    @ExceptionHandler(CarteiraDoUsuarioJaExistenteException.class)
    public final ResponseEntity<ExceptionResponse> handleCarteiraDoUsuarioJaExistenteException(CarteiraDoUsuarioJaExistenteException ex, WebRequest request){
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response,httpStatus);
    }
}
