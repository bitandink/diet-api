package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.repository.MealRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public List<Meal> findAll() {
        return mealRepository.findAll();
    }

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

    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));
    }

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

    public void deleteMeal(Long id) {

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        mealRepository.delete(meal);
    }
}