package io.github.bitandink.diet_api.exception;

import io.github.bitandink.diet_api.entity.Meal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MealNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMealNotFound(MealNotFoundException exception) {
        Map<String, String> response = Map.of(
                "message", exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
