# Day 03 - Exception Handling, ResponseEntity, ApiResponse, Generic

## 학습 목표

- 예외를 역할별로 분리해서 처리하는 이유 이해
- `GlobalExceptionHandler`의 역할 이해
- `ErrorResponse`를 사용하는 이유 이해
- `ResponseEntity`의 역할 이해
- 공통 응답 형식 `ApiResponse<T>` 이해
- Generic(`<T>`)의 기본 개념 이해
- Controller의 응답 패턴 읽기

---

# 1. GlobalExceptionHandler

## 내가 이해한 방식

`GlobalExceptionHandler`는 여러 종류의 Exception을 모아두고,

> "지금은 이 Exception이니까 클라이언트에게 이렇게 안내하면 돼."

라고 구분해서 처리하는 **안내데스크**라고 이해했다.

예외마다 상황이 다르기 때문에 클라이언트에게 전달하는 내용도 달라져야 한다.

```text
MealNotFoundException
→ Meal을 찾을 수 없음
→ 404 Not Found

MethodArgumentNotValidException
→ 클라이언트가 보낸 값이 조건에 맞지 않음
→ 400 Bad Request
```

Controller마다 `try-catch`를 작성하지 않고 예외 처리를 담당하는 별도의 클래스로 분리하면 Controller는 요청과 응답이라는 자신의 역할에 집중할 수 있다.

---

# 2. GlobalExceptionHandler 기본 코드

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MealNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMealNotFound(
            MealNotFoundException exception,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
```

---

# 3. ErrorResponse

## 내가 이해한 방식

`ErrorResponse`는 서버에서 발생한 예외를 **클라이언트가 이해하기 좋은 형태로 설명해 주는 번역기**다.

처음에는 다음 세 가지 정보만 있어도 충분하다고 판단했다.

```java
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private String path;
}
```

각 필드의 의미:

```text
status
→ 무슨 종류의 문제인지 빠르게 확인

message
→ 정확히 무엇이 문제인지 설명

path
→ 어떤 요청 경로를 처리하다 문제가 발생했는지 확인
```

예시:

```json
{
  "status": 404,
  "message": "Meal ID 100을 찾을 수 없습니다.",
  "path": "/api/meals/100"
}
```

`path`는 단순히 잘못된 URL일 때만 사용하는 것이 아니다. 정상적인 API 경로에서도 특정 데이터를 찾지 못하거나 요청 처리 중 문제가 발생할 수 있기 때문에 어느 요청에서 문제가 났는지 확인하는 용도로 사용한다.

---

# 4. ErrorResponse 작성 중 실수

처음에는 다음과 같은 형태로 작성하려고 했다.

```java
public static ErrorResponse from(RuntimeException runtimeException) {
    ...
}
```

하지만 `RuntimeException`은 `getMessage()`는 제공하지만 다음 값은 가지고 있지 않다.

```java
getStatus()
getPath()
```

`status`는 예외를 처리하는 쪽에서 결정하고, `path`는 현재 HTTP 요청 정보에서 가져와야 한다.

```text
Exception
→ 무슨 문제가 발생했는지 표현

GlobalExceptionHandler
→ 어떤 HTTP 상태로 안내할지 결정

HttpServletRequest
→ 어느 요청 경로에서 문제가 발생했는지 제공

ErrorResponse
→ 위 정보를 클라이언트가 이해하기 좋은 형태로 담음
```

---

# 5. ResponseEntity

## 내가 이해한 방식

`ResponseEntity`는 **클라이언트에게 보낼 HTTP 응답 전체를 담는 바구니**라고 이해했다.

```text
ResponseEntity

├── HTTP Status
├── Header
└── Body
```

현재 프로젝트에서는 주로 Status와 Body를 사용하고 있다.

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(response);
```

이 코드는 다음 의미다.

```text
HTTP 상태
→ 404 Not Found

Body
→ ErrorResponse
```

---

# 6. ErrorResponse의 status와 ResponseEntity의 status가 둘 다 있는 이유

```java
new ErrorResponse(
        404,
        "...",
        "..."
);
```

그리고

```java
ResponseEntity
        .status(HttpStatus.NOT_FOUND)
```

둘 다 `404`가 들어가지만 역할은 다르다.

```text
ResponseEntity의 404
→ HTTP 프로토콜 자체의 상태 코드

ErrorResponse의 404
→ JSON 데이터 안에서 클라이언트가 확인할 수 있는 값
```

즉:

```text
HTTP 응답 자체
→ 404

응답 Body
→ {
     "status": 404,
     ...
   }
```

---

# 7. 공통 응답 형식이 필요한 이유

기존에는 API마다 결과 형태가 달라질 수 있다.

```json
{
  "id": 1,
  "mealName": "닭가슴살"
}
```

또는

```json
{
  "message": "삭제 성공"
}
```

이렇게 응답 형식이 제각각이면 클라이언트가 API마다 다른 형태를 해석해야 한다.

그래서 응답 형태를 통일한다.

```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "id": 1,
    "mealName": "닭가슴살"
  }
}
```

