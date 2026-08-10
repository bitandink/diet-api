package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.SignUpRequest;
import io.github.bitandink.diet_api.dto.SignUpResponse;
import io.github.bitandink.diet_api.exception.DuplicateEmailException;
import io.github.bitandink.diet_api.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * AuthController만 대상으로 하는 MVC 슬라이스 테스트.
 *
 * 전체 Spring 애플리케이션을 실행하는 것이 아니라
 * Controller, Validation, JSON 변환, 예외 처리 등
 * 웹 계층에서 필요한 부분을 중심으로 테스트한다.
 *
 * UserService는 실제 객체를 사용하지 않고 Mock으로 대체한다.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /*
     * 실제 HTTP 서버를 띄우지 않고도
     * Controller에 HTTP 요청을 보내는 것처럼 테스트할 수 있다.
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * SignUpRequest 객체를 JSON 문자열로 변환하기 위해 사용한다.
     *
     * SignUpRequest
     *      ↓
     * JSON
     *      ↓
     * MockMvc POST 요청
     */
    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Controller 테스트에서는 UserService의 비즈니스 로직을
     * 다시 테스트하지 않는다.
     *
     * UserServiceTest에서 이미 Service 로직을 검증했으므로,
     * 여기서는 Mock으로 교체해서 Controller의 역할에 집중한다.
     */
    @MockitoBean
    private UserService userService;


    /*
     * =========================================================
     * 회원가입 성공
     * =========================================================
     *
     * 검증 흐름:
     *
     * 올바른 JSON 요청
     *      ↓
     * @Valid 통과
     *      ↓
     * AuthController
     *      ↓
     * UserService.signUp()
     *      ↓
     * 201 Created + 정상 JSON 응답
     */
    @Test
    @DisplayName("회원가입에 성공하면 201을 반환한다")
    void 회원가입_성공() throws Exception {

        // given
        // Validation을 모두 통과할 수 있는 정상적인 회원가입 요청
        SignUpRequest request = validRequest();

        /*
         * 실제 Service를 실행하지 않기 때문에
         * Service가 반환할 결과도 우리가 직접 정해준다.
         *
         * 비밀번호는 Response에 포함하지 않는다.
         */
        SignUpResponse response = new SignUpResponse(
                1L,
                "test@test.com",
                "홍길동",
                "010-1234-1234"
        );

        /*
         * UserService.signUp()이 호출되면
         * 위에서 만든 response를 반환하도록 Mock 동작을 설정한다.
         */
        when(userService.signUp(any(SignUpRequest.class)))
                .thenReturn(response);

        // when & then
        performSignUp(request)

                // 회원가입 성공이므로 HTTP 201
                .andExpect(status().isCreated())

                // 공통 ApiResponse 확인
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원 등록 성공"))

                // 회원가입 결과 데이터 확인
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-1234"));

        /*
         * Controller가 실제로 Service의 회원가입 메서드를
         * 호출했는지도 확인한다.
         */
        verify(userService)
                .signUp(any(SignUpRequest.class));
    }


    /*
     * =========================================================
     * 중복 이메일
     * =========================================================
     *
     * Service에서 DuplicateEmailException이 발생했을 때
     * GlobalExceptionHandler가 이를 409 Conflict로
     * 변환하는지 확인한다.
     */
    @Test
    @DisplayName("중복 이메일이면 409를 반환한다")
    void 이메일_중복_회원가입_실패() throws Exception {

        // given
        SignUpRequest request = validRequest();

        /*
         * 실제 DB에서 이메일 중복을 검사하지 않는다.
         *
         * Controller가 Service를 호출했을 때
         * 이미 가입된 이메일이라고 가정하여
         * DuplicateEmailException을 발생시키도록 설정한다.
         */
        when(userService.signUp(any(SignUpRequest.class)))
                .thenThrow(new DuplicateEmailException(request.email()));

        // when & then
        performSignUp(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))

                /*
                 * DuplicateEmailException의 실제 메시지와
                 * 동일하게 맞춰야 한다.
                 */
                .andExpect(jsonPath("$.message")
                        .value("이미 가입된 이메일입니다. " + request.email()))

                .andExpect(jsonPath("$.path")
                        .value("/api/auth/signup"));
    }


    /*
     * =========================================================
     * Email Validation
     * =========================================================
     */

    @Test
    @DisplayName("회원 등록 요청에서 이메일이 비어 있으면 400을 반환한다")
    void 이메일_블랭크_회원가입_실패() throws Exception {

        /*
         * 다른 값은 모두 정상으로 두고
         * email만 잘못된 값으로 만든다.
         *
         * 이렇게 해야 이 테스트가 실패했을 때
         * 원인이 email Validation이라는 것을 명확하게 알 수 있다.
         */
        SignUpRequest request = new SignUpRequest(
                "",
                "password123",
                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "email",
                "이메일은 필수 입력 항목입니다."
        );
    }


    @Test
    @DisplayName("회원 등록 요청에서 이메일이 100자를 넘으면 400을 반환한다")
    void 이메일_글자수_초과_회원가입_실패() throws Exception {

        /*
         * 긴 문자열을 직접 작성하지 않고 repeat()를 사용한다.
         *
         * 테스트 코드를 읽는 사람도
         * "아, 의도적으로 100자를 넘겼구나"
         * 라는 것을 바로 알 수 있다.
         *
         * 동시에 이메일 형식은 최대한 유지해서
         * @Email이 아니라 @Size 검증을 테스트하도록 한다.
         */
        String longEmail =
                "test@" +
                "a".repeat(50) +
                "." +
                "b".repeat(50) +
                ".com";

        SignUpRequest request = new SignUpRequest(
                longEmail,
                "password123",
                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "email",
                "이메일은 100자까지 입력 가능합니다."
        );
    }


    @Test
    @DisplayName("회원 등록 요청에서 이메일 형식이 맞지 않으면 400을 반환한다")
    void 이메일_형식_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test.test.com",
                "password123",
                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "email",
                "올바른 이메일 형식이 아닙니다."
        );
    }


    /*
     * =========================================================
     * Password Validation
     * =========================================================
     */

    @Test
    @DisplayName("회원 등록 요청에서 비밀번호가 비어 있으면 400을 반환한다")
    void 비밀번호_블랭크_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "",
                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "password",
                "비밀번호는 필수 입력 항목입니다."
        );
    }


    @Test
    @DisplayName("회원 등록 요청에서 비밀번호가 100자를 넘으면 400을 반환한다")
    void 비밀번호_글자수_초과_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",

                // 최대 100자이므로 101자로 실패 상황을 만든다.
                "a".repeat(101),

                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "password",
                "비밀번호는 8자 ~ 100자까지 입력 가능합니다."
        );
    }


    @Test
    @DisplayName("회원 등록 요청에서 비밀번호가 8자 미만이면 400을 반환한다")
    void 비밀번호_글자수_부족_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "test",
                "홍길동",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "password",
                "비밀번호는 8자 ~ 100자까지 입력 가능합니다."
        );
    }


    /*
     * =========================================================
     * Name Validation
     * =========================================================
     */

    @Test
    @DisplayName("회원 등록 요청에서 이름이 비어 있으면 400을 반환한다")
    void 이름_블랭크_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123",
                "",
                "010-1234-1234"
        );

        expectValidationError(
                request,
                "name",
                "이름은 필수 입력 항목입니다."
        );
    }


    @Test
    @DisplayName("회원 등록 요청에서 이름이 50자를 넘으면 400을 반환한다")
    void 이름_글자수_초과_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123",

                // 최대 50자이므로 51자로 실패 상황을 만든다.
                "가".repeat(51),

                "010-1234-1234"
        );

        expectValidationError(
                request,
                "name",
                "이름은 50자까지 입력 가능합니다."
        );
    }


    /*
     * =========================================================
     * Phone Validation
     * =========================================================
     */

    @Test
    @DisplayName("회원 등록 요청에서 전화번호가 50자를 넘으면 400을 반환한다")
    void 전화번호_글자수_초과_회원가입_실패() throws Exception {

        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123",
                "홍길동",

                // 최대 50자이므로 51자로 실패 상황을 만든다.
                "1".repeat(51)
        );

        expectValidationError(
                request,
                "phone",
                "연락처는 50자까지 입력 가능합니다."
        );
    }


    /*
     * =========================================================
     * Test Helper
     * =========================================================
     *
     * 아래 메서드들은 테스트 자체가 아니라
     * 여러 테스트에서 반복되는 코드를 모아놓은 Helper다.
     */


    /*
     * 모든 Validation을 통과하는 기본 회원가입 요청.
     *
     * 정상 데이터를 매 테스트마다 반복해서 작성하지 않기 위해
     * 하나의 메서드로 분리했다.
     *
     * 테스트에서는 검증하고 싶은 필드만 다른 값으로 바꾸면 된다.
     */
    private SignUpRequest validRequest() {
        return new SignUpRequest(
                "test@test.com",
                "password123",
                "홍길동",
                "010-1234-1234"
        );
    }


    /*
     * 회원가입 POST 요청을 보내는 공통 로직.
     *
     * 기존에는 모든 테스트마다 아래 코드를 반복했다.
     *
     * mockMvc.perform(
     *     post("/api/auth/signup")
     *         .contentType(...)
     *         .content(...)
     * )
     *
     * 요청 URL과 JSON 변환 방식은 모든 테스트에서 동일하므로
     * 하나의 메서드로 추출했다.
     *
     * ResultActions를 반환하기 때문에 호출한 테스트에서
     * .andExpect(...)를 이어서 작성할 수 있다.
     */
    private ResultActions performSignUp(
            SignUpRequest request
    ) throws Exception {

        return mockMvc.perform(
                post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );
    }


    /*
     * Validation 실패 테스트에서 반복되는 검증을 하나로 모았다.
     *
     * 매 Validation 테스트에서 달라지는 것은 사실상:
     *
     * 1. 잘못된 request
     * 2. 실패한 field
     * 3. 예상하는 error message
     *
     * 이 세 가지뿐이다.
     *
     * 따라서 달라지는 값만 매개변수로 받고
     * 공통 검증은 여기서 처리한다.
     */
    private void expectValidationError(
            SignUpRequest request,
            String field,
            String message
    ) throws Exception {

        performSignUp(request)

                // Validation 실패 → HTTP 400
                .andExpect(status().isBadRequest())

                // GlobalExceptionHandler가 만든 ErrorResponse 확인
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/auth/signup"))

                /*
                 * field가 "email"이면:
                 * $.errors.email
                 *
                 * field가 "password"이면:
                 * $.errors.password
                 *
                 * 처럼 동적으로 JSON 경로를 만든다.
                 */
                .andExpect(jsonPath("$.errors." + field)
                        .value(message));

        /*
         * @Valid에서 요청이 차단되었다면
         * Controller 내부의 userService.signUp()까지
         * 실행되어서는 안 된다.
         *
         * 따라서 Service와 아무 상호작용도 없었음을 검증한다.
         */
        verifyNoInteractions(userService);
    }
}