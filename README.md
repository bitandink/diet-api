# 🍱 Diet API

Spring Boot 기반 식단 관리 REST API입니다.

식단의 영양 정보를 등록·조회·수정·삭제하는 CRUD 기능을 시작으로  
Validation, 예외 처리, 테스트, JPA/Transaction, MySQL 연동, 회원가입 및 인증/인가 기능을 단계적으로 학습하고 적용하는 프로젝트입니다.

현재 Meal CRUD, MySQL 연동, 이메일 기반 회원가입과 Spring Security 기본 설정까지 완료했으며,  
다음 단계로 로그인 및 JWT 기반 인증/인가 기능을 구현할 예정입니다.

---

## 🎯 Project Goal

이 프로젝트의 핵심 목표는 UI를 만드는 것이 아니라, 하나의 HTTP 요청이 백엔드 내부에서 어떻게 처리되는지 직접 구현하면서 전체 흐름을 이해하는 것입니다.

```text
HTTP Request
    │
    ▼
Spring Security
    │
    ▼
Controller
    │
    ▼
Validation
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
    │
    ▼
HTTP Response
```

기능 구현뿐만 아니라 각 계층의 책임, 예외 처리, 데이터 검증 및 테스트까지 함께 작성하면서 프론트엔드에서 사용하는 REST API가 서버 내부에서 어떻게 동작하는지 학습하고 있습니다.

---

## 🛠 Tech Stack

### Backend

- Java 17
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Bean Validation
- Spring Security
- Lombok

### Database

- MySQL 8
  - Local 개발 환경
- H2 Database
  - JPA / Repository Test 환경

### Security

- Spring Security
- BCrypt Password Encoder
- JWT
  - 구현 예정

### Test

- JUnit 5
- Mockito
- MockMvc

### Build

- Gradle

---

## 🏗 Architecture

```text
Client
  │
  ▼
Spring Security
  │
  ▼
Controller
  │
  ▼
Service
  │
  ▼
Repository
  │
  ▼
Database
```

각 계층의 책임을 분리하여 구성합니다.

### Controller

- HTTP 요청 / 응답 처리
- Request Validation
- Service 호출
- HTTP Status 결정
- 공통 Response 반환

### Service

- 비즈니스 로직 처리
- Transaction 관리
- Entity / DTO 변환
- 이메일 중복 검사
- 비밀번호 해시 처리

### Repository

- Spring Data JPA를 통한 데이터 접근
- Query Method를 통한 조회 및 존재 여부 확인

### Entity

- Database Table Mapping
- 도메인 상태 관리

### Global Exception Handler

- Validation 예외 처리
- 리소스 조회 실패 처리
- 비즈니스 예외 처리
- 공통 ErrorResponse 생성

---

## 📌 API

### Meal API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/meals` | 전체 식단 조회 |
| GET | `/api/meals/{id}` | 특정 식단 조회 |
| POST | `/api/meals` | 식단 등록 |
| PUT | `/api/meals/{id}` | 식단 수정 |
| DELETE | `/api/meals/{id}` | 식단 삭제 |

### Auth API

| Method | Endpoint | Description | Status |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | 회원가입 | 완료 |
| POST | `/api/auth/login` | 로그인 | 예정 |

---

## 🍽 Meal

Meal은 식단의 기본 영양 정보를 관리합니다.

```text
Meal
├── id
├── mealName
├── calories
├── carbohydrate
├── protein
└── fat
```

JPA Entity를 기반으로 CRUD API를 구현했으며 수정 시 영속성 컨텍스트와 Dirty Checking을 활용합니다.

---

## 👤 User

회원가입 및 인증 기능을 위해 User 도메인을 구성했습니다.

```text
User
├── id
├── email
├── password
├── name
└── phone
```

각 필드의 역할은 다음과 같습니다.

- `id`
  - Database Primary Key

- `email`
  - 로그인 ID
  - 중복 불가
  - 최대 100자

