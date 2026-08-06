package io.github.bitandink.diet_api.exception;

public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(Long id) {
        super("해당 식단을 찾을 수 없습니다. id=" + id);
    }
}
