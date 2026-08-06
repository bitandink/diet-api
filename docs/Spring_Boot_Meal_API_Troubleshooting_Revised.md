# Spring Boot Meal API Troubleshooting

## 문서 개요

Spring Boot와 JPA를 이용해 Meal REST API를 구현하면서 발생한 오류와 구조 개선 과정을 정리한다.

이번 작업에서는 다음 기능과 개념을 다뤘다.

- Meal CRUD
- 단건 조회 예외 처리
- Validation
- Validation 예외 응답 처리
- Lombok 적용
- Entity 캡슐화
- Response DTO 적용
- Builder 패턴
- Entity → Response DTO 변환

이 문서의 목적은 단순히 오류 해결 코드를 보관하는 데 있지 않다.

각 오류가 어느 계층에서 발생했고, 왜 문제가 되었으며, 어떤 방식으로 수정했고, 그 과정에서 어떤 설계 원칙을 이해했는지 함께 기록한다.

---

# 1. 컴파일 및 타입 오류

## 1.1 Repository `save()`에 DTO를 전달한 오류

### 문제 상황

새로운 Meal을 저장하기 위해 Service에서 DTO를 Entity로 변환했지만, 실제 `save()` 호출에는 DTO를 전달했다.

```java
public Meal saveMeal(MealRequest mealRequest) {
        Meal meal = new Meal(
        mealRequest.getMealName(),
        mealRequest.getCalories(),
        mealRequest.getProtein(),
        mealRequest.getCarbohydrate(),
        mealRequest.getFat()
        );

        return mealRepository.save(mealRequest);
        }
```

### 오류 원인

`MealRepository`는 다음과 같이 선언되어 있다.

```java
public interface MealRepository extends JpaRepository<Meal, Long> {
}
```

따라서 `save()`가 받을 수 있는 객체는 `MealRequest`가 아니라 `Meal` Entity이다.

```text
MealRequest
→ 클라이언트 요청 데이터를 담는 DTO

Meal
→ JPA가 데이터베이스에 저장할 수 있는 Entity
```

DTO를 Entity로 변환해 놓고도 Repository에 DTO를 전달한 것이 원인이었다.

### 해결

```java
public Meal saveMeal(MealRequest mealRequest) {
        Meal meal = new Meal(
        mealRequest.getMealName(),
        mealRequest.getCalories(),
        mealRequest.getProtein(),
        mealRequest.getCarbohydrate(),
        mealRequest.getFat()
        );

        return mealRepository.save(meal);
        }
```

### 결과

`MealRequest`가 `Meal` Entity로 변환된 뒤 정상적으로 저장되었다.

### 배운 점

Repository는 데이터베이스와 통신하는 계층이므로 DTO가 아니라 Entity를 전달해야 한다.

```text
Client JSON
→ MealRequest
→ Meal Entity
→ Repository
→ Database
```

---

## 1.2 `orElseThrow()` 호출 위치 오류

### 문제 상황

특정 Meal을 조회하는 과정에서 괄호 위치를 잘못 작성했다.

```java
Meal meal = mealRepository.findById(
        (id).orElseThrow(() -> new MealNotFoundException(id))
        );
```

### 오류 메시지

```text
cannot find symbol
method orElseThrow(...)
```

### 오류 원인

`orElseThrow()`는 `Long id`가 가진 메서드가 아니다.

`mealRepository.findById(id)`가 반환하는 `Optional<Meal>`에 호출해야 한다.

```text
id
→ Long

findById(id)
→ Optional<Meal>

orElseThrow()
→ Optional이 제공하는 메서드
```

괄호 위치 때문에 자바가 `id.orElseThrow()`를 호출하는 것으로 해석했다.

### 해결

```java
Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));
```

### 결과

데이터가 존재하면 `Meal`을 반환하고, 존재하지 않으면 `MealNotFoundException`이 발생하도록 정상 동작했다.

### 배운 점

메서드 체이닝을 사용할 때는 각 메서드가 무엇을 반환하는지 확인해야 한다.

```text
findById(id)
→ Optional<Meal>

Optional<Meal>.orElseThrow(...)
→ Meal 또는 Exception
```

---

## 1.3 Entity Setter 누락 오류

### 문제 상황

수정 로직에서 Entity의 Setter를 호출했다.

```java
meal.setMealName(mealRequest.getMealName());
        meal.setCalories(mealRequest.getCalories());
        meal.setProtein(mealRequest.getProtein());
        meal.setCarbohydrate(mealRequest.getCarbohydrate());
        meal.setFat(mealRequest.getFat());
```

### 오류 메시지

```text
cannot find symbol
method setMealName(...)
```

