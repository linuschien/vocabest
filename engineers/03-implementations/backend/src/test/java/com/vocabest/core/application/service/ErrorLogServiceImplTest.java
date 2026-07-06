package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.ErrorLogRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorLogActionRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorLogFilterInput;
import com.vocabest.core.adapter.out.persistence.model.ErrorLog;
import com.vocabest.core.adapter.out.persistence.repository.ErrorLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorLogServiceImplTest {

    @Mock
    private ErrorLogRepository repository;

    @InjectMocks
    private ErrorLogServiceImpl service;

    @Test
    void testCreateErrorLog() {
        UUID wordId = UUID.randomUUID();
        ErrorLogRequest req = new ErrorLogRequest(wordId, 1, LocalDateTime.now());
        ErrorLog entity = new ErrorLog(UUID.randomUUID(), UUID.randomUUID(), wordId, null, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.createErrorLog(req))
                .expectNextMatches(res -> res.id() != null && res.wordId().equals(wordId))
                .verifyComplete();
    }

    @Test
    void testGetErrorLogById() {
        UUID id = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        ErrorLog entity = new ErrorLog(id, UUID.randomUUID(), wordId, null, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(service.getErrorLogById(id))
                .expectNextMatches(res -> res.id().equals(id) && res.wordId().equals(wordId))
                .verifyComplete();
    }

    @Test
    void testUpdateErrorLog() {
        UUID id = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        ErrorLogRequest req = new ErrorLogRequest(wordId, 2, LocalDateTime.now());
        ErrorLog entity = new ErrorLog(id, UUID.randomUUID(), wordId, null, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.updateErrorLog(id, req))
                .expectNextMatches(res -> res.id().equals(id))
                .verifyComplete();
    }

    @Test
    void testDeleteErrorLog() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteErrorLog(id))
                .verifyComplete();
    }

    @Test
    void testListErrorLogs() {
        StepVerifier.create(service.listErrorLogs(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testListErrorLogsWithFilter() {
        ErrorLog entity = new ErrorLog(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.domain.Example<ErrorLog>>any())).thenReturn(Flux.just(entity));

        StepVerifier.create(service.listErrorLogs(new ErrorLogFilterInput(entity.userId())))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testRecordFailure() {
        UUID id = UUID.randomUUID();
        ErrorLog entity = new ErrorLog(id, UUID.randomUUID(), UUID.randomUUID(), null, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(entity));
        when(repository.save(any())).thenReturn(Mono.just(new ErrorLog(id, entity.userId(), entity.vocabularyWordId(), entity.quizQuestionId(), 2, LocalDateTime.now().plusDays(2), entity.createdAt(), LocalDateTime.now(), entity.deletedAt())));

        StepVerifier.create(service.recordFailure(id, new ErrorLogActionRequest("reason")))
                .expectNextMatches(res -> res.success() && res.message().contains("New weight: 2"))
                .verifyComplete();
    }
}
