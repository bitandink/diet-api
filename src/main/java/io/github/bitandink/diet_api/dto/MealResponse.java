package io.github.bitandink.diet_api.dto;

import io.github.bitandink.diet_api.entity.Meal;
import lombok.Builder;
import lombok.Getter;

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
