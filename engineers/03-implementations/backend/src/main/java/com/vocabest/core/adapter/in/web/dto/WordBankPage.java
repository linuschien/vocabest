package com.vocabest.core.adapter.in.web.dto;

import com.vocabest.core.adapter.out.persistence.model.WordBank;
import java.util.List;

public record WordBankPage(
    List<WordBank> content,
    Long totalElements
) {}
