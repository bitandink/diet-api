package io.github.bitandink.diet_api.dto;

public class MealRequest {

    private String mealName;
    private Integer calories;
    private Integer protein;
    private Integer carbohydrate;
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
