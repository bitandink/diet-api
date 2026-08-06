package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.service.MealService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public Meal saveMeal(@Valid @RequestBody MealRequest mealRequest) {
        return mealService.saveMeal(mealRequest);
    }

    @GetMapping("/{id}")
    public Meal findById(@PathVariable Long id) {
        return mealService.findById(id);
    }

    @PutMapping("/{id}")
    public Meal updateMeal(@Valid @PathVariable Long id, @RequestBody MealRequest mealRequest) {
        return mealService.updateMeal(id, mealRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);

        return ResponseEntity.noContent().build();
    }
}