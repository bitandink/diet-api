# Day04 - Profile / MySQL 연동 및 회원가입 구현

## 1. 오늘 작업 목표

기존 Meal CRUD API가 H2 Database를 기반으로 동작하는 상태에서 실제 MySQL을 연결하고, 실행 환경별 설정을 분리했다.

이후 Spring Security를 추가하고 `User` 도메인을 설계하여 이메일 기반 회원가입 기능의 기본 흐름을 구현했다.

오늘 구현한 전체 흐름은 다음과 같다.

```text
Spring Profile 분리
        ↓
MySQL 연동
        ↓
애플리케이션 전용 DB 계정
        ↓
User Entity
        ↓
SignUpRequest
        ↓
UserRepository
        ↓
PasswordEncoder / BCrypt
        ↓
UserService
        ↓
SignUpResponse
        ↓
AuthController
```

---

# 2. Spring Profile 분리

## Profile을 분리한 이유

기존에는 하나의 설정만 사용했지만 실제 프로젝트에서는 실행 환경에 따라 DB 주소, 계정, 로깅 설정 등이 달라질 수 있다.

예를 들면:

```text
local
→ 개발자 PC
→ 로컬 MySQL

prod
→ 운영 서버
→ 운영 DB
```

따라서 환경별 설정을 분리할 수 있도록 Spring Profile을 적용했다.

## local profile

`application-local.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diet
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update

    show-sql: true

    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE
```

실행 환경에서는 다음 환경변수를 설정한다.

```text
SPRING_PROFILES_ACTIVE=local
DB_USERNAME=diet_app
DB_PASSWORD=실제 비밀번호
```

중요한 점은 DB 비밀번호를 Git으로 관리되는 YAML 파일에 직접 작성하지 않는 것이다.

```yaml
password: ${DB_PASSWORD}
```

Spring Boot가 실행될 때 외부 환경변수 `DB_PASSWORD` 값을 읽어 사용한다.

---

# 3. MySQL 연결

기존 H2 대신 local 환경에서 MySQL을 사용하도록 변경했다.

연결 정보:

```text
Database : diet
User     : diet_app
URL      : jdbc:mysql://localhost:3306/diet
```

애플리케이션 전용 계정을 생성하고 `diet` 데이터베이스에 대한 권한을 부여했다.

Spring Boot 실행 로그를 통해 다음 항목을 확인했다.

```text
HikariPool 시작
        ↓
MySQL Connection 생성
        ↓
Hibernate 초기화
        ↓
MySQLDialect 사용
        ↓
JPA Entity 기반 테이블 생성/갱신
```

이를 통해 Spring Boot → HikariCP → MySQL → Hibernate 연결이 정상적으로 동작하는 것을 확인했다.

---

# 4. User Entity 설계

회원가입과 로그인을 위해 `User` Entity를 추가했다.

최소한의 회원 정보만 우선 관리한다.

```text
id
email
password
name
phone
```

역할:

```text
id
→ DB 내부 PK

email
→ 로그인 ID
→ 중복 불가

password
→ BCrypt로 해시된 비밀번호

name
→ 사용자 이름

phone
→ 연락처
→ 선택 입력
```

예시:

```java
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String phone;

    protected User() {
    }

    public User(
            String email,
            String password,
            String name,
            String phone
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }
}
```

## 기본 생성자

JPA가 Entity 객체를 생성할 수 있도록 기본 생성자를 제공한다.

```java
protected User() {
}
```

외부에서 의미 없이 생성하는 것은 제한하면서 JPA가 사용할 수 있도록 `protected`로 선언했다.

## Setter를 사용하지 않은 이유

모든 필드를 외부에서 자유롭게 변경할 수 있는 구조는 피한다.

```java
user.setPassword(...);
user.setEmail(...);
```

보다는 이후 필요한 경우:

```java
user.changePassword(...);
```

처럼 의미 있는 상태 변경 메서드를 제공하는 방향을 사용한다.

