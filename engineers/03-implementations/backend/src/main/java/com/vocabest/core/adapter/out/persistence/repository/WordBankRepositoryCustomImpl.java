package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import com.vocabest.core.adapter.in.web.dto.WordBankPage;
import reactor.core.publisher.Mono;

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
    public Mono<WordBankPage> search(WordBankFilterInput filter) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> bindings = new ArrayList<>();
        int index = 0;

        if (filter != null) {
            if (filter.word() != null && !filter.word().isBlank()) {
                whereClause.append("AND LOWER(word) LIKE $").append(++index).append(" ");
                bindings.add("%" + filter.word().toLowerCase() + "%");
            }
            if (filter.startingLetter() != null && !filter.startingLetter().isBlank()) {
                whereClause.append("AND LOWER(word) LIKE $").append(++index).append(" ");
                bindings.add(filter.startingLetter().toLowerCase() + "%");
            }
            if (filter.difficultyLevel() != null) {
                whereClause.append("AND difficulty_level = $").append(++index).append(" ");
                bindings.add(filter.difficultyLevel());
            }
            if (filter.targetLevel() != null) {
                whereClause.append("AND target_level = $").append(++index).append(" ");
                bindings.add(filter.targetLevel());
            }
        }

        // Count query
        String countSql = "SELECT COUNT(*) FROM word_bank" + whereClause.toString();
        DatabaseClient.GenericExecuteSpec countSpec = databaseClient.sql(countSql);
        for (int i = 0; i < bindings.size(); i++) {
            countSpec = countSpec.bind(i, bindings.get(i));
        }
        Mono<Long> totalElementsMono = countSpec.map((row, metadata) -> row.get(0, Long.class)).one().defaultIfEmpty(0L);

        // Data query
        int limit = filter != null && filter.size() != null ? filter.size() : 20;
        int offset = filter != null && filter.page() != null ? filter.page() * limit : 0;

        String dataSql = "SELECT * FROM word_bank" + whereClause.toString() + 
                         " ORDER BY word ASC LIMIT $" + (++index) + " OFFSET $" + (++index);
        bindings.add(limit);
        bindings.add(offset);

        DatabaseClient.GenericExecuteSpec dataSpec = databaseClient.sql(dataSql);
        for (int i = 0; i < bindings.size(); i++) {
            dataSpec = dataSpec.bind(i, bindings.get(i));
        }

        Mono<List<WordBank>> contentMono = dataSpec.map((row, metadata) -> {
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
        }).all().collectList();

        return Mono.zip(contentMono, totalElementsMono)
                .map(tuple -> new WordBankPage(tuple.getT1(), tuple.getT2()));
    }
}
