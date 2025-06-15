package until.the.eternity.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.common.exception.dto.ErrorResponse;
import until.the.eternity.common.exception.enums.ErrorCode;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("CustomException: {}", code.getMessage(), ex);
        return new ResponseEntity<>(
                ErrorResponse.of(code.getCode(), code.getMessage()),
                HttpStatus.valueOf(Integer.parseInt(code.getCode())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errorMessage =
                ex.getBindingResult().getFieldError() != null
                        ? ex.getBindingResult().getFieldError().getDefaultMessage()
                        : "입력값이 올바르지 않습니다.";
        log.warn("Validation Error: {}", errorMessage);
        return new ResponseEntity<>(ErrorResponse.of("400", errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        log.warn("ConstraintViolation: {}", ex.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.of("400", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("MethodNotAllowed: {}", ex.getMethod());
        return new ResponseEntity<>(
                ErrorResponse.of("405", "허용되지 않은 HTTP 메서드입니다."), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return new ResponseEntity<>(
                ErrorResponse.of("500", "예상치 못한 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