Getter는 DTO 변환 등 Entity 상태 조회가 필요하기 때문에 사용한다.

---

# 5. Request DTO와 Java record

회원가입 요청 DTO는 Java `record`를 사용했다.

```java
public record SignUpRequest(

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 100자까지 입력 가능합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 ~ 100자까지 입력 가능합니다."
        )
        String password,

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        @Size(max = 50, message = "이름은 50자까지 입력 가능합니다.")
        String name,

        @Size(max = 50, message = "연락처는 50자까지 입력 가능합니다.")
        String phone

) {
}
```

## record를 사용한 이유

DTO는 대부분 데이터를 전달하는 것이 주 역할이다.

일반 class를 사용하면 생성자, Getter 등의 반복 코드가 필요하지만 record를 사용하면 Java 문법 자체에서 이를 간결하게 표현할 수 있다.

일반 class:

```java
request.getEmail();
```

record:

```java
request.email();
```

record의 핵심 장점은 런타임 성능이나 메모리 절약보다는 다음에 있다.

```text
반복 코드 감소
불필요한 상태 변경 방지
DTO의 의도 명확화
유지보수성 향상
```

따라서 현재 프로젝트에서는 다음 기준을 사용한다.

```text
Entity
→ 일반 class

Request / Response DTO
→ record 적극 활용

Service / Controller
→ 일반 class
```

---

# 6. UserRepository

Spring Data JPA를 이용하여 Repository를 구현했다.

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
```

`existsByEmail()`은 Spring Data JPA의 Query Method 기능을 이용한다.

메서드 이름을 분석하면:

```text
exists
→ 존재 여부 반환

By
→ 조건 지정

Email
→ User.email 필드를 조건으로 사용
```

따라서:

```java
userRepository.existsByEmail("test@test.com");
```

결과는 다음과 같다.

```text
true
→ 이미 존재

false
→ 존재하지 않음
```

## DB unique와 existsByEmail의 차이

Entity에는 이미 다음 제약조건이 존재한다.

```java
@Column(nullable = false, unique = true)
private String email;
```

하지만 `existsByEmail()`도 사용한다.

두 기능의 책임이 다르기 때문이다.

```text
existsByEmail()
→ 애플리케이션 비즈니스 로직
→ 사용자에게 "이미 가입된 이메일"이라는 의미 있는 오류 제공

unique constraint
→ DB 최종 방어선
→ 실제 중복 데이터 저장 방지
```

둘 중 하나를 선택하는 것이 아니라 함께 사용한다.

---

# 7. Spring Security 추가

비밀번호를 안전하게 저장하기 위해 Spring Security를 프로젝트에 추가했다.

Spring Security가 활성화되면서 실행 시 기본 보안 설정이 적용되는 것도 확인했다.

서버 로그:

```text
This generated password is for development use only.
Your security configuration must be updated before
running your application in production.
```

이는 오류가 아니다.

Spring Security가 classpath에 존재하지만 아직 애플리케이션의 HTTP Security 정책을 직접 설정하지 않았기 때문에 기본 보안 설정이 동작하면서 출력되는 메시지다.

향후 `SecurityFilterChain`을 직접 설정한다.

---

# 8. PasswordEncoder Bean

Service가 구체적인 BCrypt 구현체에 직접 의존하지 않도록 `PasswordEncoder` 인터페이스를 사용한다.

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

관계는 다음과 같다.

```text
PasswordEncoder
→ 인터페이스

BCryptPasswordEncoder
→ PasswordEncoder 구현체
```

Spring Container에는 실제 `BCryptPasswordEncoder` 객체가 Bean으로 등록된다.

`UserService`에서는:

```java
private final PasswordEncoder passwordEncoder;
```

처럼 인터페이스에 의존한다.

```text
UserService
      ↓
PasswordEncoder 필요
      ↓
Spring Container
      ↓
BCryptPasswordEncoder Bean
      ↓