삭제의 경우:

```json
{
  "success": true,
  "message": "삭제 성공",
  "data": null
}
```

내가 이해한 핵심:

> **조회 성공이든 등록 성공이든 삭제 성공이든 반환 형태를 일치시키면 클라이언트가 헷갈리지 않는다.**

---

# 8. ApiResponse

## 내가 이해한 방식

`ApiResponse`는 **Controller가 반환하는 여러 결과를 일정한 JSON 형태로 번역해 주는 번역기**다.

```text
Request DTO
→ 클라이언트 JSON을 Java가 이해할 형태로 번역

Response DTO
→ Java 데이터를 클라이언트가 이해할 형태로 번역

ErrorResponse
→ Exception을 클라이언트가 이해할 형태로 번역

ApiResponse
→ Controller의 여러 반환 결과를 하나의 표준 JSON 형식으로 번역
```

---

# 9. ApiResponse 기본 구조

처음에는 다음처럼 생각했다.

```java
private String success;
private String message;
private List data;
```

하지만 두 가지 문제가 있었다.

## success

성공 여부는 문자열보다 참/거짓이 더 적절하다.

```java
private boolean success;
```

## data

`data`에는 다양한 타입이 들어갈 수 있다.

```text
MealResponse
List<MealResponse>
UserResponse
null
```

따라서 `List`로 고정하면 안 된다. 이 문제를 해결하기 위해 Generic을 사용한다.

---

# 10. Generic `<T>`

## 내가 이해한 방식

`T`는 **어떠한 형태의 반환값이든 받을 수 있도록 만든 범용 그릇의 빈 자리**라고 이해했다.

```java
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
}
```

여기서 `<T>`는

> "이 클래스 안에서 아직 정해지지 않은 타입 T를 사용할 것이다."

라고 Java에게 알려주는 역할이다.

---

# 11. Generic 사용 예시

단건 조회:

```java
ApiResponse<MealResponse>
```

```text
T = MealResponse
```

목록 조회:

```java
ApiResponse<List<MealResponse>>
```

```text
T = List<MealResponse>
```

삭제:

```java
ApiResponse<Void>
```

실제 반환 데이터가 없기 때문에 `Void`를 사용할 수 있다.

---

# 12. Generic은 이미 사용하고 있었다

Generic을 새로 배운 것처럼 느껴졌지만 기존 코드에서도 이미 계속 사용하고 있었다.

```java
List<MealResponse>
Optional<Meal>
ResponseEntity<ErrorResponse>
JpaRepository<Meal, Long>
```

특히:

```java
JpaRepository<Meal, Long>
```

은 다음처럼 이해할 수 있다.

```text
Meal 데이터를 관리하고
ID 타입은 Long인 Repository
```

---

# 13. Controller의 공통 응답 패턴

`ApiResponse<T>`를 적용하면 Controller에서도 일정한 패턴이 보인다.

```text
1. Service 호출
2. 작업 결과 받기
3. ApiResponse에 담기
4. ResponseEntity로 반환
```

예: 단건 조회

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<MealResponse>> findById(
        @PathVariable Long id
) {

    MealResponse meal = mealService.findById(id);

    ApiResponse<MealResponse> response =
            new ApiResponse<>(
                    true,
                    "Meal 조회 성공",
                    meal
            );

    return ResponseEntity.ok(response);
}
```

반환 타입:

```java
ResponseEntity<ApiResponse<MealResponse>>
```

내가 이해한 방식:

```text
ResponseEntity
→ HTTP 응답 전체 바구니

ApiResponse
→ 표준 응답 양식

MealResponse
→ 실제 Meal 데이터를 클라이언트용으로 번역한 결과
```

즉:

```text
HTTP 응답 바구니
└── 표준 응답 형식
    └── 실제 Meal 데이터
```

---

# 14. 전체 조회

```java
@GetMapping
public ResponseEntity<ApiResponse<List<MealResponse>>> findAll() {

    List<MealResponse> meals = mealService.findAll();

    ApiResponse<List<MealResponse>> response =
            new ApiResponse<>(
                    true,
                    "전체 Meal 조회 성공",
                    meals
            );

    return ResponseEntity.ok(response);
}
```

여기서는:

```text
T = List<MealResponse>
```

---

# 15. 등록

```java
@PostMapping
public ResponseEntity<ApiResponse<MealResponse>> saveMeal(
        @Valid @RequestBody MealRequest mealRequest
) {

    MealResponse meal = mealService.saveMeal(mealRequest);

    ApiResponse<MealResponse> response =
            new ApiResponse<>(
                    true,
                    "Meal 등록 성공",
                    meal
            );

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}
```

등록 성공은 새로운 데이터가 생성되었기 때문에 `201 Created`를 사용할 수 있다.

---

# 16. 수정

```java
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<MealResponse>> updateMeal(
        @PathVariable Long id,
        @Valid @RequestBody MealRequest mealRequest
) {

    MealResponse meal = mealService.updateMeal(id, mealRequest);

    ApiResponse<MealResponse> response =
            new ApiResponse<>(
                    true,
                    "Meal 수정 성공",
                    meal
            );

    return ResponseEntity.ok(response);
}
```

---

# 17. 삭제

```java
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteMeal(
        @PathVariable Long id
) {

    mealService.deleteMeal(id);

    ApiResponse<Void> response =
            new ApiResponse<>(
                    true,
                    "Meal 삭제 성공",
                    null
            );

    return ResponseEntity.ok(response);
}
```

삭제 후 반환할 실제 데이터가 없기 때문에 `ApiResponse<Void>`를 사용하고 `data`에는 `null`을 넣는다.

---

# 18. 코드에서 패턴을 보기 시작했다

처음에는 복잡한 코드를 보면 한 줄씩 따로 읽었다.

하지만 개념을 먼저 이해하고 전체 코드를 보니 반복되는 구조가 보이기 시작했다.

```text
Controller

