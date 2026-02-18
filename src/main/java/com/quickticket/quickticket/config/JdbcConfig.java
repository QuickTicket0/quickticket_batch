package com.quickticket.quickticket.config;

import com.quickticket.quickticket.entity.ticket.ShortToTicketStatusConverter;
import com.quickticket.quickticket.shared.converters.OrdinalEnumToShortConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.List;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                new ShortToTicketStatusConverter(),
                new OrdinalEnumToShortConverter()
        ));
    }
}
