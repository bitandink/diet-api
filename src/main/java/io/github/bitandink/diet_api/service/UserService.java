// src/main/java/io/github/bitandink/diet_api/service/UserService.java

package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.SignUpRequest;
import io.github.bitandink.diet_api.dto.SignUpResponse;
import io.github.bitandink.diet_api.entity.User;
import io.github.bitandink.diet_api.exception.DuplicateEmailException;
import io.github.bitandink.diet_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 2. 평문 비밀번호를 BCrypt로 해시
        String encodedPassword =
                passwordEncoder.encode(request.password());

        // 3. User Entity 생성
        User user = new User(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone()
        );

        // 4. DB 저장
        User savedUser = userRepository.save(user);

        // 5. Entity → Response DTO 변환
        return SignUpResponse.from(savedUser);
    }
}