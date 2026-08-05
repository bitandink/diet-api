package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.repository.MealRepository;
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
}