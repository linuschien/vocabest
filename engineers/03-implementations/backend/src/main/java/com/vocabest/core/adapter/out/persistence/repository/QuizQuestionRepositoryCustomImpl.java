package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class QuizQuestionRepositoryCustomImpl implements QuizQuestionRepositoryCustom {

    private final DatabaseClient databaseClient;

    public QuizQuestionRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<QuizQuestion> search(QuizQuestionFilterInput filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT q.* FROM quiz_question q " +
            "JOIN word_bank w ON q.word_bank_id = w.id " +
            "WHERE 1=1 "
        );
        List<Object> bindings = new ArrayList<>();
        int index = 0;

        if (filter != null) {
            if (filter.word() != null && !filter.word().isBlank()) {
                sql.append("AND LOWER(w.word) LIKE $").append(++index).append(" ");
                bindings.add("%" + filter.word().toLowerCase() + "%");
            }
            if (filter.startingLetter() != null && !filter.startingLetter().isBlank()) {
                sql.append("AND LOWER(w.word) LIKE $").append(++index).append(" ");
                bindings.add(filter.startingLetter().toLowerCase() + "%");
            }
            if (filter.difficultyLevel() != null) {
                sql.append("AND w.difficulty_level = $").append(++index).append(" ");
                bindings.add(filter.difficultyLevel());
            }
            if (filter.targetLevel() != null) {
                sql.append("AND w.target_level = $").append(++index).append(" ");
                bindings.add(filter.targetLevel());
            }
        }

        int limit = filter != null && filter.size() != null ? filter.size() : 20;
        int offset = filter != null && filter.page() != null ? filter.page() * limit : 0;

        sql.append("LIMIT $").append(++index).append(" OFFSET $").append(++index);
        bindings.add(limit);
        bindings.add(offset);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        for (int i = 0; i < bindings.size(); i++) {
            spec = spec.bind(i, bindings.get(i));
        }

        return spec.map((row, metadata) -> new QuizQuestion(
                row.get("id", UUID.class),
                row.get("word_bank_id", UUID.class),
                row.get("contextual_cloze", String.class),
                row.get("chinese_translation", String.class),
                row.get("correct_answer", String.class),
                row.get("distractor1", String.class),
                row.get("distractor2", String.class),
                row.get("distractor3", String.class),
                row.get("explanation_root_affix", String.class),
                row.get("explanation_mnemonic", String.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                row.get("deleted_at", LocalDateTime.class)
        )).all();
    }

    @Override
    public reactor.core.publisher.Mono<Long> count(QuizQuestionFilterInput filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(q.id) FROM quiz_question q " +
            "JOIN word_bank w ON q.word_bank_id = w.id " +
            "WHERE 1=1 "
        );
        List<Object> bindings = new ArrayList<>();
        int index = 0;

        if (filter != null) {
            if (filter.word() != null && !filter.word().isBlank()) {
                sql.append("AND LOWER(w.word) LIKE $").append(++index).append(" ");
                bindings.add("%" + filter.word().toLowerCase() + "%");
            }
            if (filter.startingLetter() != null && !filter.startingLetter().isBlank()) {
                sql.append("AND LOWER(w.word) LIKE $").append(++index).append(" ");
                bindings.add(filter.startingLetter().toLowerCase() + "%");
            }
            if (filter.difficultyLevel() != null) {
                sql.append("AND w.difficulty_level = $").append(++index).append(" ");
                bindings.add(filter.difficultyLevel());
            }
            if (filter.targetLevel() != null) {
                sql.append("AND w.target_level = $").append(++index).append(" ");
                bindings.add(filter.targetLevel());
            }
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        for (int i = 0; i < bindings.size(); i++) {
            spec = spec.bind(i, bindings.get(i));
        }

        return spec.map((row, metadata) -> row.get(0, Long.class)).one();
    }
}
