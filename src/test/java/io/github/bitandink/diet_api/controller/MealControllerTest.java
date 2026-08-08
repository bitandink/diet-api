package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.service.MealService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * @WebMvcTest(MealController.class)
 *
 * Spring 애플리케이션 전체를 실행하는 것이 아니라
 * Web MVC 계층 중 MealController 테스트에 필요한 부분만 실행한다.
 *
 * 이 테스트에서 확인하려는 범위:
 *
 * HTTP Request
 *      ↓
 * MealController
 *      ↓
 * Request DTO 역직렬화
 *      ↓
 * @Valid validation
 *      ↓
 * MealService 호출
 *      ↓
 * GlobalExceptionHandler
 *      ↓
 * HTTP Response
 *
 * 반대로 실제 Repository나 DB까지 테스트하지는 않는다.
 *
 * Service는 아래의 @MockitoBean으로 가짜 객체(Mock)로 교체한다.
 */
@WebMvcTest(MealController.class)
class MealControllerTest {


    /*
     * MockMvc
     *
     * 실제 서버를 특정 포트에 띄우지 않고도
     * HTTP 요청을 보내는 것처럼 Controller를 테스트할 수 있게 해준다.
     *
     * 예:
     *
     * mockMvc.perform(
     *     get("/api/meals/1")
     * )
     *
     * 실제 클라이언트가 API를 호출하는 상황을 테스트 코드에서 재현한다.
     */
    @Autowired
    private MockMvc mockMvc;


    /*
     * ObjectMapper
     *
     * Java 객체를 JSON으로 변환하기 위해 사용한다.
     *
     * MealRequest
     *      ↓
     * JSON 문자열
     *
     * Controller는 JSON 요청을 받기 때문에
     * 테스트에서도 실제 API 요청과 최대한 비슷하게 JSON을 전송한다.
     *
     * 예:
     *
     * objectMapper.writeValueAsString(request)
     *
     * {
     *   "mealName": "닭가슴살 샐러드",
     *   "calories": 350,
     *   ...
     * }
     */
    @Autowired
    private ObjectMapper objectMapper;


    /*
     * @MockitoBean
     *
     * 실제 MealService 대신 Mockito가 만든 가짜 MealService를
     * Spring Bean으로 등록한다.
     *
     * Controller는 평소처럼 MealService를 주입받지만
     * 테스트에서는 이 Mock 객체를 사용한다.
     *
     * 그래서 실제 DB나 Repository 없이도:
     *
     * "Service가 이 값을 반환한다고 가정했을 때
     *  Controller가 올바른 HTTP 응답을 반환하는가?"
     *
     * 를 독립적으로 테스트할 수 있다.
     */
    @MockitoBean
    private MealService mealService;


    /*
     * ============================================================
     * POST /api/meals
     *
     * Meal 등록 API validation 테스트
     * ============================================================
     */


    @Test
    @DisplayName("Meal 등록 요청에서 식단 이름이 비어 있으면 400을 반환한다")
    void saveMeal_fail_whenMealNameIsBlank() throws Exception {

        /*
         * given
         *
         * API로 전달할 요청 객체를 준비한다.
         *
         * 다른 값은 정상적으로 넣고
         * mealName만 빈 문자열로 만들어서
         * "mealName validation" 하나에 집중한다.
         */
        MealRequest request = new MealRequest();
        request.setMealName("");
        request.setCalories(350);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        /*
         * when
         *
         * POST /api/meals 요청을 전송한다.
         *
         * contentType:
         * 클라이언트가 JSON 데이터를 보내고 있다는 뜻.
         *
         * content:
         * 실제 HTTP request body에 들어갈 JSON.
         */
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                /*
                 * then
                 *
                 * @Valid 검증에 실패했기 때문에
                 * HTTP Status가 400이어야 한다.
                 */
                .andExpect(status().isBadRequest())

                /*
                 * GlobalExceptionHandler가 만든 ErrorResponse의
                 * 기본 구조를 확인한다.
                 */
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals"))

                /*
                 * validation 결과 중
                 * mealName 필드에 정확한 에러 메시지가 들어갔는지 확인한다.
                 *
                 * JSON 구조:
                 *
                 * "errors": {
                 *     "mealName": "식단 이름은 필수 입력 항목입니다."
                 * }
                 */
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 필수 입력 항목입니다."));


        /*
         * validation에서 요청이 차단되었기 때문에
         * Service는 절대로 호출되면 안 된다.
         *
         * 즉:
         *
         * 잘못된 요청
         *      ↓
         * @Valid 실패
         *      ↓
         * GlobalExceptionHandler
         *
         * 여기서 끝나야 한다.
         *
         * Service나 DB까지 내려가면 안 된다.
         */
        verifyNoInteractions(mealService);
    }


