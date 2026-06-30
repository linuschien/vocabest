package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.ErrorLogRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.adapter.in.web.dto.ErrorLogActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.ErrorLogCommandService;
import com.vocabest.core.application.port.in.ErrorLogQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/error-logs")
public class ErrorLogRestController {

    private final ErrorLogCommandService commandService;
    private final ErrorLogQueryService queryService;

    public ErrorLogRestController(ErrorLogCommandService commandService, ErrorLogQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public Mono<ResponseEntity<ErrorLogResponse>> createErrorLog(@PathVariable UUID userId, @RequestBody ErrorLogRequest req) {
        // userId should ideally be passed in req or service method, simplify for now
        return commandService.createErrorLog(req)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ErrorLogResponse>> getErrorLogById(@PathVariable UUID userId, @PathVariable UUID id) {
        return queryService.getErrorLogById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ErrorLogResponse>> updateErrorLog(@PathVariable UUID userId, @PathVariable UUID id, @RequestBody ErrorLogRequest req) {
        return commandService.updateErrorLog(id, req)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteErrorLog(@PathVariable UUID userId, @PathVariable UUID id) {
        return commandService.deleteErrorLog(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/{id}:recordFailure")
    public Mono<ResponseEntity<OperationStatus>> recordFailure(@PathVariable UUID userId, @PathVariable UUID id, @RequestBody ErrorLogActionRequest req) {
        return commandService.recordFailure(id, req)
                .map(ResponseEntity::ok);
    }
}
