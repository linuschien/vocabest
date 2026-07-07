package com.vocabest.core.adapter.in.web.exception;

import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleGeneralException_withMessage() {
        Exception ex = new RuntimeException("Test error");
        Mono<ResponseEntity<Map<String, Object>>> resultMono = handler.handleGeneralException(ex);

        ResponseEntity<Map<String, Object>> response = resultMono.block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Test error", response.getBody().get("message"));
    }

    @Test
    void testHandleGeneralException_withoutMessage() {
        Exception ex = new RuntimeException();
        Mono<ResponseEntity<Map<String, Object>>> resultMono = handler.handleGeneralException(ex);

        ResponseEntity<Map<String, Object>> response = resultMono.block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Internal Server Error", response.getBody().get("message"));
    }

    @Test
    void testResolveToSingleError_withMessage() {
        Throwable ex = new RuntimeException("GraphQL Test Error");
        DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
        ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
        Field field = mock(Field.class);

        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("testField"));
        when(env.getField()).thenReturn(field);
        when(field.getSourceLocation()).thenReturn(new SourceLocation(1, 1));

        GraphQLError error = handler.resolveToSingleError(ex, env);
        assertNotNull(error);
        assertEquals("GraphQL Test Error", error.getMessage());
    }

    @Test
    void testResolveToSingleError_withoutMessage() {
        Throwable ex = new RuntimeException();
        DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
        ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
        Field field = mock(Field.class);

        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("testField"));
        when(env.getField()).thenReturn(field);
        when(field.getSourceLocation()).thenReturn(new SourceLocation(1, 1));

        GraphQLError error = handler.resolveToSingleError(ex, env);
        assertNotNull(error);
        assertEquals("GraphQL Execution Error", error.getMessage());
    }
}
