package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.service.MealService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public List<Meal> findAll() {
        return mealService.findAll();
    }
}