- `password`
  - 평문 저장 금지
  - BCrypt 해시값 저장

- `name`
  - 사용자 이름
  - 최대 50자

- `phone`
  - 선택 입력 연락처
  - 최대 50자

---

## 📝 Sign Up

회원가입의 기본 데이터 흐름은 다음과 같습니다.

```text
POST /api/auth/signup
        │
        ▼
Spring Security
        │
        ▼
SignUpRequest
        │
        ▼
Bean Validation
        │
        ▼
UserService
        │
        ├── 이메일 중복 검사
        │
        ├── BCrypt Password Encoding
        │
        ├── User Entity 생성
        │
        └── UserRepository.save()
        │
        ▼
MySQL
        │
        ▼
SignUpResponse
        │
        ▼
201 Created
```

### Request

```json
{
  "email": "test@test.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

회원가입 요청 DTO에는 Bean Validation을 적용합니다.

```text
email
→ 필수
→ 이메일 형식
→ 최대 100자

password
→ 필수
→ 8 ~ 100자

name
→ 필수
→ 최대 50자

phone
→ 선택
→ 최대 50자
```

### Response

비밀번호는 회원가입 Response에 포함하지 않습니다.

```json
{
  "success": true,
  "message": "회원 등록 성공",
  "data": {
    "id": 1,
    "email": "test@test.com",
    "name": "홍길동",
    "phone": "010-1234-5678"
  }
}
```

---

## 📦 DTO

HTTP Request / Response와 JPA Entity를 분리합니다.

```text
HTTP Request
      │
      ▼
Request DTO
      │
      ▼
Service
      │
      ▼
Entity
      │
      ▼
Database
```

Entity를 HTTP Response로 직접 반환하지 않고 Response DTO로 변환합니다.

회원가입 DTO부터 Java `record`를 사용합니다.

```java
public record SignUpRequest(
        String email,
        String password,
        String name,
        String phone
) {
}
```

`record`는 데이터 전달이 주목적인 DTO에서 반복적인 생성자와 Getter 코드를 줄이고, 불필요한 상태 변경 가능성을 제한하기 위해 사용합니다.

---

## 🔐 Password Security

사용자의 비밀번호는 평문으로 저장하지 않습니다.

Spring Security의 `PasswordEncoder` 인터페이스와 `BCryptPasswordEncoder` 구현체를 사용합니다.

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

회원가입 시:

```java
String encodedPassword =
        passwordEncoder.encode(request.password());
```

전체 흐름:

```text
Raw Password
      │
      ▼
PasswordEncoder.encode()
      │
      ▼
BCrypt
      │
      ▼
Password Hash
      │
      ▼
Database
```

로그인 구현 시에는 저장된 Hash를 복호화하지 않고 `matches()`를 사용하여 입력한 비밀번호와 저장된 해시값을 비교할 예정입니다.

```java
passwordEncoder.matches(
        rawPassword,
        encodedPassword
);
```

---

## 🛡 Spring Security

Spring Security의 `SecurityFilterChain`을 이용하여 HTTP 요청별 접근 정책을 설정합니다.

회원가입 API는 인증되지 않은 사용자도 접근할 수 있도록 허용합니다.

```java
http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/signup")
                .permitAll()
                .anyRequest()
                .authenticated()
        );
```

현재 단계에서는:

```text
POST /api/auth/signup
→ permitAll()

