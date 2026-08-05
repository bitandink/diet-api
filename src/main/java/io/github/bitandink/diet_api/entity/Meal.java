package io.github.bitandink.diet_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mealName;
    private Integer calories;
    private Integer protein;
    private Integer carbohydrate;
    private Integer fat;

    protected Meal() {
        // JPA가 Entity 객체를 생성할 때 사용하는 기본 생성자
    }

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

    public Long getId() {
        return id;
    }

    public String getMealName() {
        return mealName;
    }

    public Integer getCalories() {
        return calories;
    }

    public Integer getProtein() {
        return protein;
    }

    public Integer getCarbohydrate() {
        return carbohydrate;
    }

    public Integer getFat() {
        return fat;
    }
}