### 오류 원인

IntelliJ의 자동 생성 기능을 사용할 때 Getter만 선택한 상태였다.

`MealRequest`에는 Getter와 Setter가 있었지만, `Meal` Entity에는 Getter만 존재했다.

따라서 다음 메서드들이 실제로 생성되지 않았다.

```text
setMealName()
setCalories()
setProtein()
setCarbohydrate()
setFat()
```

### 초기 해결

CRUD 구현 단계에서는 Entity에 Setter를 추가해 수정 기능을 완성했다.

### 최종 구조 개선

이후 Lombok과 캡슐화를 적용하면서 Entity 전체 Setter를 제거하고, 목적이 드러나는 수정 메서드로 변경했다.

```java
public void update(MealRequest mealRequest) {
        this.mealName = mealRequest.getMealName();
        this.calories = mealRequest.getCalories();
        this.protein = mealRequest.getProtein();
        this.carbohydrate = mealRequest.getCarbohydrate();
        this.fat = mealRequest.getFat();
        }
```

Service에서는 다음과 같이 호출한다.

```java
meal.update(mealRequest);
```

### 배운 점

Getter와 Setter는 특별한 문법이 아니라 실제 메서드이다.

또한 Entity에 전체 Setter를 공개하면 프로젝트 어디에서나 값을 자유롭게 변경할 수 있다.

```java
meal.setCalories(-999);
        meal.setMealName("");
```

Entity의 상태를 보호하려면 전체 Setter보다 의도가 드러나는 메서드를 사용하는 편이 안전하다.

```java
meal.update(mealRequest);
```

---

## 1.4 `delete()` 결과를 반환하려 한 오류

### 문제 상황

삭제 메서드에서 `delete()`의 결과를 반환하려 했다.

```java
public Meal deleteMeal(Long id) {
        Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));

        return mealRepository.delete(meal);
        }
```

### 오류 원인

Spring Data JPA의 `delete()` 메서드는 반환값이 없는 `void` 메서드이다.

삭제 작업은 수행하지만 호출한 곳에 전달할 객체는 반환하지 않는다.

### 해결

Service 반환 타입을 `void`로 변경했다.

```java
public void deleteMeal(Long id) {
        Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));

        mealRepository.delete(meal);
        }
```

Controller에서는 객체 대신 HTTP 상태 코드로 성공을 알렸다.

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);

        return ResponseEntity.noContent().build();
        }
```

### 결과

```http
204 No Content
```

### 배운 점

`void`는 아무 작업도 하지 않는다는 뜻이 아니다.

```text
void
→ 작업은 수행함
→ 호출한 곳에 반환할 값은 없음
```

삭제 성공 여부는 객체 대신 HTTP 상태 코드로 표현할 수 있다.

```text
삭제 성공
→ 204 No Content

삭제 대상 없음
→ 404 Not Found
```

---

## 1.5 `void update()` 결과를 반환하려 한 오류

### 문제 상황

Entity 전체 Setter를 제거하고 다음 수정 메서드를 만들었다.

```java
public void update(MealRequest mealRequest) {
        this.mealName = mealRequest.getMealName();
        this.calories = mealRequest.getCalories();
        this.protein = mealRequest.getProtein();
        this.carbohydrate = mealRequest.getCarbohydrate();
        this.fat = mealRequest.getFat();
        }
```

Service에서는 다음과 같이 작성했다.

```java
return meal.update(mealRequest);
```

### 오류 원인

`update()`는 `Meal`을 반환하는 메서드가 아니라 기존 객체의 상태만 변경하는 `void` 메서드이다.

```java
public void update(...)
```

따라서 다음 코드는 반환값이 없는 메서드의 결과를 반환하라고 요청하는 코드가 된다.

```java
return meal.update(mealRequest);
```

### 해결

상태 변경, 데이터베이스 저장, 응답 변환의 책임을 분리했다.

```java
public MealResponse updateMeal(Long id, MealRequest mealRequest) {
        Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));

        meal.update(mealRequest);

        Meal updatedMeal = mealRepository.save(meal);

        return MealResponse.from(updatedMeal);
        }
```

### 결과

기존 `Meal`의 상태가 변경된 뒤 데이터베이스에 저장되고, 저장된 Entity가 `MealResponse`로 변환되어 반환되었다.

### 배운 점

객체의 상태가 변경되었다는 것과 메서드가 값을 반환한다는 것은 같은 의미가 아니다.

```text
meal.update(request)
→ 기존 객체의 상태 변경
→ 반환값 없음

mealRepository.save(meal)
→ 변경된 Entity 저장
→ 저장된 Entity 반환

