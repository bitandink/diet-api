package io.github.bitandink.diet_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 100자까지 입력 가능합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 ~ 100자까지 입력 가능합니다.")
        String password,

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        @Size(max = 50, message = "이름은 50자까지 입력 가능합니다.")
        String name,

        @Size(max = 50, message = "연락처는 50자까지 입력 가능합니다.")
        String phone

) {
}
