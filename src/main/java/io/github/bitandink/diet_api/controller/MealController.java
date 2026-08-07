package io.github.bitandink.diet_api.controller;

import io.github.bitandink.diet_api.dto.ApiResponse;
import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

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

    @PostMapping
    public ResponseEntity<ApiResponse<MealResponse>> saveMeal(
            @Valid @RequestBody MealRequest mealRequest)
        {
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MealResponse>> findById(
            @PathVariable Long id)
        {
            MealResponse meal = mealService.findById(id);

            ApiResponse<MealResponse> response =
                    new ApiResponse<>(
                            true,
                            "Meals 조회 성공",
                            meal
                    );

        return ResponseEntity.ok(response);
    }

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
}