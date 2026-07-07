package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record SubmitAnswerRequest(
    UUID questionId,
    String selectedDistractor
) {}
