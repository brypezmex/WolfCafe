package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Handles global errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles global API exceptions
	 * @param exception a WolfCafeAPI exception
	 * @param webRequest the request that caused the exception
	 * @return a ResponseEntity encapsulating the exception information for presentation to the front end
	 */
    @ExceptionHandler(WolfCafeAPIException.class)
    public ResponseEntity<ErrorDetails> handleAPIException(WolfCafeAPIException exception, WebRequest webRequest) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
