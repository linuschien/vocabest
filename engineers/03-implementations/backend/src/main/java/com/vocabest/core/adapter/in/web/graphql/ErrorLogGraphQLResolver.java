package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.ErrorLogFilterInput;
import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.application.port.in.ErrorLogQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class ErrorLogGraphQLResolver {

    private final ErrorLogQueryService queryService;

    public ErrorLogGraphQLResolver(ErrorLogQueryService queryService) {
        this.queryService = queryService;
    }

    @QueryMapping
    public Flux<ErrorLogResponse> listErrorLogs(@Argument ErrorLogFilterInput filter) {
        return queryService.listErrorLogs(filter);
    }
}
