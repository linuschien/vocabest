package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.VocabularyWord;
import java.util.UUID;

public interface VocabularyWordRepository extends R2dbcRepository<VocabularyWord, UUID>, ReactiveQueryByExampleExecutor<VocabularyWord> {
}