UserService에 DI
```

이를 통해 Service는 구체적인 암호화 구현 방식과 분리된다.

---

# 9. 비밀번호 저장

사용자가 입력한 비밀번호를 DB에 그대로 저장해서는 안 된다.

회원가입 시:

```java
String encodedPassword =
        passwordEncoder.encode(request.password());
```

로 BCrypt 해시를 생성한다.

```text
사용자 입력 비밀번호
        ↓
PasswordEncoder.encode()
        ↓
BCrypt
        ↓
해시 문자열
        ↓
DB 저장
```

중요한 점은 `encodedPassword`를 생성한 후 Entity에 반드시 해당 값을 전달해야 한다는 것이다.

잘못된 코드:

```java
User user = new User(
        request.email(),
        request.password(),
        request.name(),
        request.phone()
);
```

이 경우 평문 비밀번호가 저장된다.

올바른 코드:

```java
User user = new User(
        request.email(),
        encodedPassword,
        request.name(),
        request.phone()
);
```

비밀번호 저장은 일반적인 복호화 가능한 암호화보다 단방향 해시 개념으로 이해한다.

로그인 구현 시에는 DB 해시를 복호화하는 것이 아니라:

```java
passwordEncoder.matches(
        rawPassword,
        encodedPassword
);
```

방식으로 검증할 예정이다.

---

# 10. UserService 회원가입 로직

현재 회원가입 Service의 전체 흐름은 다음과 같다.

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "이미 가입된 이메일입니다."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone()
        );

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }
}
```

회원가입 흐름:

```text
SignUpRequest
      ↓
이메일 중복 검사
      ↓
비밀번호 BCrypt 해시
      ↓
User Entity 생성
      ↓
Repository.save()
      ↓
MySQL INSERT
      ↓
SignUpResponse 변환
```

현재 `IllegalArgumentException`은 임시 구현이다.

향후:

```text
DuplicateEmailException
        ↓
GlobalExceptionHandler
        ↓
적절한 HTTP Error Response
```

구조로 변경할 예정이다.

---

# 11. SignUpResponse

회원가입 성공 응답에서는 password를 반환하지 않는다.

```java
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
```

비밀번호가 BCrypt hash라고 하더라도 API Response에 노출해서는 안 된다.

따라서:

```text
SignUpRequest
├── email
├── password
├── name
└── phone

SignUpResponse
├── id
├── email
├── name
└── phone
```

처럼 Request와 Response의 책임을 분리한다.

---

# 12. AuthController

인증 관련 API를 `/api/auth` 하위에 구성한다.

```java
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
```

API:

```text
POST /api/auth/signup
```

Request 예시:

```json
{
  "email": "test@test.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

성공 시 HTTP Status:

```text
201 Created
```

---

# 13. 회원가입 전체 데이터 흐름

현재 구현된 전체 흐름을 정리하면 다음과 같다.

```text
Client
  │
  │ POST /api/auth/signup
  ▼
AuthController
  │
  │ @Valid
  ▼
SignUpRequest
  │
  ▼
UserService
  │
  ├── existsByEmail()
  │       ↓
  │   중복 이메일 검사
  │
  ├── passwordEncoder.encode()
  │       ↓
  │   BCrypt hash
  │
  ├── new User(...)
  │
  └── userRepository.save()
          ↓
        MySQL
          ↓
      savedUser
          ↓
 SignUpResponse.from()
          ↓
     ApiResponse
          ↓
     201 Created
```

---

# 14. 오늘 학습한 핵심 내용

## 설정과 런타임의 연결

`build.gradle`에 의존성을 추가하는 것은 단순히 코드를 import할 수 있게 하는 것만을 의미하지 않는다.

Spring Boot는 classpath에 존재하는 라이브러리를 기반으로 Auto Configuration을 수행한다.

예:

```text
Spring Data JPA 추가
→ JPA Auto Configuration

