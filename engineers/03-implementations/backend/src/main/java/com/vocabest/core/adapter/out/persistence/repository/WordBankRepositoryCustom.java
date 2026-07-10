package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.in.web.dto.WordBankPage;
import reactor.core.publisher.Mono;

public interface WordBankRepositoryCustom {
    Mono<WordBankPage> search(WordBankFilterInput filter);
}