그 외 요청
→ authenticated()
```

정책을 적용하고 있습니다.

향후 로그인 API를 추가하면 `/api/auth/login` 역시 인증 없이 접근할 수 있도록 허용하고, JWT를 이용하여 나머지 요청의 인증 여부를 판단하도록 확장할 예정입니다.

---

## ✅ Validation

Request DTO에 Bean Validation을 적용합니다.

### Meal

- 식단 이름 필수
- 식단 이름 최대 50자
- 칼로리 및 영양 정보 필수
- 칼로리 및 영양 정보 0 이상

### Sign Up

- 이메일 필수
- 이메일 형식 검증
- 이메일 최대 100자
- 비밀번호 필수
- 비밀번호 8 ~ 100자
- 이름 필수
- 이름 최대 50자
- 연락처 최대 50자

Validation 실패 시 필드별 오류 정보를 반환합니다.

```json
{
  "status": 400,
  "message": "입력값이 올바르지 않습니다.",
  "path": "/api/auth/signup",
  "errors": {
    "email": "올바른 이메일 형식이 아닙니다."
  }
}
```

Validation 실패 요청은 Service 계층까지 전달되지 않습니다.

---

## 🚨 Exception Handling

`@RestControllerAdvice`를 사용하여 API 예외를 공통으로 처리합니다.

### MealNotFoundException

존재하지 않는 식단을 조회, 수정 또는 삭제할 경우 발생합니다.

```text
HTTP 404 Not Found
```

처리 흐름:

```text
MealService
    │
    ▼
MealNotFoundException
    │
    ▼
GlobalExceptionHandler
    │
    ▼
404 Not Found
```

### MethodArgumentNotValidException

Request DTO의 Bean Validation이 실패할 경우 처리합니다.

```text
HTTP 400 Bad Request
```

처리 흐름:

```text
Request
    │
    ▼
@Valid
    │
    ▼
MethodArgumentNotValidException
    │
    ▼
GlobalExceptionHandler
    │
    ▼
400 Bad Request
```

### DuplicateEmailException

이미 가입된 이메일로 회원가입을 요청할 경우 발생합니다.

```text
HTTP 409 Conflict
```

처리 흐름:

```text
UserService
    │
    ▼
DuplicateEmailException
    │
    ▼
GlobalExceptionHandler
    │
    ▼
409 Conflict
```

애플리케이션에서는 `existsByEmail()`로 이메일 중복 여부를 사전에 확인합니다.

```java
boolean existsByEmail(String email);
```

DB에서는 `UNIQUE` 제약조건을 유지하여 중복 데이터 저장을 최종적으로 방지합니다.

```text
existsByEmail()
→ 사용자에게 의미 있는 비즈니스 오류 제공

UNIQUE Constraint
→ DB의 최종 데이터 정합성 보장
```

---

## 💾 Database

### Local

Local 환경에서는 MySQL을 사용합니다.

```text
Spring Boot
    │
    ▼
HikariCP
    │
    ▼
MySQL Connector/J
    │
    ▼
MySQL 8
```

Database:

```text
diet
```

애플리케이션은 MySQL 관리자 `root` 계정을 직접 사용하지 않고 전용 계정을 사용합니다.

```text
diet_app
```

애플리케이션 계정에는 `diet` Database에 대한 권한을 부여했습니다.

### Test

테스트 목적에 따라 실제 DB 사용 여부를 구분합니다.

```text
Service Unit Test
→ Mockito
→ DB 사용 안 함

Controller Test
→ MockMvc + Mock Service
→ DB 사용 안 함

JPA / Repository Test
→ H2 Database
```

외부 MySQL에 의존하지 않도록 JPA 테스트에서는 H2 In-Memory Database를 사용합니다.

---

## ⚙️ Spring Profile

실행 환경별 설정을 분리했습니다.

```text
src/main/resources
├── application.yml
├── application-local.yml
└── application-test.yml
```

### `application.yml`

모든 환경에서 사용하는 공통 설정을 관리합니다.

### `application-local.yml`

Local 개발 환경 설정을 관리합니다.

- MySQL DataSource
- Hibernate 설정
- SQL Log

예:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/diet
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### `application-test.yml`

JPA / Repository 테스트 환경에서 사용할 H2 Database 설정을 관리합니다.

### Environment Variables

Profile과 DB 계정 정보는 실행 환경에서 주입합니다.

```text
SPRING_PROFILES_ACTIVE=local
DB_USERNAME=diet_app
DB_PASSWORD=********
```

DB 비밀번호를 Git으로 관리되는 YAML 파일에 직접 저장하지 않습니다.

---

## 🔄 Transaction

Service 계층에서 Transaction 경계를 관리합니다.

### 조회

```java
@Transactional(readOnly = true)
```

### 등록 / 수정 / 삭제

```java
@Transactional
```

Meal 수정에서는 JPA Dirty Checking을 활용합니다.

회원가입 역시 Service의 Transaction 범위 안에서 User를 저장합니다.

---

## 🧪 Test

계층별 책임에 따라 테스트를 분리합니다.

---

### Controller Test

`@WebMvcTest`와 `MockMvc`를 활용하여 웹 계층을 테스트합니다.

주요 검증 대상:

- HTTP Status
- Response JSON
- Request Validation
- Exception Handling
- Controller → Service 호출
- Validation 실패 시 Service 미호출

회원가입 API에서는 다음 케이스를 검증합니다.

```text
회원가입 성공
→ 201 Created

