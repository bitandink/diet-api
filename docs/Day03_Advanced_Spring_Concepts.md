# Day 03 - Spring 객체지향 설계와 Exception 이해

## 학습 목표

- Spring의 객체지향 설계 철학 이해
- Interface와 DI(의존성 주입)의 관계 이해
- RuntimeException과 Custom Exception 이해
- Exception도 하나의 객체라는 개념 이해

---

# 1. Spring은 왜 계층을 나눌까?

처음에는 Controller, Service, Repository를 단순히 역할별로 나눈 구조라고 생각했다.

하지만 Spring은 **객체지향의 단일 책임 원칙(SRP)** 을 지키기 위해 계층을 분리한다.

```
Client
   │
   ▼
Controller
(요청/응답)

   │
   ▼
Service
(비즈니스 로직)

   │
   ▼
Repository
(DB 접근)

   │
   ▼
Database
```

각 계층은 자신의 책임만 수행하며 다른 계층의 역할을 대신하지 않는다.

---

# 2. 객체지향(Object-Oriented Programming)

객체지향은

> 클래스를 많이 만드는 것이 아니라

각 객체가

- 자신의 책임을 가지고
- 서로 협력하도록 설계하는 것이다.

Spring은 이러한 객체지향 구조를 쉽게 만들도록 도와주는 프레임워크이다.

---

# 3. Interface를 사용하는 이유

Service는 실제 Repository가 어떻게 구현되었는지 알 필요가 없다.

Service는

```java
MealRepository
```

라는 **역할(Role)** 만 알고 있으면 된다.

실제 구현은

- JpaRepository
- MemoryRepository
- MySQL Repository
- Mongo Repository

무엇이든 될 수 있다.

Service는

> "저장한다"

라는 규칙만 사용한다.

---

# 4. Interface는 계약서(규칙)

자동차 공장 비유

```
공장장(Service)

      │

      ▼

창고 계약서(Interface)

      │

      ▼

서울 창고
부산 창고
AWS 창고
```

공장장은

어느 창고인지 알 필요가 없다.

다만

- 저장
- 조회
- 삭제

기능만 제공하면 된다.

---

# 5. DI(Dependency Injection)

Spring은 프로젝트가 실행될 때 Bean을 생성한다.

Service에서는

```java
private final MealRepository repository;
```

만 선언하면

Spring Container가

알맞은 Repository Bean을 찾아 자동으로 주입한다.

개발자는 객체 생성보다

비즈니스 로직 작성에 집중할 수 있다.

---

# 6. Bean에 대한 이해

Bean은

Spring이 생성하고 관리하는 객체이다.

학습 비유

```
Spring(봄)

↓

Bean(콩)

↓

Container(콩밭)

↓

DI(필요한 곳에 심기)
```

공식 개념은 아니지만

Bean의 역할을 이해하기 쉬운 비유이다.

---

# 7. RuntimeException을 사용하는 이유

Java의 Exception은 크게 두 종류이다.

## Checked Exception

```java
Exception
```

반드시

- try-catch

또는

- throws

를 사용해야 한다.

---

## Unchecked Exception

```java
RuntimeException
```

필요한 경우에만 처리하면 된다.

Spring은 RuntimeException을 많이 사용하며

GlobalExceptionHandler에서 공통 처리하는 구조를 사용한다.

---

# 8. 사용자 정의(Custom) Exception

예외도 하나의 객체이다.

예시

```java
MealNotFoundException
```

역할

```
Meal 조회 실패
```

다른 Exception도 마찬가지이다.

| Exception | 책임 |
|-----------|------|
| NullPointerException | Null 참조 |
| NumberFormatException | 문자열 → 숫자 변환 실패 |
| ArithmeticException | 수학 계산 오류 |
| MealNotFoundException | Meal 조회 실패 |

각 Exception은 하나의 책임만 가진다.

---

# 9. Exception도 객체이다.

Java는 객체지향 언어이다.

예외도 객체이며 상속 구조를 가진다.

```
Throwable
    │
    ▼
Exception
    │
    ▼
RuntimeException
    │
    ▼
MealNotFoundException
```

---

# 10. super()의 의미

```java
public MealNotFoundException(Long id) {
    super("Meal ID " + id + "를 찾을 수 없습니다.");
}
```

`super()`는

부모 클래스인

```java
RuntimeException
```

의 생성자를 호출한다.

메시지는 부모(RuntimeException)가 관리하도록 위임하는 것이다.

---

# 11. Exception도 객체지향 설계이다.

예외 역시 책임을 가진 객체이다.

```
Repository
↓
조회만 책임

Service
↓
조회 결과를 해석

Exception
↓
무슨 문제가 발생했는지 표현

GlobalExceptionHandler
↓
클라이언트에게 어떻게 전달할지 결정
```

예외 처리도 객체지향의 책임 분리 원칙을 따른다.

---

# 오늘의 핵심 깨달음

Spring은

기능이 많아서 좋은 프레임워크가 아니라

**객체지향 설계를 쉽게 구현하도록 만든 프레임워크**이다.

모든 구조에는 이유가 존재한다.

- Controller
- Service
- Repository
- DTO
- Entity
- Bean
- Interface
- Exception

모두 하나의 책임만 가지도록 설계되어 있다.

---

# 오늘의 키워드

- SRP (Single Responsibility Principle)
- 객체지향(Object-Oriented Programming)
- Bean
- DI (Dependency Injection)
- IoC
- Interface
- RuntimeException
- Checked Exception
- Unchecked Exception
- Custom Exception
- super()
- GlobalExceptionHandler
- Spring Container
- Polymorphism(다형성)
- Responsibility(책임)
- Maintainability(유지보수성)

---

# 오늘의 Insight

> **Spring은 코드를 실행하기 위한 프레임워크가 아니라 사람이 오랫동안 유지보수할 수 있는 객체지향 구조를 만들기 위한 프레임워크이다.**

그리고 앞으로 새로운 기술을 배울 때 항상 기억할 것.

> **"어떻게 사용하는가?"보다 "왜 이 기술이 만들어졌는가?"를 먼저 생각하자.**

기술의 사용법은 검색으로 찾을 수 있지만,

기술이 만들어진 이유를 이해하면 오래 기억하고 응용할 수 있다.