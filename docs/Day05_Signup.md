# 2026-08-10 회원가입 기능 구현 및 테스트

## 1. 작업 목표

이번 작업에서는 사용자 회원가입 기능을 완성하고, 단순히 API가 동작하는 것을 확인하는 데서 끝내지 않고 Service와 Controller 계층을 각각 테스트했다.

주요 목표는 다음과 같다.

* 사용자 회원가입 API 구현
* 이메일 중복 검사
* 비밀번호 암호화
* 회원가입 요청 Validation
* 회원가입 관련 예외 처리
* Spring Security 기본 설정
* Postman을 이용한 실제 요청 확인
* Service 단위 테스트
* Controller / Validation 테스트

---

## 2. 회원가입 요청 DTO

회원가입 요청은 `SignUpRequest` record로 구성했다.

주요 입력값:

* `email`
* `password`
* `name`
* `phone`

Bean Validation을 이용하여 입력값을 검증한다.

### Validation 규칙

#### Email

* 필수 입력
* 이메일 형식 검증
* 최대 100자

#### Password

* 필수 입력
* 최소 8자
* 최대 100자

#### Name

* 필수 입력
* 최대 50자

#### Phone

* 선택 입력
* 최대 50자

DTO는 값 전달이 주된 책임이고 별도의 상태 변경이 필요하지 않기 때문에 일반 클래스 대신 Java `record`를 사용했다.

---

## 3. User Entity

회원 정보를 저장하기 위한 `User` Entity를 구성했다.

주요 필드:

* `id`
* `email`
* `password`
* `name`
* `phone`

이메일에는 DB 레벨에서도 `unique` 제약조건을 적용했다.

애플리케이션의 이메일 중복 검사와 별개로 DB에서도 중복 데이터 저장을 방지한다.

---

## 4. UserRepository

Spring Data JPA의 `JpaRepository`를 사용했다.

회원가입 시 이메일 중복 여부를 확인하기 위해 다음 Repository 메서드를 사용한다.

```java
boolean existsByEmail(String email);
```

Spring Data JPA의 Query Method 기능을 이용하면 별도의 JPQL을 작성하지 않고 메서드 이름으로 이메일 존재 여부를 조회할 수 있다.

---

## 5. 회원가입 Service

회원가입의 핵심 비즈니스 로직은 `UserService`에서 처리한다.

처리 흐름:

```text
SignUpRequest
    ↓
이메일 중복 검사
    ↓
비밀번호 암호화
    ↓
User Entity 생성
    ↓
Repository 저장
    ↓
SignUpResponse 반환
```

### 이메일 중복 검사

```java
if (userRepository.existsByEmail(request.email())) {
    throw new DuplicateEmailException(request.email());
}
```

이미 가입된 이메일이면 회원가입을 진행하지 않고 `DuplicateEmailException`을 발생시킨다.

### 비밀번호 암호화

사용자가 입력한 비밀번호를 그대로 DB에 저장하지 않는다.

```java
String encodedPassword =
        passwordEncoder.encode(request.password());
```

암호화된 비밀번호를 `User` Entity에 전달한다.

```java
User user = new User(
        request.email(),
        encodedPassword,
        request.name(),
        request.phone()
);
```

여기서 중요한 점은 `request.password()`가 아니라 `encodedPassword`를 저장해야 한다는 것이다.

---

## 6. PasswordEncoder 설정

Spring Security의 `PasswordEncoder`를 Bean으로 등록했다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

회원가입에서는 `encode()`를 이용하여 비밀번호를 암호화한다.

향후 로그인 구현에서는 저장된 BCrypt 비밀번호를 복호화하는 것이 아니라 `matches()`를 이용하여 입력된 비밀번호와 비교할 예정이다.

---

## 7. Spring Security 설정

Spring Security를 적용하면서 회원가입 API는 인증되지 않은 사용자도 접근할 수 있도록 허용했다.

회원가입 요청:

```text
POST /api/auth/signup
```

회원가입은 로그인 이전에 사용하는 API이므로 `permitAll()` 대상이다.

Spring Security 적용 후 API 경로와 Security의 `requestMatchers()` 경로가 일치해야 한다는 점도 확인했다.

---

## 8. 회원가입 Controller

회원가입 API의 흐름:

```text
POST /api/auth/signup
        ↓
@RequestBody
        ↓
SignUpRequest
        ↓
@Valid
        ↓
UserService.signUp()
        ↓
SignUpResponse
        ↓
ApiResponse
        ↓
201 Created
```

회원가입 성공 시 `201 Created`를 반환한다.

Controller는 HTTP 요청/응답을 담당하고 실제 회원가입 규칙은 Service에 위임한다.

---

## 9. DuplicateEmailException

기존에 `IllegalArgumentException`으로 처리했던 이메일 중복 상황을 별도의 커스텀 예외로 분리했다.

