package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.SignUpRequest;
import io.github.bitandink.diet_api.dto.SignUpResponse;
import io.github.bitandink.diet_api.exception.DuplicateEmailException;
import io.github.bitandink.diet_api.repository.UserRepository;
import io.github.bitandink.diet_api.entity.User;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        user = new User (
                "test@test.com",
                "password123",
                "홍길동",
                "010-1234-1234"

        );

        signUpRequest = new SignUpRequest(user.getEmail(), user.getPassword(), user.getName(), user.getPhone());
    }

    @Test
    @DisplayName("회원으로 등록한다")
    void signUp() {

        when(userRepository.existsByEmail(signUpRequest.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(signUpRequest.password()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SignUpResponse response =
                userService.signUp(signUpRequest);


        assertEquals(
                "test@test.com",
                response.email()
        );

        assertEquals(
                "홍길동",
                response.name()
        );

        assertEquals(
                "010-1234-1234",
                response.phone()
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser =
                userCaptor.getValue();

        assertEquals(
                "encodedPassword",
                savedUser.getPassword()
        );

        verify(passwordEncoder)
                .encode("password123");
    }

    @Test
    @DisplayName("이메일 중복 가입일 경우 회원가입에 실패한다")
    void 이메일_중복인_경우_회원가입_실패() {
        when(userRepository.existsByEmail(signUpRequest.email()))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> userService.signUp(signUpRequest)
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}
