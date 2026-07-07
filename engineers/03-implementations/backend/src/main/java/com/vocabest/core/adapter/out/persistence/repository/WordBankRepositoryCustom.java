package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import reactor.core.publisher.Flux;

public interface WordBankRepositoryCustom {
    Flux<WordBank> search(WordBankFilterInput filter);
}
