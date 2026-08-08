package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.service.MealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MealService mealService;

    @Test
    @DisplayName("Meal 등록 요청에서 식단 이름이 비어 있으면 400을 반환한다")
    void saveMeal_fail_whenMealNameIsBlank() throws Exception {

        // given
        MealRequest request = new MealRequest();
        request.setMealName("");
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/api/meals"))
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 필수 입력 항목입니다."));

        verifyNoInteractions(mealService);
    }

    @Test
    @DisplayName("Meal 등록 요청에서 식단 이름이 50자를 넘으면 400을 반환한다")
    void saveMeal_fail_whenMealNameIsTooLong() throws Exception {

        // given
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
                .andExpect(jsonPath("$.errors.mealName")
                        .value("식단 이름은 50자를 넘을 수 없습니다."));

        verifyNoInteractions(mealService);
    }

    @Test
    @DisplayName("Meal 등록 요청에서 칼로리가 null이면 400을 반환한다")
    void saveMeal_fail_whenCaloriesIsNull() throws Exception {

        // given
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

        // given
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/api/meals"))
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

        verifyNoInteractions(mealService);
    }

    @Test
    @DisplayName("Meal 등록 요청이 정상적이면 Meal을 생성하고 201을 반환한다")
    void saveMeal_success() throws Exception {

        // given
        MealRequest request = new MealRequest();
        request.setMealName("닭가슴살 샐러드");
        request.setCalories(350);
        request.setProtein(40);
        request.setCarbohydrate(30);
        request.setFat(10);

        MealResponse response = MealResponse.builder()
                .id(1L)
                .mealName("닭가슴살 샐러드")
                .calories(350)
                .protein(40)
                .carbohydrate(30)
                .fat(10)
                .build();

        when(mealService.saveMeal(any(MealRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/meals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Meal 등록 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.mealName")
                        .value("닭가슴살 샐러드"))
                .andExpect(jsonPath("$.data.calories").value(350))
                .andExpect(jsonPath("$.data.protein").value(40))
                .andExpect(jsonPath("$.data.carbohydrate").value(30))
                .andExpect(jsonPath("$.data.fat").value(10));

        verify(mealService)
                .saveMeal(any(MealRequest.class));
    }
}