package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.ErrorEventFilterInput;
import com.vocabest.core.adapter.in.web.dto.ErrorEventPage;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ErrorEventRepositoryCustomImpl implements ErrorEventRepositoryCustom {

    private final DatabaseClient databaseClient;

    public ErrorEventRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    private LocalDateTime parseDate(String dateStr, String defaultTime) {
        if (dateStr.contains("T")) {
            return java.time.ZonedDateTime.parse(dateStr).withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime();
        } else {
            return LocalDateTime.parse(dateStr + defaultTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    @Override
    public Mono<ErrorEventPage> search(ErrorEventFilterInput filter) {
        StringBuilder query = new StringBuilder("SELECT * FROM error_event WHERE 1=1");
        StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM error_event WHERE 1=1");
        List<Object> bindings = new ArrayList<>();

        if (filter.userId() != null) {
            bindings.add(filter.userId());
            String condition = " AND user_id = $" + bindings.size();
            query.append(condition);
            countQuery.append(condition);
        }

        if (filter.startDate() != null) {
            LocalDateTime start = parseDate(filter.startDate(), "T00:00:00");
            bindings.add(start);
            String condition = " AND timestamp >= $" + bindings.size();
            query.append(condition);
            countQuery.append(condition);
        }

        if (filter.endDate() != null) {
            LocalDateTime end = parseDate(filter.endDate(), "T23:59:59");
            bindings.add(end);
            String condition = " AND timestamp <= $" + bindings.size();
            query.append(condition);
            countQuery.append(condition);
        }

        query.append(" ORDER BY timestamp DESC");

        int page = filter.page() != null ? filter.page() : 0;
        int size = filter.size() != null ? filter.size() : 10;
        int offset = page * size;
        
        query.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query.toString());
        DatabaseClient.GenericExecuteSpec countSpec = databaseClient.sql(countQuery.toString());

        for (int i = 0; i < bindings.size(); i++) {
            spec = spec.bind(i, bindings.get(i));
            countSpec = countSpec.bind(i, bindings.get(i));
        }

        Mono<List<ErrorEvent>> itemsMono = spec.map((row, rowMetadata) -> new ErrorEvent(
                row.get("id", UUID.class),
                row.get("user_id", UUID.class),
                row.get("quiz_question_id", UUID.class),
                row.get("timestamp", LocalDateTime.class),
                row.get("selected_distractor", String.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                row.get("deleted_at", LocalDateTime.class)
        )).all().collectList();

        Mono<Integer> countMono = countSpec.map((row, rowMetadata) -> {
            Number count = row.get(0, Number.class);
            return count != null ? count.intValue() : 0;
        }).first().defaultIfEmpty(0);

        return Mono.zip(itemsMono, countMono)
                .map(tuple -> new ErrorEventPage(tuple.getT1(), tuple.getT2()));
    }
}
