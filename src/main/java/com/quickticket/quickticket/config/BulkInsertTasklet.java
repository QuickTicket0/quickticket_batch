package com.quickticket.quickticket.config;

import com.quickticket.quickticket.entity.ticket.TicketBulkInsertQueueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BulkInsertTasklet implements Tasklet {
    private static final int BATCH_SIZE = 1000;

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<String> keys = redisTemplate.opsForZSet().popMin("sync:bulk-insert-queue:ticket-issue", BATCH_SIZE)
                .parallelStream()
                .map(id -> "sync:ticket-issue:" + id.getValue())
                .toList();

        if (keys.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        List<Object> hashResults = redisTemplate.executePipelined(
            new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String key : keys) {
                        operations.opsForHash().entries((K) key);
                    }
                    return null;
                }
            }
        );

        var entities = hashResults.stream()
            .map(obj -> (Map<String, Object>) obj)
            .filter(map -> map != null && !map.isEmpty())
            .map(map -> objectMapper.convertValue(map, TicketBulkInsertQueueEntity.class))
            .toList();

        String sql = "INSERT INTO ticket_issue (ticket_issue_id, canceled_at, created_at, person_number, ticket_status, waiting_number, payment_method_id, performance_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
            sql,
            entities,
            BATCH_SIZE,
            (PreparedStatement ps, TicketBulkInsertQueueEntity entity) -> {
                ps.setLong(1, entity.getTicketIssueId());
                ps.setObject(2, entity.getCanceledAt());
                ps.setObject(3, entity.getCreatedAt());
                ps.setInt(4, entity.getPersonNumber());
                ps.setShort(5, (short) entity.getStatus().code);
                ps.setLong(6, entity.getWaitingNumber());
                ps.setLong(7, entity.getPaymentMethodId());
                ps.setLong(8, entity.getPerformanceId());
                ps.setLong(9, entity.getUserId());
            }
        );

        redisTemplate.delete(keys);

        return RepeatStatus.FINISHED;
    }
}

