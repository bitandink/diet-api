// src/main/java/io/github/bitandink/diet_api/dto/SignUpResponse.java

package io.github.bitandink.diet_api.dto;

import io.github.bitandink.diet_api.entity.User;

public record SignUpResponse(
        Long id,
        String email,
        String name,
        String phone
) {

    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone()
        );
    }
}