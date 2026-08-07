package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.repository.MealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    // 전체 식단 조회
    public List<MealResponse> findAll() {
        return mealRepository.findAll()
                .stream()
                .map(MealResponse::from)
                .toList();
    }

    // 특정 식단 조회
    public MealResponse findById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        return MealResponse.from(meal);
    }

    // 새로운 식단 등록
    public MealResponse saveMeal(MealRequest mealRequest) {
        Meal meal = new Meal(
                mealRequest.getMealName(),
                mealRequest.getCalories(),
                mealRequest.getProtein(),
                mealRequest.getCarbohydrate(),
                mealRequest.getFat()
        );

        Meal savedMeal = mealRepository.save(meal);

        return MealResponse.from(savedMeal);
    }

    // 기존 식단 수정
    @Transactional
    public MealResponse updateMeal(Long id, MealRequest request) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        meal.update(
                request.getMealName(),
                request.getCalories(),
                request.getProtein(),
                request.getCarbohydrate(),
                request.getFat()
        );

        return MealResponse.from(meal);
    }

    // 식단 삭제
    public void deleteMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        mealRepository.delete(meal);
    }
}