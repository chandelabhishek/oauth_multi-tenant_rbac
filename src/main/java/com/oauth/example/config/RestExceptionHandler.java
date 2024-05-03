package com.oauth.example.config;

import com.oauth.example.domain.exception.NotFoundException;
import com.oauth.example.domain.exception.TokenInvalidException;
import com.oauth.example.domain.exception.UserDeactivatedException;
import com.oauth.example.domain.exception.UserPasswordDidNotMatchException;
import com.oauth.example.domain.model.ErrorResponse;
import com.oauth.example.domain.model.FieldError;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RestControllerAdvice()
public class RestExceptionHandler {
    private static final Logger logger = LogManager.getLogger(RestExceptionHandler.class);

    private ErrorResponse getErrorResponse(Exception exception, HttpStatusCode httpStatusCode) {
        exception.printStackTrace();
        logger.error(exception);
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus(httpStatusCode.value());
        errorResponse.setException(exception.getClass().getSimpleName());
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }

    private List<FieldError> getFieldErrors(BindException exception) {
        return exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    final FieldError fieldError = new FieldError();
                    fieldError.setErrorCode(error.getDefaultMessage());
                    fieldError.setField(error.getField());
                    return fieldError;
                })
                .toList();
    }


    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(final ChangeSetPersister.NotFoundException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            final ResponseStatusException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, exception.getStatusCode()), exception.getStatusCode());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException exception) {
        ErrorResponse errorResponse = getErrorResponse(exception, exception.getStatusCode());
        errorResponse.setFieldErrors(getFieldErrors(exception));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        ErrorResponse errorResponse = getErrorResponse(exception, exception.getStatusCode());
        errorResponse.setFieldErrors(getFieldErrors(exception));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserPasswordDidNotMatchException.class)
    public ResponseEntity<ErrorResponse> handleUserPasswordDidNotMatchException(
            UserPasswordDidNotMatchException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.UNPROCESSABLE_ENTITY), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserDeactivatedException.class)
    public ResponseEntity<ErrorResponse> handleUserDeactivatedException(
            UserDeactivatedException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<Object> handleTokenInvalidException(TokenInvalidException exception) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request) {
        return new ResponseEntity<>(getErrorResponse(exception, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Throwable.class)
    @ApiResponse(responseCode = "4xx/5xx", description = "Error")
    public ResponseEntity<ErrorResponse> handleThrowable(final Throwable exception) {
        return new ResponseEntity<>(getErrorResponse((Exception) exception, HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

