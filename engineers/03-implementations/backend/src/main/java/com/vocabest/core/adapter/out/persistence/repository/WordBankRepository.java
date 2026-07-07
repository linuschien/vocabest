package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import java.util.UUID;

public interface WordBankRepository extends R2dbcRepository<WordBank, UUID>, WordBankRepositoryCustom {
}
