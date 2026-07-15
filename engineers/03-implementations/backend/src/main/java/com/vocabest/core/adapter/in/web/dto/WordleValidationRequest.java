package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WordleValidationRequest(
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{5}$")
    String guess
) {}
