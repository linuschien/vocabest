package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.ErrorEventFilterInput;
import com.vocabest.core.adapter.in.web.dto.ErrorEventPage;
import reactor.core.publisher.Mono;

public interface ErrorEventRepositoryCustom {
    Mono<ErrorEventPage> search(ErrorEventFilterInput filter);
}
