package com.quickticket.quickticket.shared.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class OrdinalEnumToShortConverter implements Converter<OrdinalEnum, Short> {
    @Override
    public Short convert(OrdinalEnum source) {
        return (short) source.getCode();
    }
}
