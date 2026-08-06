package io.github.bitandink.diet_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class MealRequest {

    @NotBlank(message = "필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "항목의 글자수가 1~50자여야 합니다.")
    private String mealName;

    @NotNull(message = "필수 입력 항목입니다.")
    @PositiveOrZero(message = "0 이상이어야 합니다.")
    private Integer calories;

    @NotNull(message = "필수 입력 항목입니다.")
    @PositiveOrZero(message = "0 이상이어야 합니다.")
    private Integer protein;

    @NotNull(message = "필수 입력 항목입니다.")
    @PositiveOrZero(message = "0 이상이어야 합니다.")
    private Integer carbohydrate;

    @NotNull(message = "필수 입력 항목입니다.")
    @PositiveOrZero(message = "0 이상이어야 합니다.")
    private Integer fat;

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getProtein() {
        return protein;
    }

    public void setProtein(Integer protein) {
        this.protein = protein;
    }

    public Integer getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(Integer carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public Integer getFat() {
        return fat;
    }

    public void setFat(Integer fat) {
        this.fat = fat;
    }
}
