package io.github.bitandink.diet_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealRequest {
    @NotBlank(message = "식단 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "식단 이름은 50자를 넘을 수 없습니다.")
    private String mealName;

    @NotNull(message = "칼로리는 필수 입력 항목입니다.")
    @PositiveOrZero(message = "칼로리는 0 이상이어야 합니다.")
    private Integer calories;

    @NotNull(message = "단백질은 필수 입력 항목입니다.")
    @PositiveOrZero(message = "단백질은 0 이상이어야 합니다.")
    private Integer protein;

    @NotNull(message = "탄수화물은 필수 입력 항목입니다.")
    @PositiveOrZero(message = "탄수화물은 0 이상이어야 합니다.")
    private Integer carbohydrate;

    @NotNull(message = "지방은 필수 입력 항목입니다.")
    @PositiveOrZero(message = "지방은 0 이상이어야 합니다.")
    private Integer fat;
}
