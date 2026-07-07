package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import java.util.UUID;

public interface ErrorEventRepository extends R2dbcRepository<ErrorEvent, UUID> {
}