MealResponse.from(updatedMeal)
→ Entity를 Response DTO로 변환
```

이 구분은 이후 JPA 변경 감지(Dirty Checking)를 이해할 때도 중요하다.

---

# 2. REST API 연결 오류

## 2.1 수정 API에 `@PostMapping`을 사용한 오류

### 문제 상황

수정 API를 구현하면서 IntelliJ 자동완성에서 `@PostMapping`을 잘못 선택했다.

```java
@PostMapping("/{id}")
public Meal updateMeal(
@PathVariable Long id,
@RequestBody MealRequest mealRequest
        ) {
        return mealService.saveMeal(mealRequest);
        }
```

### 오류 메시지

```text
Request method 'POST' is not supported
```

### 오류 원인

수정 API는 `PUT` 요청을 처리해야 하지만 `@PostMapping`을 사용했다.

또한 Controller가 수정 메서드가 아니라 등록 메서드인 `saveMeal()`을 호출하고 있었다.

### 해결

```java
@PutMapping("/{id}")
public Meal updateMeal(
@PathVariable Long id,
@RequestBody MealRequest mealRequest
        ) {
        return mealService.updateMeal(id, mealRequest);
        }
```

Postman에서도 HTTP Method를 `PUT`으로 설정했다.

```http
PUT /api/meals/1
```

### 결과

특정 ID의 Meal 수정 요청이 정상적으로 Controller와 Service에 연결되었다.

### 배운 점

REST API는 URL만으로 동작이 결정되지 않는다.

```text
HTTP Method + URL
→ 요청의 의미
```

```text
POST /api/meals
→ 새로운 식단 등록

PUT /api/meals/{id}
→ 기존 식단 수정
```

로직이 맞더라도 Mapping 어노테이션이나 호출하는 Service 메서드가 잘못되면 요청은 정상적으로 연결되지 않는다.

---

## 2.2 존재하지 않는 ID 조회 시 서버 오류가 노출된 문제

### 문제 상황

존재하지 않는 ID를 조회했을 때 데이터가 없다는 상황을 적절하게 처리하지 못했다.

```http
GET /api/meals/999
```

### 오류 원인

`findById()`가 반환한 `Optional<Meal>`이 비어 있을 경우를 클라이언트용 응답으로 변환하지 않았다.

존재하지 않는 자원을 요청한 상황은 서버 전체의 고장이 아니라 `404 Not Found`로 표현해야 한다.

### 해결 1: 사용자 정의 예외 생성

```java
public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(Long id) {
        super("해당 식단을 찾을 수 없습니다. id=" + id);
    }
}
```

### 해결 2: Service에서 예외 발생

```java
return mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));
```

### 해결 3: 전역 예외 처리

```java
@ExceptionHandler(MealNotFoundException.class)
public ResponseEntity<Map<String, String>> handleMealNotFound(
        MealNotFoundException exception
        ) {
        Map<String, String> response = Map.of(
        "message", exception.getMessage()
        );

        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(response);
        }
```

### 결과

```http
404 Not Found
```

```json
{
  "message": "해당 식단을 찾을 수 없습니다. id=999"
}
```

### 배운 점

Exception은 프로그램을 중단시키는 장치로만 사용하는 것이 아니다.

서버 내부 상황을 클라이언트가 이해할 수 있는 HTTP 상태 코드와 JSON 응답으로 번역하는 데 사용할 수 있다.

```text
데이터 없음
→ MealNotFoundException
→ GlobalExceptionHandler
→ 404 Not Found
→ JSON Response
```

---

# 3. 저장 및 실행 상태 문제

## 3.1 현재 코드와 콘솔 오류가 일치하지 않은 문제

### 문제 상황

코드를 수정했는데도 IntelliJ 하단에는 수정 전 코드와 관련된 오류가 계속 표시되었다.

현재 코드에는 문제가 없어 보였지만 오류 메시지는 이전 상태를 가리키고 있었다.

### 추정 원인 및 점검 항목

당시 원인은 하나로 확정하지 못했으며 다음 가능성을 점검했다.

- 파일 저장이 반영되지 않음
- 서버가 수정 전 코드로 실행 중
- 이전 빌드 결과가 남아 있음
- Gradle 변경 사항이 반영되지 않음

### 해결 및 점검 순서

```text
1. 파일 저장
2. 실행 중인 서버 완전히 종료
3. 프로젝트 다시 빌드
4. Gradle Reload
5. 서버 재실행
```

### 결과

수정한 코드가 다시 컴파일되고 최신 상태로 서버가 실행되었다.

### 배운 점

오류 메시지와 현재 코드가 일치하지 않을 때는 코드만 확인해서는 안 된다.

```text
코드
빌드 결과
Gradle 상태
실행 중인 서버
```

를 함께 확인해야 한다.

트러블슈팅 문서에서는 확정된 원인과 추정 원인을 구분해서 기록하는 것이 중요하다.

---

# 4. Validation 및 오류 응답

## 4.1 Validation 어노테이션을 붙였는데 음수가 저장된 문제

### 문제 상황

`MealRequest`에 Validation 규칙을 추가했다.

```java
@NotNull
@PositiveOrZero
private Integer calories;
```

하지만 Postman에서 음수를 전송했을 때 그대로 저장되었다.

```json
{
  "mealName": "닭가슴살",
  "calories": -2,
  "protein": 20,
  "carbohydrate": 10,
  "fat": 5
}
```

### 오류 원인

DTO에 Validation 규칙은 정의했지만 Controller의 `@RequestBody` 앞에 `@Valid`를 붙이지 않았다.

```text
@NotNull, @PositiveOrZero
→ 검사 규칙 정의

