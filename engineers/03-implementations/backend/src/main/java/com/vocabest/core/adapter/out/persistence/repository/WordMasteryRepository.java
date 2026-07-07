package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import java.util.UUID;

public interface WordMasteryRepository extends R2dbcRepository<WordMastery, UUID> {
}
