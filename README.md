# 🍱 Diet API

Spring Boot 기반 식단 관리 REST API입니다.

식단의 영양 정보를 등록하고 조회, 수정, 삭제할 수 있으며  
REST API 설계와 계층별 테스트, 예외 처리, 데이터 검증을 학습하고 적용하는 프로젝트입니다.

---

## 🛠 Tech Stack

### Backend

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Bean Validation

### Database

- H2 Database

### Test

- JUnit 5
- Mockito
- MockMvc
- Spring Data JPA Test

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

각 계층의 책임을 분리하여 구성했습니다.

- **Controller**
    - HTTP 요청/응답 처리
    - Request Validation
    - Service 호출

- **Service**
    - 비즈니스 로직 처리
    - 트랜잭션 관리
    - Entity / DTO 변환

- **Repository**
    - Spring Data JPA를 통한 데이터 접근

- **Entity**
    - 데이터베이스 테이블 매핑
    - 식단 데이터 상태 변경

---

## 📌 API

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/meals` | 전체 식단 조회 |
| GET | `/api/meals/{id}` | 특정 식단 조회 |
| POST | `/api/meals` | 식단 등록 |
| PUT | `/api/meals/{id}` | 식단 수정 |
| DELETE | `/api/meals/{id}` | 식단 삭제 |

---

## ✅ Validation

식단 등록 및 수정 요청에 Bean Validation을 적용했습니다.

주요 검증 규칙:

- 식단 이름은 필수 입력
- 식단 이름은 최대 50자
- 칼로리 및 영양 정보는 필수 입력
- 칼로리 및 영양 정보는 0 이상

Validation 실패 시 필드별 에러 정보를 반환합니다.

예시:

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

현재 처리하는 주요 예외:

- `MealNotFoundException`
    - 존재하지 않는 식단 조회 / 수정 / 삭제
    - HTTP `404 Not Found`

- `MethodArgumentNotValidException`
    - Request Validation 실패
    - HTTP `400 Bad Request`

---

## 🧪 Test

Controller, Service, JPA 계층의 책임을 분리하여 테스트합니다.

### Controller Test

`@WebMvcTest`와 `MockMvc`를 사용하여 다음 항목을 검증합니다.

- HTTP Status
- Response JSON
- Request Validation
- Exception Handling
- Controller → Service 호출

### Service Test

JUnit과 Mockito를 사용하여 Service 로직을 단위 테스트합니다.

- 전체 / 단건 조회
- 등록
- 수정
- 삭제
- 존재하지 않는 Meal 예외 처리
- Repository 호출 검증

### JPA Test

실제 JPA와 H2 Database를 사용하여 다음 동작을 검증합니다.

- Entity 저장
- DB 재조회
- `flush()` / `clear()`를 통한 영속성 컨텍스트 검증
- Dirty Checking을 통한 수정 반영
- DB 제약조건

---

## 💾 Database Constraints

API Validation뿐만 아니라 DB 레벨에서도 기본적인 데이터 정합성을 보장합니다.

- `mealName`
    - `NOT NULL`
    - 최대 길이 50

- `calories`
    - `NOT NULL`

- `protein`
    - `NOT NULL`

- `carbohydrate`
    - `NOT NULL`

- `fat`
    - `NOT NULL`

---

## 🔄 Transaction

Service 계층에서 트랜잭션 경계를 관리합니다.

- 조회
    - `@Transactional(readOnly = true)`

- 등록 / 수정 / 삭제
    - `@Transactional`

수정 로직에서는 JPA Dirty Checking을 활용하여 Entity 변경 사항을 반영합니다.

---

## 📈 Progress

### Completed

- [x] Spring Boot 프로젝트 생성
- [x] Health Check API
- [x] 프로젝트 패키지 구조 구성
- [x] Meal Entity
- [x] Meal Repository
- [x] Meal Service
- [x] Meal CRUD REST API
- [x] H2 Database 연동
- [x] Request DTO Validation
- [x] Validation Error Response
- [x] Global Exception Handling
- [x] Meal Not Found 처리
- [x] Controller Test
- [x] Service Unit Test
- [x] JPA / Repository Test
- [x] JPA Dirty Checking 검증
- [x] Entity Database Constraints
- [x] Service Transaction 관리

### Planned

- [ ] Profile 설정 분리
- [ ] MySQL 연동
- [ ] Spring Security
- [ ] JWT 인증 / 인가
- [ ] Docker
- [ ] AWS EC2 배포