@Valid
→ 실제 검사 실행
```

규칙은 존재했지만 검증이 실행되지 않은 상태였다.

### 해결

POST와 PUT 요청에 모두 `@Valid`를 추가했다.

```java
@PostMapping
public MealResponse saveMeal(
@Valid @RequestBody MealRequest mealRequest
        ) {
        return mealService.saveMeal(mealRequest);
        }
```

```java
@PutMapping("/{id}")
public MealResponse updateMeal(
@PathVariable Long id,
@Valid @RequestBody MealRequest mealRequest
        ) {
        return mealService.updateMeal(id, mealRequest);
        }
```

Validation 의존성도 확인했다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

### 결과

음수 데이터가 Service와 Repository까지 전달되지 않고 Controller 진입 단계에서 차단되었다.

```http
400 Bad Request
```

### 배운 점

Validation 규칙을 선언하는 것과 검증을 실행하는 것은 서로 다른 역할이다.

```text
Client
→ @RequestBody
→ @Valid
→ MealRequest 검증
→ 성공 시 Service
→ 실패 시 Exception
```

---

## 4.2 Validation 실패 메시지의 한글이 깨진 문제

### 문제 상황

Validation은 정상적으로 실패했지만 IntelliJ 콘솔에서는 한글 메시지가 깨져 출력되었다.

```text
default message [0 �̻��̾�� �մϴ�.]
```

반면 `application.properties`에 HTTP 인코딩 설정을 추가한 뒤 Postman 응답창에서는 한글 메시지가 정상적으로 표시되었다.

즉, 클라이언트 응답 인코딩 문제는 해결되었지만 IntelliJ 실행 콘솔의 한글 깨짐은 별도로 남아 있었다.

### 오류 원인

HTTP 응답 인코딩과 IntelliJ 콘솔 인코딩은 서로 다른 영역이다.

```text
HTTP 인코딩 설정
→ Postman 등 클라이언트가 받는 요청·응답 문자 처리

IntelliJ 콘솔 인코딩
→ 애플리케이션 로그와 예외 메시지를 IDE 콘솔에 표시하는 문자 처리
```

따라서 Spring Boot의 HTTP 인코딩 설정만으로는 IntelliJ 콘솔의 한글 깨짐까지 반드시 해결되지는 않는다.

또한 `MethodArgumentNotValidException`을 별도로 처리하지 않으면 클라이언트에는 Spring 내부 예외 정보가 길고 복잡하게 노출될 수 있다.

### 해결 1: HTTP 응답 인코딩 설정

`application.properties`에 다음 설정을 추가했다.

```properties
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

### 확인 결과

Postman 응답창에서는 Validation 한글 메시지가 정상적으로 표시되었다.

```json
{
  "status": 400,
  "message": "입력값이 올바르지 않습니다.",
  "errors": {
    "calories": "0 이상이어야 합니다."
  }
}
```

### 남은 문제: IntelliJ 콘솔 한글 깨짐

HTTP 인코딩 설정 후에도 IntelliJ 콘솔에서는 한글이 계속 깨져 보였다.

따라서 콘솔 문제는 HTTP 설정과 분리해 IntelliJ 실행 환경의 인코딩을 별도로 확인해야 한다.

예를 들어 Run Configuration의 VM options에 다음 값을 적용할 수 있다.

```text
-Dfile.encoding=UTF-8
-Dconsole.encoding=UTF-8
```

필요한 경우 IntelliJ의 파일 인코딩과 터미널·실행 콘솔 설정도 함께 확인한다.

