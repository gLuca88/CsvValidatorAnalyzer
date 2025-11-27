package com.gianluca.security.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	// JSON malformato → 400
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleUnreadableJson(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON non valido o malformato");
	}

	// Campi non validi → 400
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campi non validi");
	}

	// Content-Type errato → 415
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<String> handleMediaType(HttpMediaTypeNotSupportedException ex) {
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Content-Type non supportato");
	}

	// Metodo HTTP errato → 405
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<String> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Metodo HTTP non consentito");
	}
}