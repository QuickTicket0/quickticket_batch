package com.quickticket.quickticket.entity.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "TICKET_ISSUE")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketIssueEntity {
    @Id
    private Long ticketIssueId;

    @NotNull
    private Long userId;

    @NotNull
    private Long performanceId;

    @NotNull
    private TicketStatus ticketStatus;

    private Long paymentMethodId;

    private LocalDateTime createdAt;

    private LocalDateTime canceledAt;

    private Long waitingNumber;

    private Integer personNumber;
}