### 해결 2: Validation 전역 예외 처리

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidationException(
        MethodArgumentNotValidException exception
) {
    Map<String, String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                    fieldError -> fieldError.getField(),
                    DefaultMessageSourceResolvable::getDefaultMessage,
                    (firstMessage, secondMessage) -> firstMessage,
                    LinkedHashMap::new
            ));

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("message", "입력값이 올바르지 않습니다.");
    response.put("errors", errors);

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
}
```

### 최종 결과

- Postman에서는 한글 Validation 메시지가 정상적으로 표시되었다.
- 클라이언트에는 긴 내부 예외 로그 대신 필드별 오류 JSON이 반환되었다.
- IntelliJ 콘솔의 한글 깨짐은 HTTP 응답 문제와 별개의 IDE 콘솔 인코딩 문제로 남았다.

### 배운 점

HTTP 요청·응답 인코딩과 IDE 콘솔 인코딩은 같은 문제가 아니다.

```text
server.servlet.encoding.*
→ 클라이언트 응답 문자 인코딩

JVM·IntelliJ 콘솔 인코딩
→ 실행 로그와 예외 메시지 표시
```

전역 예외 처리기는 클라이언트가 이해하기 쉬운 JSON을 만드는 역할을 하고, 콘솔 인코딩 설정은 개발자가 로그를 읽을 수 있게 만드는 별도의 설정이다.

---

# 5. Entity 수정 및 영속성 구조 개선

## 5.1 수정 API에서 새 Entity를 생성한 문제

### 문제 상황

기존 Meal을 수정하면서 새로운 Entity를 생성했다.

```java
public Meal updateMeal(Long id, MealRequest mealRequest) {
        Meal meal = new Meal(
        mealRequest.getMealName(),
        mealRequest.getCalories(),
        mealRequest.getProtein(),
        mealRequest.getCarbohydrate(),
        mealRequest.getFat()
        );

        return mealRepository.save(meal);
        }
```

수정 대상의 `id`를 전달받았지만 실제 코드에서는 사용하지 않았다.

### 오류 원인

`new Meal(...)`은 기존 Entity를 가져오는 것이 아니라 새로운 객체를 생성한다.

새 객체에는 기존 데이터의 ID가 없기 때문에 JPA는 신규 Entity로 판단할 수 있다.

그 결과 기존 행을 수정하는 것이 아니라 새로운 행이 추가될 가능성이 생긴다.

### 해결

먼저 ID로 기존 Meal을 조회한 뒤 해당 객체의 값을 변경했다.

```java
public Meal updateMeal(Long id, MealRequest mealRequest) {
        Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));

        meal.setMealName(mealRequest.getMealName());
        meal.setCalories(mealRequest.getCalories());
        meal.setProtein(mealRequest.getProtein());
        meal.setCarbohydrate(mealRequest.getCarbohydrate());
        meal.setFat(mealRequest.getFat());

        return mealRepository.save(meal);
        }
```

이후에는 Setter 대신 Entity의 `update()` 메서드로 리팩터링했다.

```java
meal.update(mealRequest);
```

### 결과

기존 식별자를 가진 Entity의 상태가 변경되고 해당 데이터가 수정되었다.

### 배운 점

`save()`는 신규 저장과 수정을 모두 처리할 수 있지만 어떤 객체를 전달하는지가 중요하다.

```text
new Meal()
→ 새로운 객체
→ 신규 저장 대상

findById(id)
→ 기존 식별자를 가진 Entity
→ 값 변경 후 기존 데이터 수정
```

POST에서는 새로운 자원을 생성하므로 `new Meal()`을 사용한다.

PUT에서는 기존 자원을 수정하므로 `findById()`로 수정 대상을 먼저 찾아야 한다.

---

## 5.2 Entity Setter 제거와 `update()` 메서드 도입

### 문제 상황

Lombok을 적용하면서 DTO와 Entity에 모두 `@Setter`를 붙일지 혼란이 생겼다.

```java
@Getter
@Setter
```

### 구조적 문제

Entity에 전체 Setter를 공개하면 애플리케이션 어디에서나 상태를 직접 변경할 수 있다.

```java
meal.setCalories(-999);
        meal.setMealName("");
```

이렇게 되면 누가, 언제, 어떤 의도로 값을 변경했는지 파악하기 어렵고 객체의 상태도 쉽게 오염될 수 있다.

### 해결

Request DTO는 JSON 데이터를 받아야 하므로 Getter와 Setter를 사용했다.

```java
@Getter
@Setter
public class MealRequest {
}
```

Entity는 Getter와 JPA용 기본 생성자만 공개했다.

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal {
}
```