중복 이메일
→ 409 Conflict

이메일
→ 필수값
→ 이메일 형식
→ 최대 길이

비밀번호
→ 필수값
→ 최소 길이
→ 최대 길이

이름
→ 필수값
→ 최대 길이

전화번호
→ 최대 길이
```

Validation 실패 시:

```java
verifyNoInteractions(userService);
```

를 사용하여 Service까지 요청이 전달되지 않는지도 확인합니다.

반복되는 MockMvc 요청 및 Validation 검증 로직은 Helper 메서드로 분리했습니다.

```text
performSignUp()
→ 회원가입 HTTP 요청 공통 처리

expectValidationError()
→ Validation 실패 응답 공통 검증
```

---

### Service Test

JUnit 5와 Mockito를 활용하여 비즈니스 로직을 단위 테스트합니다.

#### Meal Service

- 전체 조회
- 단건 조회
- 등록
- 수정
- 삭제
- 존재하지 않는 Meal 예외 처리
- Repository 호출 검증
- Dirty Checking을 이용한 수정 흐름

#### User Service

- 정상 회원가입
- 이메일 중복 확인
- `DuplicateEmailException`
- `PasswordEncoder.encode()` 호출
- 암호화된 비밀번호 저장
- 중복 이메일일 경우 저장 중단

`ArgumentCaptor`를 이용하여 Repository에 실제 전달된 `User` 객체를 확인합니다.

```java
ArgumentCaptor<User> userCaptor =
        ArgumentCaptor.forClass(User.class);

verify(userRepository)
        .save(userCaptor.capture());

User savedUser = userCaptor.getValue();
```

이를 통해 단순히 `save()` 호출 여부뿐만 아니라 저장 대상의 비밀번호가 실제로 인코딩된 값인지 검증합니다.

---

### JPA / Repository Test

H2 Database를 활용하여 실제 JPA 동작을 검증합니다.

- Entity 저장
- DB 재조회
- `flush()` / `clear()`
- Persistence Context
- Dirty Checking
- Database Constraint

---

## 🧪 Test Strategy

테스트는 계층의 책임에 따라 분리합니다.

```text
Controller Test
→ HTTP 계약
→ JSON
→ Validation
→ Exception Response

Service Test
→ 비즈니스 규칙
→ Repository 호출
→ PasswordEncoder 호출

JPA Test
→ 실제 Entity Mapping
→ Persistence Context
→ Database Constraint
```

하나의 테스트에서는 가능하면 하나의 조건만 실패하도록 테스트 데이터를 구성합니다.

예를 들어 이메일 최대 길이 Validation을 테스트할 경우:

```text
@Email
→ 통과

