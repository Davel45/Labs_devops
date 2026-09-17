package org.example.tournament.application;

import jakarta.validation.constraints.NotBlank;

public record TournamentCreateDTO(
        @NotBlank(message = "Назва турніру не може бути порожньою")
        String name
) {}