package com.griotold.prompthub.adapter;

import com.griotold.prompthub.domain.category.DuplicateCategoryNameException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ProblemDetail handleDuplicateCategory(DuplicateCategoryNameException e) {
        return createProblemDetail(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException e) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, e);
    }

    /**
     * Bean Validation 에러 처리 - 부모 클래스 메서드 오버라이드
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", errors);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setProperty("exception", ex.getClass().getSimpleName());
        pd.setProperty("validationErrors", errors);

        return ResponseEntity.badRequest().body(pd);  // 수정된 부분
    }

    /**
     * Bean Validation 에러 처리 (일반적인 제약 조건 위반)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", errors);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setProperty("exception", e.getClass().getSimpleName());
        pd.setProperty("validationErrors", errors);
        return pd;
    }


    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private static ProblemDetail createProblemDetail(HttpStatus status, Exception e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        pd.setProperty("timestamp", LocalDateTime.now());
        pd.setProperty("exception", e.getClass().getSimpleName());
        return pd;
    }
}
