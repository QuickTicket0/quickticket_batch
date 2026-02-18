package com.quickticket.quickticket.entity.ticket;

import com.quickticket.quickticket.shared.converters.OrdinalEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TicketStatus implements OrdinalEnum {
    WAITING(0),
    PRESET(1),
    CANCELED(2),
    SEAT_ALLOCATED_ALL(3),
    SEAT_ALLOCATED_PARTIAL(4);

    public final int code;
}
