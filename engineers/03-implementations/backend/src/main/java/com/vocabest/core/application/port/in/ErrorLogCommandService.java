package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.ErrorLogRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.adapter.in.web.dto.ErrorLogActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ErrorLogCommandService {
    Mono<ErrorLogResponse> createErrorLog(ErrorLogRequest req);
    Mono<ErrorLogResponse> updateErrorLog(UUID id, ErrorLogRequest req);
    Mono<Void> deleteErrorLog(UUID id);
    Mono<OperationStatus> recordFailure(UUID id, ErrorLogActionRequest req);
}
