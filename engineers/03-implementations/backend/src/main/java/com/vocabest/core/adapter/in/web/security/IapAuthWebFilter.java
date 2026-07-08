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
        String emailHeader = exchange.getRequest().getHeaders().getFirst("x-goog-authenticated-user-email");
        
        if ((emailHeader == null || emailHeader.isEmpty()) && mockUserEmail != null && !mockUserEmail.isEmpty()) {
            emailHeader = mockUserEmail;
        }

        if (emailHeader == null || emailHeader.isEmpty()) {
            return chain.filter(exchange);
        }

        String parsedEmail = emailHeader.replace("accounts.google.com:", "");
        return userRepository.findByEmail(parsedEmail)
                .flatMap(user -> chain.filter(exchange)
                        .contextWrite(Context.of("CURRENT_USER", user, "CURRENT_EMAIL", parsedEmail)))
                .switchIfEmpty(chain.filter(exchange)
                        .contextWrite(Context.of("CURRENT_EMAIL", parsedEmail)));
    }
}
