package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository;
import com.vocabest.core.adapter.out.persistence.repository.UserAggregatedStats;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.HashMap;

@Controller
public class UserGraphQLResolver {

    private final UserRepository repository;
    private final DailyProgressRepository dailyProgressRepository;

    public UserGraphQLResolver(UserRepository repository, DailyProgressRepository dailyProgressRepository) {
        this.repository = repository;
        this.dailyProgressRepository = dailyProgressRepository;
    }

    @QueryMapping
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Flux<User> listUsers(@Argument UserFilterInput filter) {
        if (filter != null) {
            com.vocabest.core.adapter.out.persistence.model.TargetLevel tl = null;
            if (filter.targetLevel() != null) {
                tl = com.vocabest.core.adapter.out.persistence.model.TargetLevel.valueOf(filter.targetLevel());
            }
            com.vocabest.core.adapter.out.persistence.model.Role role = null;
            if (filter.role() != null) {
                role = com.vocabest.core.adapter.out.persistence.model.Role.valueOf(filter.role());
            }
            User probe = new User(filter.id(), filter.email(), role, tl, filter.learningStreak(), null, null, filter.dailyTargetQuestions(), null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        }
        return repository.findAll();
    }

    @BatchMapping(typeName = "User", field = "stats")
    public Mono<Map<User, UserStats>> stats(List<User> users) {
        List<UUID> userIds = users.stream().map(User::id).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return dailyProgressRepository.findAggregatedStatsByUserIds(userIds)
                .collectMap(UserAggregatedStats::userId)
                .map(statsMap -> {
                    Map<User, UserStats> result = new HashMap<>();
                    for (User user : users) {
                        UserAggregatedStats userStats = statsMap.get(user.id());
                        if (userStats != null) {
                            String accuracy = userStats.totalAnswered() > 0 
                                    ? String.format("%.1f%%", (double) userStats.totalCorrect() / userStats.totalAnswered() * 100) 
                                    : "0.0%";
                            result.put(user, new UserStats(userStats.totalAnswered(), userStats.totalCorrect(), accuracy));
                        } else {
                            result.put(user, new UserStats(0, 0, "0.0%"));
                        }
                    }
                    return result;
                });
    }

    public record UserStats(Integer totalQuestionsAnswered, Integer totalCorrectAnswers, String overallAccuracy) {}
}
