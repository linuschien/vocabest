package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.UUID;

@Controller
@com.vocabest.core.adapter.in.web.security.AdminOnly
public class QuizQuestionGraphQLResolver {

    private final QuizQuestionRepository repository;
    private final WordBankRepository wordBankRepository;

    public QuizQuestionGraphQLResolver(QuizQuestionRepository repository, WordBankRepository wordBankRepository) {
        this.repository = repository;
        this.wordBankRepository = wordBankRepository;
    }

    @QueryMapping
    public Flux<QuizQuestion> listQuizQuestions(@Argument QuizQuestionFilterInput filter) {
        return repository.search(filter);
    }

    @BatchMapping
    public Mono<Map<QuizQuestion, WordBank>> wordBank(List<QuizQuestion> questions) {
        Set<UUID> wordBankIds = questions.stream()
                .map(QuizQuestion::wordBankId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        return wordBankRepository.findAllById(wordBankIds)
                .collectMap(WordBank::id)
                .map(wbMap -> {
                    Map<QuizQuestion, WordBank> result = new HashMap<>();
                    for (QuizQuestion q : questions) {
                        if (q.wordBankId() != null) {
                            result.put(q, wbMap.get(q.wordBankId()));
                        }
                    }
                    return result;
                });
    }
}