수정은 의미 있는 메서드를 통해서만 수행하도록 했다.

```java
public void update(MealRequest request) {
        this.mealName = request.getMealName();
        this.calories = request.getCalories();
        this.protein = request.getProtein();
        this.carbohydrate = request.getCarbohydrate();
        this.fat = request.getFat();
        }
```

### 결과

Service에서 Entity 내부 필드를 하나씩 수정하지 않고 다음처럼 의도를 표현할 수 있게 되었다.

```java
meal.update(mealRequest);
```

### 배운 점

Lombok은 단순히 코드를 줄이는 도구가 아니다.

어떤 메서드를 자동 생성하고 외부에 공개할지 결정하는 것도 객체 설계의 일부이다.

```text
Request DTO
→ 클라이언트 입력 수신
→ Getter + Setter

Entity
→ 데이터베이스와 매핑되는 핵심 객체
→ Getter
→ 전체 Setter 제한
→ 의미 있는 상태 변경 메서드 사용
```

이는 객체의 내부 상태를 보호하는 캡슐화와 연결된다.

---

## 5.3 JPA 기본 생성자 이해

### 문제 상황

Entity에 다음 어노테이션이 왜 필요한지 이해하기 어려웠다.

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

### 원인

개발자가 직접 객체를 생성할 때는 매개변수 생성자를 호출한다.

```java
new Meal(mealName, calories, protein, carbohydrate, fat);
```

그러나 데이터베이스 조회 결과를 Entity로 만드는 과정에서는 개발자가 직접 `new Meal(...)`을 호출하지 않는다.

JPA가 기본 생성자를 사용해 Entity 객체를 생성한 뒤 데이터베이스 값을 매핑한다.

```text
Database row
→ JPA/Hibernate
→ 기본 생성자로 Meal 객체 생성
→ 필드값 매핑
```

### 해결

JPA가 사용할 수 있도록 기본 생성자를 제공하되, 애플리케이션 코드에서 의미 없는 빈 객체를 쉽게 생성하지 못하도록 접근 범위를 제한했다.

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

### 배운 점

```text
@NoArgsConstructor
→ JPA가 Entity를 생성하기 위해 필요

AccessLevel.PROTECTED
→ 외부에서 의미 없는 new Meal() 사용 제한
```

---

# 6. API 응답 구조 개선

## 6.1 Entity 직접 반환 구조 개선

### 문제 상황

CRUD 구현 직후 Controller는 `Meal` Entity를 그대로 반환했다.

```java
@GetMapping("/{id}")
public Meal findById(@PathVariable Long id) {
        return mealService.findById(id);
        }
```

기능은 정상적으로 동작했지만 Entity가 외부 API 응답에 직접 노출되었다.

### 구조적 문제

요청 데이터를 받을 때는 `MealRequest`를 사용했지만 응답 데이터를 보낼 때는 별도의 DTO가 없었다.

```text
Database
→ Entity
→ Client
```

이 구조에서는 다음 문제가 생길 수 있다.

- Entity 필드 변경이 API 응답 변경으로 이어짐
- 클라이언트에 불필요한 필드가 노출될 수 있음
- 내부 데이터베이스 구조와 외부 API가 강하게 결합됨

### 해결 1: Response DTO 생성

```java
@Getter
@Builder
public class MealResponse {

    private Long id;
    private String mealName;
    private Integer calories;
    private Integer protein;
    private Integer carbohydrate;
    private Integer fat;

    public static MealResponse from(Meal meal) {
        return MealResponse.builder()
                .id(meal.getId())
                .mealName(meal.getMealName())
                .calories(meal.getCalories())
                .protein(meal.getProtein())
                .carbohydrate(meal.getCarbohydrate())
                .fat(meal.getFat())
                .build();
    }
}
```

### 해결 2: Service 반환 타입 변경

```java
public MealResponse findById(Long id) {
        Meal meal = mealRepository.findById(id)
        .orElseThrow(() -> new MealNotFoundException(id));

        return MealResponse.from(meal);
        }
```

### 해결 3: Controller 반환 타입 변경

```java
@GetMapping("/{id}")
public MealResponse findById(@PathVariable Long id) {
        return mealService.findById(id);
        }
```

### 결과

클라이언트는 Entity가 아니라 응답에 필요한 데이터만 담긴 `MealResponse`를 받게 되었다.

### 배운 점

Service의 반환 타입이 바뀌면 해당 결과를 전달하는 Controller의 반환 타입도 함께 변경되어야 한다.

```text
Repository
→ Meal Entity

Service
→ Meal Entity를 MealResponse로 변환

Controller
→ MealResponse 반환

Client
→ 필요한 JSON 수신
```

