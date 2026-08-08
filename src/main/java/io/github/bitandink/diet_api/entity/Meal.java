package io.github.bitandink.diet_api.entity;

import jakarta.persistence.Column;
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

    /*
     * MealRequest에서는
     *
     * @NotBlank
     * @Size(max = 50)
     *
     * 로 검증하고 있다.
     *
     * 따라서 DB 컬럼도:
     *
     * null 불가
     * 최대 길이 50
     *
     * 로 맞춰준다.
     */
    @Column(nullable = false, length = 50)
    private String mealName;

    /*
     * 영양 정보들은 MealRequest에서 @NotNull이므로
     * DB에서도 null을 허용하지 않도록 맞춘다.
     */
    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Integer protein;

    @Column(nullable = false)
    private Integer carbohydrate;

    @Column(nullable = false)
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