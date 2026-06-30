package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.adapter.in.web.dto.ErrorLogFilterInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ErrorLogQueryService {
    Mono<ErrorLogResponse> getErrorLogById(UUID id);
    Flux<ErrorLogResponse> listErrorLogs(ErrorLogFilterInput filter);
}