Service 호출
↓
결과 받기
↓
ApiResponse 생성
↓
ResponseEntity 반환
```

처음부터 아래 코드를 봤다면 복잡하게 느껴졌을 수 있다.

```java
ResponseEntity<ApiResponse<List<MealResponse>>>
```

하지만 각각의 역할을 이해한 뒤에는:

```text
ResponseEntity
→ HTTP 응답 바구니

ApiResponse
→ 표준 응답 번역기

List<MealResponse>
→ 실제 데이터
```

처럼 읽을 수 있게 되었다.

---

# 19. 파일이 많아져도 유지보수가 쉬운 이유

Spring 구조를 적용하면서 파일 수는 늘어났다.

하지만 파일 하나당 담당하는 역할은 오히려 줄어들었다.

```text
Controller
→ 요청 / 응답

Service
→ 실제 비즈니스 로직

Repository
→ DB 접근

DTO
→ 데이터 번역

Exception
→ 무슨 문제가 발생했는지 표현

GlobalExceptionHandler
→ 어떤 안내를 할지 결정

ErrorResponse
→ 에러 안내 내용을 클라이언트용으로 번역

ApiResponse
→ 성공 응답 형식을 통일
```

그래서 문제가 발생했을 때 프로젝트 전체를 뒤지는 대신 문제가 생긴 역할의 파일을 찾아가면 된다.

내가 이해한 기준:

> **파일을 많이 나누는 것이 중요한 게 아니라, 따로 맡겨야 할 책임이 있는지를 기준으로 나눈다.**

---

# 20. 오늘의 핵심 연결

```text
Client
    ↓
Request DTO
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
DB
```

정상 처리 결과:

```text
Service 결과
    ↓
MealResponse
    ↓
ApiResponse<T>
    ↓
ResponseEntity
    ↓
Client
```

예외 발생:

```text
Exception
    ↓
GlobalExceptionHandler
    ↓
ErrorResponse
    ↓
ResponseEntity
    ↓
Client
```

---

# 21. 나만의 Spring 비유 사전

```text
Entity
→ DB 데이터를 담는 바구니

Repository
→ 창고 / 창고지기

Service
→ 실제 작업을 수행하는 공장장

Controller
→ 클라이언트 요청을 접수하고 결과를 전달하는 곳

DTO
→ 번역기

Interface
→ 역할과 규칙이 적힌 계약서

Bean
→ Spring이 미리 만들어 관리하는 콩 씨앗

Spring Container
→ 콩밭

DI
→ 필요한 객체를 Spring이 넣어주는 것

Exception
→ 어떤 문제가 발생했는지 표현하는 사고 보고서

GlobalExceptionHandler
→ 여러 사고를 구분해서 안내하는 안내데스크

ErrorResponse
→ 에러 안내문

ResponseEntity
→ HTTP 응답 전체를 담는 바구니 / 우체국 봉투

ApiResponse
→ 응답 형태를 통일하는 표준 안내문

Generic <T>
→ 어떤 형태의 데이터든 받을 수 있도록 비워둔 자리
```

---

# 22. 오늘의 Insight

> **코드를 먼저 외우는 것보다 역할과 이유를 먼저 이해하면 코드에서 패턴이 보이기 시작한다.**

또한:

> **좋은 설계는 파일 수를 무조건 줄이는 것이 아니라, 각 파일이 자신이 맡은 책임을 명확하게 갖도록 만드는 것이다.**

그리고 API 응답에서는:

> **조회, 등록, 수정, 삭제처럼 결과가 달라도 클라이언트에게 전달되는 기본 형식을 통일하면 사용하는 쪽에서 훨씬 이해하기 쉽다.**

---

# 오늘의 주요 키워드

- `@RestControllerAdvice`
- `@ExceptionHandler`
- `GlobalExceptionHandler`
- `ErrorResponse`
- `HttpServletRequest`
- `HttpStatus`
- `ResponseEntity`
- HTTP Status Code
- `ApiResponse<T>`
- Generic
- `<T>`
- `Void`
- 공통 API 응답
- 책임 분리
- 응답 형식 통일
- Controller 응답 패턴
- 유지보수성
- 리팩토링