@Size
→ 실패
```

하도록 데이터를 구성하여 어떤 Validation 조건 때문에 테스트가 실패했는지 명확하게 유지합니다.

---

## 📈 Progress

### Completed

#### Project

- [x] Spring Boot 프로젝트 생성
- [x] 프로젝트 패키지 구조 구성
- [x] Health Check API

#### Meal

- [x] Meal Entity
- [x] Meal Repository
- [x] Meal Service
- [x] Meal CRUD REST API
- [x] Request DTO
- [x] Bean Validation
- [x] Meal Not Found 처리

#### Response / Exception

- [x] 공통 API Response
- [x] Global Exception Handling
- [x] Validation Error Response
- [x] DuplicateEmailException
- [x] 이메일 중복 `409 Conflict` 처리

#### JPA / Database

- [x] Spring Data JPA
- [x] Transaction 관리
- [x] JPA Dirty Checking
- [x] Database Constraint
- [x] H2 Database
- [x] Spring Profile 분리
- [x] YAML 설정 전환
- [x] MySQL 연동
- [x] MySQL 전용 애플리케이션 계정
- [x] DB 계정 환경변수 관리

#### User / Sign Up

- [x] User Entity
- [x] 이메일 기반 로그인 ID 설계
- [x] SignUpRequest
- [x] Java record DTO 적용
- [x] SignUpResponse
- [x] UserRepository
- [x] `existsByEmail()` 구현
- [x] 회원가입 Service
- [x] 회원가입 Controller
- [x] 회원가입 API 실제 요청 검증
- [x] 회원가입 Validation
- [x] BCrypt Password Encoding
- [x] MySQL User 저장 확인
- [x] BCrypt Password 저장 확인

#### Spring Security

- [x] Spring Security 의존성 추가
- [x] PasswordEncoder Bean
- [x] BCryptPasswordEncoder 적용
- [x] SecurityFilterChain 설정
- [x] CSRF 비활성화
- [x] `/api/auth/signup` 비인증 접근 허용
- [x] 그 외 요청 인증 필요 설정

#### Test

- [x] Meal Controller Test
- [x] Meal Service Unit Test
- [x] JPA / Repository Test
- [x] Signup Service Test
- [x] Signup Controller Test
- [x] Signup Validation Test
- [x] Duplicate Email Test
- [x] Password Encoding Test

---

## ⏭ Next

### Login

- [ ] LoginRequest
- [ ] LoginResponse
- [ ] 이메일 기반 User 조회
- [ ] 존재하지 않는 User 처리
- [ ] `PasswordEncoder.matches()` 비밀번호 검증
- [ ] 로그인 실패 예외 처리
- [ ] Login Service
- [ ] Login Controller
- [ ] `/api/auth/login` 비인증 접근 허용
- [ ] Login Service Test
- [ ] Login Controller Test

### JWT / Authorization

- [ ] JWT Access Token 발급
- [ ] JWT 검증
- [ ] JWT Authentication Filter
- [ ] 인증 사용자 식별
- [ ] User - Meal 관계
- [ ] 로그인 사용자 기준 Meal 접근 제한
- [ ] Authorization

### Deployment

- [ ] Docker
- [ ] AWS 배포

---

## 🗺 Roadmap

```text
Meal CRUD
    │
    ▼
Validation
    │
    ▼
Exception Handling
    │
    ▼
Test
    │
    ▼
JPA / Transaction
    │
    ▼
Spring Profile
    │
    ▼
MySQL
    │
    ▼
User
    │
    ▼
Sign Up
    │
    ▼
Spring Security
    │
    ▼
Login  ← 다음
    │
    ▼
JWT
    │
    ▼
Authentication / Authorization
    │
    ▼
Docker
    │
    ▼
AWS
```

---

## 📚 Study Notes

구현하면서 학습한 내용과 트러블슈팅은 `docs` 디렉터리에 정리합니다.

주요 학습 주제:

- H2 Database
- Spring Data JPA
- Persistence Context
- Dirty Checking
- Transaction
- Bean Validation
- Exception Handling
- Controller / Service / Repository Test
- Mockito
- ArgumentCaptor
- MockMvc
- Spring Profile
- YAML
- MySQL
- Java record
- Spring Security
- SecurityFilterChain
- BCrypt
- 회원가입
- 테스트 계층 분리