    @Test
    @DisplayName("Meal 등록 요청에서 식단 이름이 50자를 넘으면 400을 반환한다")
    void saveMeal_fail_whenMealNameIsTooLong() throws Exception {

        // given
        //
        // 정확히 validation 경계를 넘기기 위해
        // 50자가 아니라 51자를 전달한다.
        MealRequest request = new MealRequest();
        request.setMealName("a".repeat(51));
        request.setCalories(350);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        // when & then
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())

                // @Size 등의 validation annotation에 설정한
                // 사용자용 메시지가 그대로 반환되는지 확인
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 50자를 넘을 수 없습니다."));


        // Controller가 Service까지 요청을 전달하지 않았는지 확인
        verifyNoInteractions(mealService);
    }


    @Test
    @DisplayName("Meal 등록 요청에서 칼로리가 null이면 400을 반환한다")
    void saveMeal_fail_whenCaloriesIsNull() throws Exception {

        // given
        //
        // calories만 null로 만든다.
        // @NotNull validation 동작을 확인하기 위한 테스트.
        MealRequest request = new MealRequest();
        request.setMealName("닭가슴살 샐러드");
        request.setCalories(null);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        // when & then
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.calories")
                        .value("칼로리는 필수 입력 항목입니다."));


        verifyNoInteractions(mealService);
    }


    @Test
    @DisplayName("Meal 등록 요청에서 칼로리가 음수이면 400을 반환한다")
    void saveMeal_fail_whenCaloriesIsNegative() throws Exception {

        // given
        //
        // 0은 허용되고 음수는 허용되지 않는 validation이라고 가정.
        // 따라서 경계 바로 아래 값인 -1을 사용한다.
        MealRequest request = new MealRequest();
        request.setMealName("닭가슴살 샐러드");
        request.setCalories(-1);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        // when & then
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.calories")
                        .value("칼로리는 0 이상이어야 합니다."));


        verifyNoInteractions(mealService);
    }


    @Test
    @DisplayName("Meal 등록 요청에서 여러 입력값이 잘못되면 필드별 에러를 반환한다")
    void saveMeal_fail_whenMultipleFieldsAreInvalid() throws Exception {

        /*
         * given
         *
         * 이번에는 여러 필드를 동시에 잘못된 값으로 만든다.
         *
         * 목적은 하나의 validation 에러만 처리되는 것이 아니라
         * 여러 FieldError가 errors Map으로 제대로 변환되는지 확인하는 것.
         */
        MealRequest request = new MealRequest();
        request.setMealName("");
        request.setCalories(-100);
        request.setProtein(-10);
        request.setCarbohydrate(-20);
        request.setFat(-5);


        // when & then
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())

                // 공통 ErrorResponse 검증
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals"))

                /*
                 * 각 validation 에러가
                 *
                 * errors:
                 *   fieldName -> message
                 *
                 * 구조로 내려오는지 확인한다.
                 */
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 필수 입력 항목입니다."))
                .andExpect(jsonPath("$.errors.calories")
                        .value("칼로리는 0 이상이어야 합니다."))
                .andExpect(jsonPath("$.errors.protein")
                        .value("단백질은 0 이상이어야 합니다."))
                .andExpect(jsonPath("$.errors.carbohydrate")
                        .value("탄수화물은 0 이상이어야 합니다."))
                .andExpect(jsonPath("$.errors.fat")
                        .value("지방은 0 이상이어야 합니다."));


        // 잘못된 입력은 Service까지 내려가면 안 된다.
        verifyNoInteractions(mealService);
    }


    @Test
    @DisplayName("Meal 등록 요청이 정상적이면 Meal을 생성하고 201을 반환한다")
    void saveMeal_success() throws Exception {

        /*
         * given - 요청 데이터
         *
         * 클라이언트가 보내는 정상적인 Meal 등록 요청.
         */
        MealRequest request = new MealRequest();
        request.setMealName("닭가슴살 샐러드");
        request.setCalories(350);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        /*
         * given - Service 반환값
         *
         * 실제 Service가 Meal을 저장하면
         * 이런 MealResponse가 반환된다고 가정한다.
         *
         * Controller 테스트에서는 DB를 사용하지 않으므로
         * ID도 직접 만들어준다.
         */
        MealResponse response = MealResponse.builder()
                .id(1L)
                .mealName("닭가슴살 샐러드")
                .calories(350)
                .protein(40)
                .carbohydrate(30)
                .fat(10)
                .build();


        /*
         * Mock Service 동작 정의
         *
         * mealService.saveMeal(...)
         *
         * 이 호출되면 실제 Service 로직을 실행하지 않고
         * 위에서 만든 response를 반환한다.
         *
         * any(MealRequest.class)
         *
         * 는 MealRequest 타입 객체라면 어떤 객체든 허용한다는 의미.
         */
        when(mealService.saveMeal(any(MealRequest.class)))
                .thenReturn(response);


        /*
         * when
         *
         * 실제 등록 요청을 재현한다.
         */
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                /*
                 * then
                 *
                 * 새로운 Resource를 생성했기 때문에
                 * 201 Created를 기대한다.
                 */
                .andExpect(status().isCreated())

                // ApiResponse 공통 영역
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Meal 등록 성공"))

                // 실제 저장 결과인 data 검증
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.mealName")
                        .value("닭가슴살 샐러드"))
                .andExpect(jsonPath("$.data.calories").value(350))
                .andExpect(jsonPath("$.data.protein").value(40))
                .andExpect(jsonPath("$.data.carbohydrate").value(30))
                .andExpect(jsonPath("$.data.fat").value(10));


        /*
         * Controller가 정상 요청을 받았을 때
         * 실제로 MealService.saveMeal()을 호출했는지 확인한다.
         *
         * validation 실패 테스트에서는 Service 호출이 없어야 했고,
         * 성공 테스트에서는 Service 호출이 있어야 한다.
         */
        verify(mealService)
                .saveMeal(any(MealRequest.class));
    }


    /*
     * ============================================================
     * GET /api/meals/{id}
     *
     * Meal 단건 조회 테스트
     * ============================================================
     */


    @Test
    @DisplayName("Meal 단건 조회에 성공하면 Meal 정보와 200을 반환한다")
    void getMeal_success() throws Exception {

        /*
         * given
         *
         * Service의 findById(1L)가 호출되면
         * 아래 Meal 정보를 반환한다고 가정한다.
         */
        MealResponse response = MealResponse.builder()
                .id(1L)
                .mealName("닭가슴살 샐러드")
                .calories(350)
                .protein(40)
                .carbohydrate(30)
                .fat(10)
                .build();


        when(mealService.findById(1L))
                .thenReturn(response);


        /*
         * when
         *
         * GET /api/meals/1
         *
         * 을 요청한다.
         *
         * {id} 자리에 1L이 들어간다.
         */
        mockMvc.perform(
                        get("/api/meals/{id}", 1L)
                )

                /*
                 * then
                 *
                 * 정상 조회이므로 200 OK.
                 */
                .andExpect(status().isOk())

                // ApiResponse 확인
                .andExpect(jsonPath("$.success").value(true))

                // 조회된 Meal 데이터 확인
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.mealName")
                        .value("닭가슴살 샐러드"))
                .andExpect(jsonPath("$.data.calories").value(350))
                .andExpect(jsonPath("$.data.protein").value(40))
                .andExpect(jsonPath("$.data.carbohydrate").value(30))
                .andExpect(jsonPath("$.data.fat").value(10));


        /*
         * URL에서 전달받은 id=1이
         * Service까지 정확하게 전달되었는지 확인한다.
         */
        verify(mealService).findById(1L);
    }


    @Test
    @DisplayName("존재하지 않는 Meal을 조회하면 404를 반환한다")
    void getMeal_fail_whenMealDoesNotExist() throws Exception {

        /*
         * given
         *
         * 존재하지 않는 ID를 Service에 전달하면
         * MealNotFoundException이 발생한다고 가정한다.
         *
         * 이 예외는 이후 GlobalExceptionHandler가 처리한다.
         */
        when(mealService.findById(999L))
                .thenThrow(new MealNotFoundException(999L));


        /*
         * when
         *
         * GET /api/meals/999
         */
        mockMvc.perform(
                        get("/api/meals/{id}", 999L)
                )

                /*
                 * then
                 *
                 * 흐름:
                 *
                 * Controller
                 *      ↓
                 * Service Mock
                 *      ↓
                 * MealNotFoundException
                 *      ↓
                 * GlobalExceptionHandler
                 *      ↓
                 * ErrorResponse + 404
                 */
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals/999"));


        /*
         * Controller가 요청받은 999L을
         * Service로 전달했는지도 검증한다.
         */
        verify(mealService).findById(999L);
    }


    /*
     * ============================================================
     * GET /api/meals
     *
     * Meal 전체 조회 테스트
     * ============================================================
     */


    @Test
    @DisplayName("Meal 전체 조회에 성공하면 Meal 목록과 200을 반환한다")
    void getMeals_success() throws Exception {

        /*
         * given
         *
         * Service가 Meal 2개를 반환한다고 가정한다.
         */
        MealResponse firstMeal = MealResponse.builder()
                .id(1L)
                .mealName("닭가슴살 샐러드")
                .calories(350)
                .protein(40)
                .carbohydrate(30)
                .fat(10)
                .build();

        MealResponse secondMeal = MealResponse.builder()
                .id(2L)
                .mealName("연어 덮밥")
                .calories(520)
                .protein(32)
                .carbohydrate(55)
                .fat(18)
                .build();


        /*
         * 두 MealResponse를 List로 만든다.
         *
         * 이 List는 JSON으로 변환되면 배열이 된다.
         */
        List<MealResponse> responses = List.of(
                firstMeal,
                secondMeal
        );


        /*
         * Service의 전체 조회 결과를 Mock으로 지정한다.
         */
        when(mealService.findAll())
                .thenReturn(responses);


        /*
         * when
         *
         * GET /api/meals
         */
        mockMvc.perform(
                        get("/api/meals")
                )

                /*
                 * then
                 */
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))

                /*
                 * JSON 배열 첫 번째 요소
                 *
                 * data[0]
                 */
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].mealName")
                        .value("닭가슴살 샐러드"))
                .andExpect(jsonPath("$.data[0].calories")
                        .value(350))

                /*
                 * JSON 배열 두 번째 요소
                 *
                 * data[1]
                 */
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].mealName")
                        .value("연어 덮밥"))
                .andExpect(jsonPath("$.data[1].calories")
                        .value(520))

                /*
                 * 배열의 전체 크기까지 확인한다.
                 *
                 * 단순히 첫 번째, 두 번째 데이터가 존재하는 것뿐 아니라
                 * 정확하게 두 개가 반환되었는지 확인.
                 */
                .andExpect(jsonPath("$.data.length()")
                        .value(2));


        // Controller가 Service 전체 조회 메서드를 호출했는지 확인
        verify(mealService).findAll();
    }


    /*
     * ============================================================
     * PUT /api/meals/{id}
     *
     * Meal 수정 테스트
     * ============================================================
     */


    @Test
    @DisplayName("Meal 수정 요청이 정상적이면 Meal을 수정하고 200을 반환한다")
    void updateMeal_success() throws Exception {

        /*
         * given - 클라이언트 수정 요청
         */
        MealRequest request = new MealRequest();
        request.setMealName("수정된 닭가슴살 샐러드");
        request.setCalories(400);
        request.setProtein(45);
        request.setCarbohydrate(35);
        request.setFat(12);


        /*
         * given - 수정 완료 후 Service의 반환값
         */
        MealResponse response = MealResponse.builder()
                .id(1L)
                .mealName("수정된 닭가슴살 샐러드")
                .calories(400)
                .protein(45)
                .carbohydrate(35)
                .fat(12)
                .build();


        /*
         * Service Mock 설정
         *
         * eq(1L)
         * → 첫 번째 인자는 정확하게 1L이어야 한다.
         *
         * any(MealRequest.class)
         * → 두 번째 인자는 MealRequest 타입이면 된다.
         */
        when(mealService.updateMeal(
                eq(1L),
                any(MealRequest.class)
        )).thenReturn(response);


        /*
         * when
         *
         * PUT /api/meals/1
         *
         * URL에는 수정 대상의 ID가 들어가고
         * Request Body에는 수정할 값이 들어간다.
         */
        mockMvc.perform(
                        put("/api/meals/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                /*
                 * then
                 *
                 * 정상 수정이므로 200 OK
                 */
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))

                // 수정 결과 확인
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.mealName")
                        .value("수정된 닭가슴살 샐러드"))
                .andExpect(jsonPath("$.data.calories").value(400))
                .andExpect(jsonPath("$.data.protein").value(45))
                .andExpect(jsonPath("$.data.carbohydrate").value(35))
                .andExpect(jsonPath("$.data.fat").value(12));


        /*
         * URL의 id가 Service까지 정확히 전달되었는지 확인한다.
         */
        verify(mealService).updateMeal(
                eq(1L),
                any(MealRequest.class)
        );
    }


    @Test
    @DisplayName("Meal 수정 요청의 입력값이 올바르지 않으면 400을 반환한다")
    void updateMeal_fail_whenRequestIsInvalid() throws Exception {

        /*
         * given
         *
         * mealName을 빈 값으로 만들어 validation을 실패시킨다.
         *
         * POST에서 validation 규칙 자체는 이미 자세하게 테스트했기 때문에
         * PUT에서는 대표적인 잘못된 요청 하나만 테스트한다.
         *
         * 여기서 확인하고 싶은 핵심:
         *
         * "PUT 요청에도 @Valid가 적용되어 있는가?"
         */
        MealRequest request = new MealRequest();
        request.setMealName("");
        request.setCalories(350);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);


        // when
        mockMvc.perform(
                        put("/api/meals/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                // then
                .andExpect(status().isBadRequest())

                // GlobalExceptionHandler의 validation 응답 확인
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals/1"))
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 필수 입력 항목입니다."));


        /*
         * 중요:
         *
         * validation 실패는 Controller에서 Service를 호출하기 전에
         * 처리되어야 한다.
         *
         * 따라서 Service 호출이 하나라도 발생하면 테스트 실패.
         */
        verifyNoInteractions(mealService);
    }

    @Test
    @DisplayName("존재하지 않는 Meal을 수정하면 404를 반환한다")
    void updateMeal_fail_whenMealDoesNotExist() throws Exception {

        /*
         * given
         *
         * 요청 데이터 자체는 정상이다.
         *
         * 즉 @Valid는 통과해야 하고,
         * 그 이후 Service에서 "수정할 Meal이 없다"는 상황을 만든다.
         */
        MealRequest request = new MealRequest();
        request.setMealName("연어 샐러드");
        request.setCalories(450);
        request.setProtein(35);
        request.setCarbohydrate(20);
        request.setFat(25);


        /*
         * Service Mock 설정
         *
         * 999번 Meal을 수정하려 하면
         * MealNotFoundException이 발생한다고 가정한다.
         *
         * 실제 흐름에서는 Service가 Repository.findById(999L)를 호출하고
         * 데이터가 없으면 이 예외를 던지게 된다.
         */
        when(mealService.updateMeal(
                eq(999L),
                any(MealRequest.class)
        )).thenThrow(new MealNotFoundException(999L));


        /*
         * when
         *
         * PUT /api/meals/999
         */
        mockMvc.perform(
                        put("/api/meals/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )

                /*
                 * then
                 *
                 * Service에서 발생한 MealNotFoundException을
                 * GlobalExceptionHandler가 받아서 404로 변환해야 한다.
                 */
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals/999"));


        /*
         * validation은 정상 통과했으므로
         * Service는 실제로 호출되어야 한다.
         */
        verify(mealService).updateMeal(
                eq(999L),
                any(MealRequest.class)
        );
    }

    @Test
    @DisplayName("Meal 삭제에 성공하면 200을 반환한다")
    void deleteMeal_success() throws Exception {

        // when & then
        mockMvc.perform(
                        delete("/api/meals/{id}", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Meal 삭제 성공"));

        // Controller가 URL의 id를 Service에 전달했는지 확인
        verify(mealService).deleteMeal(1L);
    }

    @Test
    @DisplayName("존재하지 않는 Meal을 삭제하면 404를 반환한다")
    void deleteMeal_fail_whenMealDoesNotExist() throws Exception {

        /*
         * given
         *
         * 999번 Meal을 삭제하려 하면
         * Service가 MealNotFoundException을 던진다고 가정한다.
         *
         * deleteMeal()은 void 메서드이기 때문에
         * Mockito의 doThrow() 문법을 사용한다.
         */
        doThrow(new MealNotFoundException(999L))
                .when(mealService)
                .deleteMeal(999L);


        /*
         * when
         *
         * DELETE /api/meals/999
         */
        mockMvc.perform(
                        delete("/api/meals/{id}", 999L)
                )

                /*
                 * then
                 *
                 * Service 예외를 GlobalExceptionHandler가 받아
                 * 404 응답으로 변환해야 한다.
                 */
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path")
                        .value("/api/meals/999"));


        /*
         * Controller가 요청받은 ID를
         * Service에 제대로 전달했는지 확인한다.
         */
        verify(mealService).deleteMeal(999L);
    }
}