package org.example.match.application;

import jakarta.validation.constraints.NotBlank;

public record MatchCreateDTO(
        @NotBlank(message = "Вкажіть команди")
        String teams,

        @NotBlank(message = "Вкажіть дату та час")
        String matchDate
) {}