package com.quickticket.quickticket.entity.ticket;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Arrays;

@ReadingConverter
public class ShortToTicketStatusConverter implements Converter<Short, TicketStatus> {
    @Override
    public TicketStatus convert(Short source) {
        return Arrays.stream(TicketStatus.class.getEnumConstants())
                .filter(e -> e.getCode() == source)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown code: " + source));
    }
}
