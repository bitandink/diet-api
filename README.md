# 🍱 Diet API

Spring Boot 기반 식단 관리 REST API입니다.

식단의 영양 정보를 등록·조회·수정·삭제하는 CRUD 기능을 시작으로  
Validation, 예외 처리, 테스트, JPA/Transaction, MySQL 연동, 회원가입 및 인증/인가 기능을 단계적으로 학습하고 적용하는 프로젝트입니다.

현재 Meal CRUD와 MySQL 연동을 완료했으며, 이메일 기반 회원가입과 Spring Security 인증 기능을 구현하고 있습니다.

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
  - Test 환경

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

### Service

- 비즈니스 로직 처리
- Transaction 관리
- Entity / DTO 변환
- 비밀번호 해시 처리

### Repository

- Spring Data JPA를 통한 데이터 접근
- Query Method를 통한 조회 및 존재 여부 확인

### Entity

- Database Table Mapping
- 도메인 상태 관리

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
| POST | `/api/auth/signup` | 회원가입 | 구현 중 |
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
- `password`
  - 평문 저장 금지
  - BCrypt 해시값 저장
- `name`
  - 사용자 이름
- `phone`
  - 선택 입력 연락처

---

## 📝 Sign Up

회원가입의 기본 데이터 흐름은 다음과 같습니다.

```text
POST /api/auth/signup
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
        ├── BCrypt Password Encoding
        ├── User Entity 생성
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

로그인 구현 시에는 저장된 Hash를 복호화하지 않고 `matches()`를 사용하여 검증할 예정입니다.

```java
passwordEncoder.matches(
        rawPassword,
        encodedPassword
);
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

회원가입 DTO부터 Java `record`도 사용합니다.

```java
public record SignUpRequest(
        String email,
        String password,
        String name,
        String phone
) {
}
```

`record`는 데이터 전달이 주목적인 DTO에서 반복적인 생성자, Getter 등의 코드를 줄이고 변경 가능성을 제한하기 위해 사용합니다.

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
  "path": "/api/meals",
  "errors": {
    "mealName": "식단 이름은 필수 입력 항목입니다.",
    "calories": "칼로리는 0 이상이어야 합니다."
  }
}
```

---

## 🚨 Exception Handling

`@RestControllerAdvice`를 사용하여 API 예외를 공통으로 처리합니다.

### MealNotFoundException

존재하지 않는 식단을 조회, 수정, 삭제할 경우 발생합니다.

```text
HTTP 404 Not Found
```

### MethodArgumentNotValidException

Request Validation이 실패할 경우 처리합니다.

```text
HTTP 400 Bad Request
```

### Duplicate Email

회원가입 시 이메일 중복 여부를 확인합니다.

```java
boolean existsByEmail(String email);
```

현재 중복 이메일에 대한 전용 예외는 구현 예정입니다.

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
HTTP Error Response
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

Test 환경에서는 외부 MySQL에 의존하지 않도록 H2 In-Memory Database를 사용합니다.

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

자동 테스트 환경에서 사용할 H2 Database 설정을 관리합니다.

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

조회:

```java
@Transactional(readOnly = true)
```

등록 / 수정 / 삭제:

```java
@Transactional
```

Meal 수정에서는 JPA Dirty Checking을 활용합니다.

회원가입 역시 Service의 Transaction 안에서 User를 저장합니다.

---

## 🧪 Test

계층별 책임에 따라 테스트를 분리합니다.

### Controller Test

`MockMvc`를 활용하여 다음을 검증합니다.

- HTTP Status
- Response JSON
- Request Validation
- Exception Handling
- Controller → Service 호출

### Service Test

JUnit과 Mockito를 활용하여 비즈니스 로직을 단위 테스트합니다.

현재 Meal Service를 기준으로 다음을 검증합니다.

- 전체 조회
- 단건 조회
- 등록
- 수정
- 삭제
- 존재하지 않는 Meal 예외 처리
- Repository 호출
- Dirty Checking을 이용한 수정 흐름

회원가입 Service Test는 추가 예정입니다.

### JPA / Repository Test

H2 Database를 활용하여 실제 JPA 동작을 검증합니다.

- Entity 저장
- DB 재조회
- `flush()` / `clear()`
- Persistence Context
- Dirty Checking
- Database Constraint

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

#### Test

- [x] Controller Test
- [x] Service Unit Test
- [x] JPA / Repository Test

#### User / Sign Up

- [x] User Entity
- [x] 이메일 기반 로그인 ID 설계
- [x] SignUpRequest
- [x] Java record DTO 적용
- [x] UserRepository
- [x] `existsByEmail()` 구현
- [x] Spring Security 의존성 추가
- [x] PasswordEncoder Bean
- [x] BCrypt Password Encoding
- [x] UserService 회원가입 기본 로직
- [x] SignUpResponse
- [x] 회원가입 Controller 기본 구현

### In Progress

- [ ] 회원가입 API 실제 요청 검증
- [ ] SecurityFilterChain 설정
- [ ] `/api/auth/signup` 비인증 접근 허용
- [ ] 중복 이메일 전용 예외
- [ ] 회원가입 Service Test
- [ ] 회원가입 Controller Test

### Planned

- [ ] 로그인 API
- [ ] JWT Access Token 발급
- [ ] JWT 검증
- [ ] JWT Authentication Filter
- [ ] 인증 / 인가
- [ ] User - Meal 관계
- [ ] 로그인 사용자 기준 Meal 접근 제한
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
Spring Security  ← 현재
    │
    ▼
Login
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
- Spring Profile
- YAML
- MySQL
- Java record
- Spring Security
- BCrypt
- 회원가입