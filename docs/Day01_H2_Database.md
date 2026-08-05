# Day 01 - H2 Database Configuration

## 📌 목표

Spring Boot 프로젝트를 생성하고 H2 Database를 연동하여
JPA가 Entity를 기반으로 데이터베이스 테이블을 자동 생성하는 과정을 이해한다.

---

# 프로젝트 생성

### 개발 환경

- Java 17
- Spring Boot 4
- Gradle
- IntelliJ IDEA
- H2 Database
- Spring Data JPA

---

# 프로젝트 구조

프로젝트는 Spring Boot의 계층형 아키텍처를 기반으로 구성하였다.

```
src
└── main
    ├── controller
    ├── service
    ├── repository
    ├── entity
    ├── dto
    ├── config
    ├── exception
    ├── common
    └── util
```

각 계층의 역할은 다음과 같다.

| 계층 | 역할 |
|------|------|
| Controller | 클라이언트의 HTTP 요청을 처리 |
| Service | 비즈니스 로직 수행 |
| Repository | 데이터베이스 접근 |
| Entity | 데이터베이스 테이블과 매핑되는 객체 |
| DTO | 요청(Request)과 응답(Response) 데이터 전달 |
| Config | Spring 설정 |
| Exception | 예외 처리 |
| Common / Util | 공통 기능 |

---

# Health Check API

프로젝트가 정상적으로 실행되는지 확인하기 위해
간단한 Health Check API를 구현하였다.

### Endpoint

```
GET /health
```

### Response

```
OK
```

---

# H2 Database 설정

application.properties에 H2 Database 연결 정보를 추가하였다.

```properties
spring.datasource.url=jdbc:h2:mem:dietdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### 설정 설명

- H2 In-Memory Database 사용
- 데이터베이스 이름 : dietdb
- 기본 사용자 : sa
- 비밀번호 없음

---

# JPA 설정

```properties
spring.jpa.hibernate.ddl-auto=update
```

Spring Boot 실행 시 Entity를 기반으로
데이터베이스 테이블을 자동 생성하도록 설정하였다.

### ddl-auto 옵션

| 옵션 | 설명 |
|------|------|
| create | 실행 시 기존 테이블 삭제 후 새로 생성 |
| create-drop | 종료 시 테이블 삭제 |
| update | 기존 테이블 유지 후 변경 사항만 적용 |

현재 프로젝트에서는 **update** 옵션을 사용하였다.

---

# SQL 출력 설정

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 목적

- 실행되는 SQL 확인
- SQL을 보기 좋은 형태로 출력

---

# H2 Console 활성화

```properties
spring.h2.console.enabled=true
```

브라우저에서

```
http://localhost:8080/h2-console
```

접속하여 데이터베이스를 확인할 수 있도록 설정하였다.

---

# 오늘 학습한 내용

Spring Boot에서 요청이 처리되는 기본 흐름을 이해하였다.

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
```

또한 JPA의 기본 동작 원리를 학습하였다.

```
@Entity

↓

Hibernate

↓

Table 생성

↓

Repository를 통해 CRUD 수행
```

---

# 느낀 점

처음에는 Entity가 단순한 Java 객체라고 생각했지만,
JPA와 Hibernate가 실행되면서 실제 데이터베이스 테이블로 변환된다는 점을 이해할 수 있었다.

또한 application.properties의 설정이 단순한 옵션이 아니라
Spring Boot의 데이터베이스 연결과 JPA 동작 방식을 제어하는 중요한 설정 파일이라는 것을 알게 되었다.

---

# 오늘 작업 내용 정리
Spring Boot의 계층 구조를 식당에 비유하면 아래와 같이 이해할 수 있다.

- **Entity**는 데이터베이스의 데이터를 Java 객체로 표현한 것으로, 데이터를 담는 바구니와 같은 역할을 한다.
- **Repository**는 Entity를 이용해 데이터베이스와 통신하는 계층이다. 냉장고에서 재료를 꺼내거나 보관하는 역할에 비유할 수 있다.
- **Service**는 Repository로부터 가져온 데이터를 이용해 비즈니스 로직을 수행하는 계층이다. 단순히 데이터를 전달하는 것이 아니라, 필요한 계산이나 검증, 조건 판단 등을 수행한다.
- **Controller**는 클라이언트의 요청을 받아 Service에 전달하고, 처리 결과를 응답하는 창구 역할을 한다.

요청은 다음과 같은 흐름으로 처리된다.

```
Client → Controller → Service → Repository → Database
```

응답은 반대의 순서로 전달된다.
```
Database → Repository → Service → Controller → Client
```

---

## 오늘의 키워드

- Spring Boot
- Layered Architecture
- Controller
- Service
- Repository
- Entity
- Dependency Injection(DI)
- H2 Database
- JPA

---

# 다음 목표

- Meal Create API 구현
- Meal 조회 API 구현
- CRUD(Create / Read) 기능 완성
- H2 Console에서 데이터 저장 확인