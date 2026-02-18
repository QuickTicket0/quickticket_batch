package com.quickticket.quickticket.entity.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@RedisHash("sync:bulk-insert-queue:ticket-issue")
public class TicketBulkInsertQueueEntity {
    @Id
    private Long ticketIssueId;

    @Indexed
    @NotNull
    private Long userId;

    @NotNull
    private Long performanceId;

    @NotNull
    private TicketStatus status;

    private Long paymentMethodId;
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;
    private Long waitingNumber;
    private Integer personNumber;
    private List<Long> wantingSeatsId;
}
