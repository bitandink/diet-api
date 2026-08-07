package io.github.bitandink.diet_api.entity;

import io.github.bitandink.diet_api.dto.MealRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mealName;

    private Integer calories;

    private Integer protein;

    private Integer carbohydrate;

    private Integer fat;

    public Meal(
            String mealName,
            Integer calories,
            Integer protein,
            Integer carbohydrate,
            Integer fat
    ) {
        this.mealName = mealName;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrate = carbohydrate;
        this.fat = fat;
    }

    public void update(
            String mealName,
            Integer calories,
            Integer protein,
            Integer carbohydrate,
            Integer fat
    ) {
        this.mealName = mealName;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrate = carbohydrate;
        this.fat = fat;
    }
}