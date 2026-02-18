package com.quickticket.quickticket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.quickticket.quickticket.entity.ticket.TicketBulkInsertQueueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BulkInsertTasklet implements Tasklet {
    private static final int BATCH_SIZE = 1000;

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectReader reader = new ObjectMapper().readerFor(TicketBulkInsertQueueEntity.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<String> rawData = redisTemplate.opsForList().leftPop("sync:bulk-insert-queue:ticket-issue", BATCH_SIZE);

        if (rawData == null || rawData.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        List<TicketBulkInsertQueueEntity> entities = rawData.parallelStream()
                .map(json -> {
                    try {
                        return (TicketBulkInsertQueueEntity) reader.readValue(json);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        String sql = "INSERT INTO TICKET_ISSUE (ticket_issue_id, canceled_at, created_at, person_number, ticket_status, waiting_number, payment_method_id, performance_id, user_id) " +
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

        return RepeatStatus.FINISHED;
    }
}

