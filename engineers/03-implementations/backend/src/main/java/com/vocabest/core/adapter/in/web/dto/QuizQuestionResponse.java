package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuizQuestionResponse(
    UUID id,
    String wordBankId,
    String contextualCloze,
    String chineseTranslation,
    String correctAnswer,
    String distractor1,
    String distractor2,
    String distractor3,
    String explanationRootAffix,
    String explanationMnemonic
) {}
