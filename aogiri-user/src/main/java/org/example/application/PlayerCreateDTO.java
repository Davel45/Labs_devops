package org.example.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerCreateDTO(
        @NotBlank(message = "Нікнейм не може бути порожнім")
        @Size(min = 3, max = 20, message = "Нікнейм має бути від 3 до 20 символів")
        String nickname
) {}