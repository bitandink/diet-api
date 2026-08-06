# Day02 - REST API & CRUD

## 목표

Spring Boot를 이용하여 REST API를 구현하고,
클라이언트의 요청이 데이터베이스까지 전달되는 전체 흐름을 이해한다.

---

# 오늘 구현한 기능

## Create

```http
POST /api/meals
```

새로운 식단 등록

---

## Read (전체 조회)

```http
GET /api/meals
```

전체 식단 조회

---

## Read (단건 조회)

```http
GET /api/meals/{id}
```

ID로 특정 식단 조회

---

## Update

```http
PUT /api/meals/{id}
```

기존 식단 수정

---

## Delete

```http
DELETE /api/meals/{id}
```

식단 삭제

---

# CRUD 흐름
```
Client

↓

Controller

↓

Service

↓

Repository

↓

Database

↓

Repository

↓

Service

↓

Controller

↓

Client
```

---

# 데이터 흐름
```
클라이언트는 JSON을 보낸다.

↓

Spring이 JSON을 DTO(Request) 객체로 변환한다.

↓

Controller는 DTO를 Service로 전달한다.

↓

Service는 DTO를 Entity로 변환한다.

↓

Repository가 Entity를 이용하여 Database와 통신한다.

↓

Database가 결과를 반환한다.

↓

Repository → Service → Controller 순서로 전달된다.

↓

Controller가 Entity(Response)를 JSON으로 변환하여 클라이언트에게 전달한다.
```

---

# 각 계층의 역할

## Controller

- 클라이언트 요청을 받는다.
- URL과 HTTP Method를 연결한다.
- Service에게 작업을 요청한다.
- 결과를 클라이언트에게 반환한다.

---

## Service

- 비즈니스 로직을 수행한다.
- Repository와 통신한다.
- Entity를 생성하거나 수정한다.
- 예외 상황을 처리한다.

---

## Repository

- Database와 직접 통신한다.
- CRUD를 수행한다.
- SQL을 직접 작성하지 않아도 된다(JPA).

---

## Entity

Database 테이블과 매핑되는 객체

예)

Meal Table

↓

Meal Entity

---

## DTO

클라이언트와 데이터를 주고받기 위한 객체
```
Request DTO

↓

Controller

↓

Service

↓

Entity
```

---

# 왜 DTO를 사용하는가?

클라이언트와 Database를 직접 연결하지 않기 위해

클라이언트의 요청 형식과
Database 구조를 분리하기 위해

역할을 명확하게 나누기 위해

---

# 왜 Entity를 사용하는가?

Entity는 Database 테이블을 Java 객체로 표현한 것이다.

Database의 데이터를 직접 수정하지 않고

Java 객체(Entity)를 통해 작업한 후

Repository가 Database에 반영한다.

---

# 예외 처리(Exception)

조회하려는 데이터가 없을 경우 
기존 500 Internal Server Error → 개선 404 Not Found


```
`메시지

{
"message": "해당 식단을 찾을 수 없습니다. id=999"
}
```

---

# Exception 흐름
```
Client

↓

Controller

↓

Service

↓

Repository

↓

Database

↓

데이터 없음

↓

MealNotFoundException 발생

↓

GlobalExceptionHandler

↓

404 Not Found 반환
```
---

# 오늘 작업 내용 정리

## URL

URL은 서버 안의 특정 자원을 요청하는 주소이다.

ex)
```
GET /api/meals → 식단 목록 요청 
GET /api/meals/1 → 1번 식단 요청
```
---

## PathVariable

URL에 포함된 값을 받아오는 방법

ex)
```
GET /api/meals/1 → id = 1
```

---

## RequestBody

JSON 데이터를 DTO 객체로 변환한다.

---

## ResponseEntity

HTTP 상태 코드와 응답 데이터를 함께 반환하기 위한 객체
```
200 OK

404 Not Found

204 No Content
```

---

# Postman

REST API 테스트 도구

사용한 기능

- POST
- GET
- PUT
- DELETE

JSON 요청 및 응답 확인

---

# SQL 출력
```
application.properties

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.format_sql=true

logging.level.org.hibernate.orm.jdbc.bind=TRACE
```
실행되는 SQL과 바인딩되는 파라미터를 확인할 수 있다.


---

# H2 Database

현재는 메모리 DB를 사용한다.

서버를 종료하면 모든 데이터가 사라진다.

따라서 테스트 데이터는 서버 실행 후 다시 등록해야 한다.

---

# 오늘 가장 중요하게 이해한 것

Spring Boot는 **"코드를 많이 작성하는 기술"** 이 아니라 
**"역할을 분리하여 데이터의 흐름을 설계하는 기술"** 이라는 점을 이해했다.

```
Controller

↓

Service

↓

Repository

↓

Database
```
각 계층은 자신만의 역할만 수행하며 역할이 명확하게 분리될수록 유지보수가 쉬워진다.

---

# 핵심 키워드

## Spring Boot

- REST API
- CRUD
- Client
- Server
- HTTP
- JSON
- URL
- Path Variable
- Request Body
- Response Entity

---

## HTTP Method

- GET
- POST
- PUT
- DELETE

---

## Spring MVC

- Controller
- Service
- Repository
- Entity
- DTO
- Request DTO
- Response DTO (개념)
- Exception
- Global Exception Handler

---

## JPA

- JpaRepository
- save()
- findAll()
- findById()
- delete()

---

## Java

- Object
- Getter
- Setter
- Constructor
- Optional
- orElseThrow()
- RuntimeException
- extends
- return
- void

---

## Database

- H2 Database
- Entity Mapping
- Primary Key (id)

---

## API Test

- Postman
- Request
- Response
- Status Code

---

## HTTP Status

- 200 OK
- 201 Created
- 204 No Content
- 404 Not Found
- 500 Internal Server Error

---

## 예외 처리

- MealNotFoundException
- GlobalExceptionHandler
- ResponseEntity
- @ExceptionHandler
- @RestControllerAdvice

---

## Annotation

- @RestController
- @RequestMapping
- @GetMapping
- @PostMapping
- @PutMapping
- @DeleteMapping
- @PathVariable
- @RequestBody
- @Service
- @Repository
- @Entity

---

# 오늘 가장 중요하게 이해한 개념

✔ Controller는 요청을 받는다.

✔ Service는 비즈니스 로직을 수행한다.

✔ Repository는 Database와 통신한다.

✔ Entity는 Database를 Java 객체로 표현한 것이다.

✔ DTO는 클라이언트와 데이터를 주고받기 위한 객체이다.

✔ 데이터는 계층을 지나면서 목적에 맞는 형태로 계속 변환된다.

✔ Exception은 개발자용 오류를 클라이언트가 이해할 수 있는 HTTP 응답으로 변환한다.

✔ CRUD는 Create, Read, Update, Delete를 의미한다.

✔ Spring Boot의 핵심은 코드를 많이 작성하는 것이 아니라 데이터의 흐름과 역할을 설계하는 것이다.

---

# 다음 목표

- Validation
- Response DTO
- JPA Dirty Checking
- MySQL 연동
- Swagger(OpenAPI)
- Spring Security + JWT