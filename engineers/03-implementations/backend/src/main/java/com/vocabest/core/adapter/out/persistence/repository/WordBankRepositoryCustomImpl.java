package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class WordBankRepositoryCustomImpl implements WordBankRepositoryCustom {

    private final DatabaseClient databaseClient;

    public WordBankRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<WordBank> search(WordBankFilterInput filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM word_bank WHERE 1=1 ");
        List<Object> bindings = new ArrayList<>();
        int index = 0;

        if (filter != null) {
            if (filter.word() != null && !filter.word().isBlank()) {
                sql.append("AND LOWER(word) LIKE $").append(++index).append(" ");
                bindings.add("%" + filter.word().toLowerCase() + "%");
            }
            if (filter.startingLetter() != null && !filter.startingLetter().isBlank()) {
                sql.append("AND LOWER(word) LIKE $").append(++index).append(" ");
                bindings.add(filter.startingLetter().toLowerCase() + "%");
            }
            if (filter.difficultyLevel() != null) {
                sql.append("AND difficulty_level = $").append(++index).append(" ");
                bindings.add(filter.difficultyLevel());
            }
            if (filter.targetLevel() != null) {
                sql.append("AND target_level = $").append(++index).append(" ");
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

        return spec.map((row, metadata) -> {
            String targetLevelStr = row.get("target_level", String.class);
            TargetLevel targetLevel = targetLevelStr != null ? TargetLevel.valueOf(targetLevelStr) : null;
            return new WordBank(
                    row.get("id", UUID.class),
                    row.get("word", String.class),
                    row.get("parts_of_speech", String.class),
                    row.get("chinese_translation", String.class),
                    targetLevel,
                    row.get("difficulty_level", Integer.class),
                    row.get("exam_frequency", Integer.class),
                    row.get("created_at", LocalDateTime.class),
                    row.get("updated_at", LocalDateTime.class),
                    row.get("deleted_at", LocalDateTime.class)
            );
        }).all();
    }
}
