package com.vocabest.core.adapter.in.web.security;

import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
public class IapAuthWebFilter implements WebFilter {

    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${vocabest.security.mock-user-email:}")
    private String mockUserEmail;

    public IapAuthWebFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS ||
            !(path.startsWith("/api/") || path.startsWith("/graphql") || path.startsWith("/graphiql"))) {
            return chain.filter(exchange);
        }

        String emailHeader = exchange.getRequest().getHeaders().getFirst("x-goog-authenticated-user-email");
        
        if ((emailHeader == null || emailHeader.isEmpty()) && mockUserEmail != null && !mockUserEmail.isEmpty()) {
            emailHeader = mockUserEmail;
        }

        if (emailHeader == null || emailHeader.isEmpty()) {
            return chain.filter(exchange);
        }

        String parsedEmail = emailHeader.replace("accounts.google.com:", "");
        return userRepository.findByEmail(parsedEmail)
                .map(user -> Context.of("CURRENT_USER", (Object) user, "CURRENT_EMAIL", (Object) parsedEmail))
                .defaultIfEmpty(Context.of("CURRENT_EMAIL", (Object) parsedEmail))
                .flatMap(context -> chain.filter(exchange).contextWrite(context));
    }
}