백엔드의 각 계층은 따로 존재하는 것이 아니라 데이터 타입을 통해 하나의 흐름으로 연결되어 있다.

---

## 6.2 `static from()` 정적 팩토리 메서드 이해

### 문제 상황

다음 메서드에서 `static`이 데이터를 미리 고정하거나 미리 꺼내두는 역할이라고 이해했다.

```java
public static MealResponse from(Meal meal)
```

### 원인

Java에서 `static`은 Entity를 미리 조회하거나 데이터를 저장해 두는 의미가 아니다.

`static` 메서드는 특정 객체 인스턴스가 아니라 클래스 자체에 속한다.

### 이해 및 해결

`from()`이 `static`이기 때문에 `MealResponse` 객체를 먼저 생성하지 않고 클래스 이름으로 직접 호출할 수 있다.

```java
MealResponse.from(meal);
```

`static`이 없다면 호출을 위해 기존 `MealResponse` 객체가 필요하다.

```java
MealResponse response = new MealResponse();
        response.from(meal);
```

그러나 `from()`의 목적은 새로운 `MealResponse`를 만드는 것이다.

Response 객체를 만들기 위해 Response 객체를 먼저 생성하는 구조는 자연스럽지 않다.

### 역할

```text
Meal Entity
→ MealResponse.from(meal)
→ 새로운 MealResponse 생성
```

`from()`은 Entity를 Response DTO로 변환하는 정적 팩토리 메서드이다.

### 유지보수 측면의 장점

Service마다 다음 Builder 코드를 반복하지 않아도 된다.

```java
MealResponse.builder()
        .id(meal.getId())
        .mealName(meal.getMealName())
        .calories(meal.getCalories())
        .build();
```

모든 변환이 다음 한 줄로 통일된다.

```java
MealResponse.from(meal);
```

응답 필드가 변경되어도 `from()` 메서드를 중심으로 수정할 수 있어 유지보수가 쉬워진다.

### 배운 점

`static from()`의 핵심은 데이터를 미리 꺼내는 것이 아니라 다음 두 가지이다.

```text
1. 객체를 먼저 만들지 않고 클래스 이름으로 호출
2. Entity → Response DTO 변환 책임을 한곳에 모음
```

---

# 7. 최종 데이터 흐름

## 7.1 정상 요청

```text
Client
→ JSON Request
→ @RequestBody
→ @Valid
→ MealRequest
→ Controller
→ Service
→ Meal Entity
→ Repository
→ Database
→ Meal Entity
→ MealResponse.from()
→ MealResponse
→ Controller
→ JSON Response
→ Client
```

---

## 7.2 조회 대상이 없는 경우

```text
Client
→ Controller
→ Service
→ Repository
→ 데이터 없음
→ MealNotFoundException
→ GlobalExceptionHandler
→ 404 Not Found
→ JSON Response
```

---

## 7.3 Validation 실패

```text
Client
→ JSON Request
→ @Valid
→ Validation 실패
→ MethodArgumentNotValidException
→ GlobalExceptionHandler
→ 400 Bad Request
→ 필드별 오류 JSON
```

---

## 7.4 수정 요청

```text
PUT /api/meals/{id}
→ Controller가 id와 MealRequest를 받음
→ Service가 id로 기존 Meal 조회
→ meal.update(mealRequest)
→ mealRepository.save(meal)
→ MealResponse.from(updatedMeal)
→ 수정 결과 반환
```

---

# 8. 트러블슈팅을 통해 배운 핵심

1. DTO와 Entity는 역할이 다르며 Repository에는 Entity를 전달해야 한다.
2. 수정은 새로운 Entity를 만드는 것이 아니라 기존 Entity를 조회한 후 상태를 변경하는 것이다.
3. 메서드 체이닝에서는 각 메서드의 반환 타입을 확인해야 한다.
4. URL과 HTTP Method가 함께 API의 의미를 결정한다.
5. `void` 메서드는 작업은 수행하지만 값을 반환하지 않는다.
6. 객체의 상태 변경과 메서드의 반환은 서로 다른 개념이다.
7. Validation 규칙을 실제로 실행하려면 Controller에 `@Valid`가 필요하다.
8. 예외는 클라이언트가 이해할 수 있는 HTTP 응답으로 변환해야 한다.
9. Entity의 전체 Setter를 제거하면 객체의 상태를 더 안전하게 보호할 수 있다.
10. `update()`의 상태 변경과 `save()`의 데이터베이스 저장 및 반환은 서로 다른 역할이다.
11. JPA는 Entity 생성을 위해 기본 생성자를 필요로 한다.
12. Response DTO를 사용하면 Entity의 직접 노출을 방지할 수 있다.
13. Service 반환 타입이 변경되면 Controller 반환 타입도 함께 변경된다.
14. Builder는 Setter 없이 가독성 있게 객체를 생성할 수 있도록 돕는다.
15. 정적 팩토리 메서드는 객체 변환 책임을 한곳에 모아 중복을 줄인다.
16. 오류 메시지와 현재 코드가 일치하지 않으면 저장, 빌드, Gradle, 서버 실행 상태도 확인해야 한다.
17. 확정된 오류 원인과 추정 원인은 문서에서 구분해야 한다.

