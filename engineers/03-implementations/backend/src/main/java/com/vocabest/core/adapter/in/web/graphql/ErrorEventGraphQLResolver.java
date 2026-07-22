package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.ErrorEventFilterInput;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import com.vocabest.core.adapter.out.persistence.repository.ErrorEventRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import reactor.core.publisher.Mono;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import com.vocabest.core.adapter.in.web.dto.ErrorEventPage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.UUID;

@Controller
public class ErrorEventGraphQLResolver {

    private final ErrorEventRepository repository;
    private final QuizQuestionRepository quizQuestionRepository;

    public ErrorEventGraphQLResolver(ErrorEventRepository repository, QuizQuestionRepository quizQuestionRepository) {
        this.repository = repository;
        this.quizQuestionRepository = quizQuestionRepository;
    }

    @QueryMapping
    @com.vocabest.core.adapter.in.web.security.RequireOwnership("#filter?.userId()")
    public Mono<ErrorEventPage> listErrorEvents(@Argument ErrorEventFilterInput filter) {
        return repository.search(filter);
    }

    @SchemaMapping(typeName = "ErrorEvent", field = "timestamp")
    public java.time.OffsetDateTime timestamp(ErrorEvent event) {
        if (event.timestamp() == null) return null;
        return event.timestamp().atOffset(java.time.ZoneOffset.UTC);
    }

    @BatchMapping
    public Mono<Map<ErrorEvent, QuizQuestion>> quizQuestion(List<ErrorEvent> events) {
        Set<UUID> quizIds = events.stream()
                .map(ErrorEvent::quizQuestionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        return quizQuestionRepository.findAllById(quizIds)
                .collectMap(QuizQuestion::id)
                .map(quizMap -> {
                    Map<ErrorEvent, QuizQuestion> result = new HashMap<>();
                    for (ErrorEvent event : events) {
                        if (event.quizQuestionId() != null) {
                            result.put(event, quizMap.get(event.quizQuestionId()));
                        }
                    }
                    return result;
                });
    }
}
