// src/main/java/io/github/bitandink/diet_api/controller/AuthController.java

package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.ApiResponse;
import io.github.bitandink.diet_api.dto.SignUpRequest;
import io.github.bitandink.diet_api.dto.SignUpResponse;
import io.github.bitandink.diet_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {

        SignUpResponse signUpResponse =
                userService.signUp(request);

        ApiResponse<SignUpResponse> response =
                new ApiResponse<>(
                        true,
                        "회원 등록 성공",
                        signUpResponse
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}