```text
DuplicateEmailException
```

이메일 중복은 단순한 잘못된 메서드 인자가 아니라 애플리케이션의 명확한 비즈니스 규칙이기 때문에 전용 예외를 사용하는 것이 의도를 표현하기 쉽다.

---

## 10. GlobalExceptionHandler

`@RestControllerAdvice`를 이용하여 회원가입 관련 예외도 기존 전역 예외 처리 흐름에 포함했다.

이메일 중복 시:

```text
DuplicateEmailException
        ↓
GlobalExceptionHandler
        ↓
409 Conflict
        ↓
ErrorResponse
```

HTTP Status는 `409 Conflict`를 사용했다.

이를 통해 Controller 내부에서 직접 예외 응답을 생성하지 않고 공통 예외 처리 계층에서 동일한 응답 구조를 유지한다.

---

## 11. Postman 테스트

실제 애플리케이션을 실행하고 Postman을 이용하여 회원가입 API를 확인했다.

확인한 항목:

* 정상적인 회원가입 요청
* User 테이블 데이터 생성
* 암호화된 비밀번호 저장
* 동일 이메일 재가입
* 이메일 중복 시 409 응답
* Validation 실패 응답

테스트 코드를 작성하기 전에 실제 HTTP 요청을 보내 전체 흐름이 정상적으로 연결되는지 먼저 확인했다.

---

# 12. UserService 테스트

Mockito를 이용하여 Service 계층을 단위 테스트했다.

Service 테스트에서는 실제 DB를 사용하지 않고 `UserRepository`와 `PasswordEncoder`를 Mock으로 대체했다.

## 회원가입 성공 테스트

검증한 내용:

* 이메일 중복 검사 실행
* `PasswordEncoder.encode()` 호출
* 암호화된 비밀번호를 가진 User 저장
* 정상적인 `SignUpResponse` 반환

특히 `ArgumentCaptor<User>`를 사용하여 Repository에 실제로 전달된 User 객체를 확인했다.

```java
ArgumentCaptor<User> userCaptor =
        ArgumentCaptor.forClass(User.class);

verify(userRepository)
        .save(userCaptor.capture());

User savedUser = userCaptor.getValue();
```

이를 통해 단순히 `save()`가 호출됐는지만 확인하는 것이 아니라 실제 저장 대상의 비밀번호가 암호화된 값인지 검증했다.

## 이메일 중복 테스트

Repository의 이메일 존재 여부를 `true`로 설정했다.

```java
when(userRepository.existsByEmail(signUpRequest.email()))
        .thenReturn(true);
```

그리고 다음 예외가 발생하는지 확인했다.

```java
assertThrows(
        DuplicateEmailException.class,
        () -> userService.signUp(signUpRequest)
);
```

정상 회원가입에서는 `existsByEmail()`이 `false`, 중복 이메일 테스트에서는 `true`라는 서로 반대되는 조건을 사용한다는 점을 확인했다.

---

# 13. AuthController 테스트

`@WebMvcTest`와 `MockMvc`를 이용하여 회원가입 Controller를 테스트했다.

Controller 테스트에서는 Service 내부 로직을 다시 테스트하지 않는다.

```text
Controller Test
    → HTTP
    → JSON
    → Validation
    → Status Code
    → Exception Response

Service Test
    → 비즈니스 로직
    → Repository 호출
    → PasswordEncoder
```

계층별 책임을 분리하여 테스트한다.

---

## 14. 회원가입 성공 테스트

정상 요청을 전달했을 때 다음 내용을 확인했다.

* HTTP `201 Created`
* 성공 응답
* 회원 ID
* 이메일
* 이름
* 전화번호
* `UserService.signUp()` 호출

---

## 15. 이메일 중복 Controller 테스트

Service가 `DuplicateEmailException`을 발생시키도록 Mock을 설정했다.

이를 통해 다음 흐름을 확인했다.

```text
AuthController
    ↓
UserService
    ↓
DuplicateEmailException
    ↓
GlobalExceptionHandler
    ↓
409 Conflict
```

테스트 과정에서 expected 문자열과 실제 응답 문자열의 공백 차이도 테스트가 잡아내는 것을 확인했다.

---

## 16. Validation Controller 테스트

다음 Validation 조건을 테스트했다.

### Email

* 빈 문자열
* 100자 초과
* 잘못된 이메일 형식

### Password

* 빈 문자열
* 8자 미만
* 100자 초과

### Name

* 빈 문자열
* 50자 초과

### Phone

* 50자 초과

Validation 실패 시 공통적으로 다음을 확인한다.

* HTTP `400 Bad Request`
* ErrorResponse의 status
* ErrorResponse의 message
* 요청 path
* 필드별 Validation message
* UserService가 호출되지 않았는지 확인

---

## 17. Validation 테스트 Helper 분리