MySQL Connector 추가
→ MySQL 연결 가능

Spring Security 추가
→ Security Auto Configuration
```

따라서 서버 시작 로그를 읽으면 애플리케이션이 어떤 순서로 초기화되는지 확인할 수 있다.

## 서버 로그 읽기

기존에는 실행 로그를 단순히 지나쳤지만 다음과 같은 의미를 구분할 수 있게 되었다.

```text
Repository scanning
→ Spring Data Repository 검색

HikariPool
→ DB Connection Pool 초기화

Database JDBC URL
→ 실제 연결 DB 확인

Hibernate
→ JPA ORM 초기화

Tomcat started
→ HTTP 서버 기동 완료

Spring Security generated password
→ 기본 Security 설정 활성화
```

로그를 통해 문제 발생 위치를 좁힐 수 있다.

```text
Bean 생성 문제인가?
DB 연결 문제인가?
JPA 문제인가?
Security에서 차단됐나?
Controller까지 요청이 도착했나?
```

---

# 15. 현재 완료 상태

## 완료

* [x] Spring Profile 분리
* [x] YAML 설정 사용
* [x] `application-local.yml` 구성
* [x] DB username/password 환경변수 분리
* [x] MySQL Connector 적용
* [x] MySQL `diet` Database 연결
* [x] 애플리케이션 전용 `diet_app` 계정 사용
* [x] 기존 Meal CRUD MySQL 연결 확인
* [x] User Entity 설계
* [x] 이메일 기반 로그인 ID 결정
* [x] SignUpRequest 구현
* [x] DTO에 Java record 적용
* [x] 회원가입 Validation 적용
* [x] UserRepository 구현
* [x] 이메일 존재 여부 Query Method 구현
* [x] Spring Security 의존성 추가
* [x] PasswordEncoder Bean 등록
* [x] BCryptPasswordEncoder 적용
* [x] 회원가입 Service 기본 로직 구현
* [x] SignUpResponse 구현
* [x] AuthController 회원가입 Endpoint 구현
* [x] MySQL users 테이블 생성 확인

---

# 16. 다음 작업

다음 작업에서는 현재 작성한 회원가입 코드를 실제 HTTP 요청으로 검증한다.

우선순위:

```text
1. 애플리케이션 실행
2. POST /api/auth/signup 호출
3. Spring Security 접근 차단 여부 확인
4. SecurityFilterChain 설정
5. /api/auth/signup permitAll 설정
6. 정상 회원가입 요청 테스트
7. MySQL users 테이블 데이터 확인
8. BCrypt password 저장 확인
9. Validation 실패 테스트
10. 중복 이메일 요청 테스트
11. DuplicateEmailException 구현
12. GlobalExceptionHandler 연결
13. 회원가입 Service / Controller 테스트
```

회원가입 기능이 안정화된 이후 로그인 기능을 구현한다.

로그인 구현 예정 흐름:

```text
POST /api/auth/login
        ↓
email로 User 조회
        ↓
passwordEncoder.matches()
        ↓
비밀번호 검증
        ↓
JWT Access Token 생성
        ↓
클라이언트 응답
```

이후 JWT 인증 Filter를 추가하여 보호가 필요한 API에 인증/인가를 적용한다.

---

# 17. 현재 프로젝트 단계

현재 프로젝트는 단순 CRUD를 넘어 다음 단계로 진입했다.

```text
Meal CRUD
    ↓
Validation
    ↓
Exception Handling
    ↓
Layer Test
    ↓
JPA / Transaction
    ↓
Profile 분리
    ↓
MySQL
    ↓
User / 회원가입        ← 현재
    ↓
Spring Security
    ↓
Login
    ↓
JWT
    ↓
인증 / 인가
    ↓
Docker
    ↓
AWS
```

다음 작업에서는 회원가입 API의 실제 요청/응답을 검증하면서 Spring Security의 HTTP 인증/인가 설정을 학습한다.
