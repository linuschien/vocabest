package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import java.util.UUID;

public interface DailyProgressRepository extends R2dbcRepository<DailyProgress, UUID> {
}