Validation 테스트를 작성하면서 동일한 MockMvc 코드가 반복되었다.

반복되는 코드를 다음 Helper 메서드로 분리했다.

### performSignUp()

회원가입 HTTP 요청 자체를 담당한다.

```java
private ResultActions performSignUp(
        SignUpRequest request
) throws Exception {
    return mockMvc.perform(
            post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    );
}
```

### expectValidationError()

Validation 실패 시 공통적으로 확인해야 하는 내용을 담당한다.

테스트마다 달라지는 것은 다음 세 가지뿐이다.

* 요청 객체
* 실패한 필드
* 예상 에러 메시지

이를 매개변수로 전달하여 반복 코드를 줄였다.

---

## 18. 테스트 데이터 작성 시 발견한 점

이메일 최대 길이 테스트 과정에서 하나의 테스트 데이터가 `@Size`와 `@Email`을 동시에 실패시키는 문제가 발생했다.

처음에는 긴 이메일을 단순히 생성했지만 이메일 local-part 자체가 지나치게 길어 `@Email` 검증도 함께 실패했다.

이 경험을 통해 다음 원칙을 확인했다.

> 하나의 테스트에서는 가능하면 하나의 조건만 실패하도록 테스트 데이터를 구성한다.

예를 들어 이메일 길이를 검증하려면:

```text
@Email → 통과
@Size  → 실패
```

하도록 데이터를 구성해야 해당 테스트가 정확히 길이 제한만 검증할 수 있다.

---

## 19. 테스트 메서드 한글 사용

테스트 메서드명에 한글을 사용했다.

예:

```java
void 이메일_중복_회원가입_실패()
```

프로덕션 코드는 영어 이름을 유지하되 테스트 코드는 요구사항처럼 읽을 수 있도록 한글 이름을 사용했다.

테스트의 목적을 빠르게 파악할 수 있다는 장점이 있었다.

---

# 20. 오늘 확인한 전체 회원가입 흐름

```text
Client / Postman
        ↓
POST /api/auth/signup
        ↓
Spring Security
        ↓
AuthController
        ↓
SignUpRequest
        ↓
Bean Validation
        ↓
UserService
        ↓
UserRepository.existsByEmail()
        ↓
PasswordEncoder.encode()
        ↓
User Entity
        ↓
UserRepository.save()
        ↓
Database
        ↓
SignUpResponse
        ↓
ApiResponse
        ↓
201 Created
```

예외가 발생하면:

```text
Validation 실패
        ↓
MethodArgumentNotValidException
        ↓
GlobalExceptionHandler
        ↓
400 Bad Request
```

또는:

```text
이메일 중복
        ↓
DuplicateEmailException
        ↓
GlobalExceptionHandler
        ↓
409 Conflict
```

---

# 21. 오늘 배운 핵심

## 테스트는 단순 성공 여부 확인이 아니다

테스트를 통해 다음과 같은 작은 문제도 발견할 수 있었다.

* 문자열 공백 차이
* 잘못 구성한 Validation 테스트 데이터
* 잘못된 JSON Path
* Service가 호출되면 안 되는 상황
* 암호화된 비밀번호가 실제 저장 객체에 들어가는지 여부

## 테스트 계층을 분리한다

Controller 테스트에서 Service 로직까지 모두 검증하려고 하지 않는다.

각 계층이 담당하는 책임을 기준으로 테스트한다.

## 반복되는 테스트 코드는 Helper로 분리할 수 있다

처음에는 MockMvc 흐름을 이해하기 위해 반복해서 직접 작성했다.

흐름을 이해한 이후에는 반복되는 요청과 검증 로직을 Helper 메서드로 분리하여 테스트의 의도가 더 잘 보이도록 개선했다.

## 에러 로그에서 expected / actual을 확인한다

테스트 실패 시 단순히 "테스트가 실패했다"고 보는 것이 아니라:

```text
expected
actual
```

을 비교하면 문제의 범위를 빠르게 좁힐 수 있다.

---

# 22. 다음 작업

다음 작업은 로그인 기능 구현이다.

예상 흐름:

```text
POST /api/auth/login
        ↓
LoginRequest
        ↓
이메일로 User 조회
        ↓
사용자 존재 여부 확인
        ↓
PasswordEncoder.matches()
        ↓
비밀번호 검증
        ↓
로그인 성공
        ↓
LoginResponse
```

로그인 자체를 먼저 완성한 뒤 JWT 인증/인가로 확장한다.

JWT를 바로 추가하기보다 먼저 다음 질문을 코드로 확인하는 것이 목표다.

> 이메일과 비밀번호를 이용해 서버가 사용자를 어떻게 인증하는가?

이후:

> 로그인에 성공한 사용자를 다음 HTTP 요청에서도 서버가 어떻게 식별하는가?

라는 문제를 JWT와 Spring Security를 통해 해결할 예정이다.
