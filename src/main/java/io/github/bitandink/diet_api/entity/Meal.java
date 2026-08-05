package io.github.bitandink.diet_api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Meal {

    @Id
    @GeneratedValue
    private Long id;

    private String mealName;

    private Integer calories;

    private Integer protein;

    private Integer carbohydrate;

    private Integer fat;

}
