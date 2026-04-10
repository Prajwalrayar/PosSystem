package com.zosh.exception;


import com.zosh.payload.response.ApiErrorResponse;
import com.zosh.payload.response.ExceptionResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {


	@ExceptionHandler(UserException.class)
	public ResponseEntity<ExceptionResponse> UserExceptionHandler(
			UserException ex, WebRequest req) {
		ExceptionResponse response = new ExceptionResponse(
				ex.getMessage(),
				req.getDescription(false), LocalDateTime.now());
		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ApiErrorResponse(
						"Access denied",
						List.of(new ApiErrorResponse.FieldErrorResponse("authorization", ex.getMessage()))
				));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		List<ApiErrorResponse.FieldErrorResponse> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::toFieldError)
				.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiErrorResponse("Validation failed", errors));
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(new ApiErrorResponse(
						"Validation failed",
						List.of(new ApiErrorResponse.FieldErrorResponse(ex.getField(), ex.getMessage()))
				));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ExceptionResponse> AuthenticationExceptionHandler(
			AuthenticationException ex, WebRequest req) {
		ExceptionResponse response = new ExceptionResponse(
				ex.getMessage(),
				req.getDescription(false),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ExceptionResponse> BadCredentialsExceptionHandler(
			BadCredentialsException ex, WebRequest req) {
		ExceptionResponse response = new ExceptionResponse(
				ex.getMessage(),
				req.getDescription(false),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ExceptionResponse> ResourceNotFoundExceptionHandler(
			ResourceNotFoundException ex, WebRequest req) {
		ExceptionResponse response = new ExceptionResponse(
				ex.getMessage(),
				req.getDescription(false),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
	}



	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
			DataIntegrityViolationException ex,WebRequest req) {

		Map<String, Object> response = new HashMap<>();
		response.put("message", ex.getMessage());
		response.put("error", req.getDescription(false));
		response.put("timestamp", LocalDateTime.now());
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}




	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> ExceptionHandler(Exception ex,
															  WebRequest req) {
		ExceptionResponse response = new ExceptionResponse(
				ex.getMessage(),
				req.getDescription(false),
				LocalDateTime.now()
		);

		return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
	}

	private ApiErrorResponse.FieldErrorResponse toFieldError(FieldError error) {
		return new ApiErrorResponse.FieldErrorResponse(error.getField(), error.getDefaultMessage());
	}

}
