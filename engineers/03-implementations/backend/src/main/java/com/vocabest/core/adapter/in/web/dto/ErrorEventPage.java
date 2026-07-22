package com.vocabest.core.adapter.in.web.dto;

import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import java.util.List;

public record ErrorEventPage(
    List<ErrorEvent> content,
    int totalElements
) {}