---

# 9. 오류 유형별 분류

## 9.1 오타 및 연결 오류

- `@PostMapping`과 `@PutMapping` 혼동
- Controller에서 `saveMeal()`과 `updateMeal()` 호출 혼동
- Getter만 생성하고 Setter 누락
- `orElseThrow()` 괄호 위치 오류
- 수정 전 빌드 결과가 남아 현재 코드와 오류가 일치하지 않음

## 9.2 타입 및 반환값 오류

- Repository에 DTO 전달
- `delete()` 결과를 반환하려 함
- `void update()` 결과를 반환하려 함
- Service와 Controller 반환 타입 불일치

## 9.3 API 및 예외 처리 오류

- 존재하지 않는 ID를 적절한 404 응답으로 변환하지 않음
- `@Valid` 누락으로 Validation 미실행
- Validation 내부 예외 로그를 클라이언트용 JSON으로 변환하지 않음
- 한글 인코딩 깨짐

## 9.4 구조 및 설계 개선

- 수정 시 새로운 Entity 생성
- Entity 전체 Setter 공개
- JPA 기본 생성자 접근 범위 설계
- Entity 직접 반환
- Response DTO와 Builder 도입
- `static from()`을 통한 변환 책임 집중

---

# 10. 앞으로 사용할 트러블슈팅 작성 형식

```md
## 오류 또는 개선 제목

### 문제 상황
어떤 기능을 구현하다가 어떤 문제가 발생했는가?

### 오류 메시지
실제 오류 메시지나 비정상 동작은 무엇이었는가?

### 원인
오류가 발생한 이유는 무엇인가?

원인이 확정되지 않았다면
`추정 원인 및 점검 항목`으로 구분한다.

### 해결
어떤 코드나 설정을 어떻게 수정했는가?

### 결과
수정 후 어떤 응답, SQL, 상태 코드 또는 동작을 확인했는가?

### 배운 점
이 문제를 통해 어떤 Java, Spring, JPA 또는 객체지향 개념을 이해했는가?
```

---

# 11. 포트폴리오용 핵심 트러블슈팅 후보

전체 학습 기록은 상세하게 유지하되, 포트폴리오에는 다음 사례를 중심으로 요약할 수 있다.

## 11.1 수정 API에서 신규 데이터가 생성되던 문제

- `new Meal()`과 기존 Entity 조회의 차이
- JPA 식별자와 신규 저장·수정 판단
- 수정 대상 조회 후 상태 변경

## 11.2 Validation 규칙이 실행되지 않던 문제

- DTO 제약 조건과 `@Valid`의 역할 분리
- 잘못된 입력을 Service 이전 단계에서 차단

## 11.3 `void update()` 결과를 반환하려 한 문제

- 상태 변경과 반환값의 차이
- Entity 행동, Repository 저장, Response 변환 책임 분리

## 11.4 Entity 직접 반환 구조 개선

- 내부 Entity와 외부 API 응답 분리
- Response DTO 도입
- Builder와 `static from()`을 통한 변환 책임 집중

## 11.5 Entity Setter 제거

- 무분별한 상태 변경 방지
- 캡슐화
- 의미 있는 수정 메서드 도입

---

# 마무리

이번 트러블슈팅 과정에서는 단순히 Spring Boot CRUD 코드를 완성하는 것보다 다음 관점을 익힌 것이 가장 큰 성과였다.

```text
클라이언트가 어떤 데이터를 보내는가?
→ Controller는 어떤 형태로 받는가?
→ Service는 어떤 로직을 수행하는가?
→ Entity는 자신의 상태를 어떻게 관리하는가?
→ Repository에는 어떤 객체를 전달하는가?
→ 결과를 어떤 Response DTO로 반환하는가?
→ 오류는 어떤 HTTP 응답으로 번역되는가?
```

백엔드 개발은 각 코드를 따로 작성하는 작업이 아니라, 데이터가 계층별 역할에 맞게 안전하게 이동하도록 흐름을 설계하는 작업이다.
