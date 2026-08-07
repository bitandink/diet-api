package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.repository.MealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private MealService mealService;

    private Meal meal;
    private MealRequest mealRequest;

    @BeforeEach
    void setUp() {
        meal = new Meal(
                "닭가슴살 샐러드",
                350,
                40,
                30,
                10
        );

        mealRequest = new MealRequest();
        mealRequest.setMealName("닭가슴살 샐러드");
        mealRequest.setCalories(350);
        mealRequest.setProtein(40);
        mealRequest.setCarbohydrate(30);
        mealRequest.setFat(10);
    }

    @Test
    @DisplayName("전체 식단을 조회한다")
    void findAll() {
        // given
        Meal meal2 = new Meal(
                "고구마",
                200,
                3,
                45,
                1
        );

        when(mealRepository.findAll())
                .thenReturn(List.of(meal, meal2));

        // when
        List<MealResponse> responses = mealService.findAll();

        // then
        assertEquals(2, responses.size());

        assertEquals("닭가슴살 샐러드", responses.get(0).getMealName());
        assertEquals(350, responses.get(0).getCalories());

        assertEquals("고구마", responses.get(1).getMealName());
        assertEquals(200, responses.get(1).getCalories());

        verify(mealRepository).findAll();
    }

    @Test
    @DisplayName("ID로 식단을 조회한다")
    void findById() {
        // given
        Long id = 1L;

        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));

        // when
        MealResponse response = mealService.findById(id);

        // then
        assertEquals("닭가슴살 샐러드", response.getMealName());
        assertEquals(350, response.getCalories());
        assertEquals(40, response.getProtein());
        assertEquals(30, response.getCarbohydrate());
        assertEquals(10, response.getFat());

        verify(mealRepository).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 식단을 조회하면 예외가 발생한다")
    void findById_notFound() {
        // given
        Long id = 999L;

        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.findById(id)
        );

        verify(mealRepository).findById(id);
    }

    @Test
    @DisplayName("새로운 식단을 등록한다")
    void saveMeal() {
        // given
        when(mealRepository.save(any(Meal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MealResponse response = mealService.saveMeal(mealRequest);

        // then
        assertEquals("닭가슴살 샐러드", response.getMealName());
        assertEquals(350, response.getCalories());
        assertEquals(40, response.getProtein());
        assertEquals(30, response.getCarbohydrate());
        assertEquals(10, response.getFat());

        verify(mealRepository).save(any(Meal.class));
    }

    @Test
    @DisplayName("기존 식단을 수정한다")
    void updateMeal() {
        // given
        Long id = 1L;

        MealRequest updateRequest = new MealRequest();
        updateRequest.setMealName("연어 샐러드");
        updateRequest.setCalories(450);
        updateRequest.setProtein(35);
        updateRequest.setCarbohydrate(20);
        updateRequest.setFat(25);

        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));

        // when
        MealResponse response = mealService.updateMeal(id, updateRequest);

        // then
        assertEquals("연어 샐러드", response.getMealName());
        assertEquals(450, response.getCalories());
        assertEquals(35, response.getProtein());
        assertEquals(20, response.getCarbohydrate());
        assertEquals(25, response.getFat());

        verify(mealRepository).findById(id);

        // save()는 호출되지 않는 것이 정상
        verify(mealRepository, never()).save(any(Meal.class));
    }

    @Test
    @DisplayName("존재하지 않는 식단을 수정하면 예외가 발생한다")
    void updateMeal_notFound() {
        // given
        Long id = 999L;

        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.updateMeal(id, mealRequest)
        );

        verify(mealRepository).findById(id);
        verify(mealRepository, never()).save(any(Meal.class));
    }

    @Test
    @DisplayName("식단을 삭제한다")
    void deleteMeal() {
        // given
        Long id = 1L;

        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));

        // when
        mealService.deleteMeal(id);

        // then
        verify(mealRepository).findById(id);
        verify(mealRepository).delete(meal);
    }

    @Test
    @DisplayName("존재하지 않는 식단을 삭제하면 예외가 발생한다")
    void deleteMeal_notFound() {
        // given
        Long id = 999L;

        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.deleteMeal(id)
        );

        verify(mealRepository).findById(id);
        verify(mealRepository, never()).delete(any(Meal.class));
    }
}