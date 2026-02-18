package com.quickticket.quickticket.entity.ticket;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "WANTING_SEATS")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WantingSeatsEntity {
    private Long ticketIssueId;

    private Long seatId;

    private Long performanceId;
}
