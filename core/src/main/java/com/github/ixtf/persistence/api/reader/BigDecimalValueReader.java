package com.github.ixtf.persistence.api.reader;

import com.github.ixtf.persistence.api.ValueReader;

import java.math.BigDecimal;

/**
 * @author jzb 2019-02-15
 */
public final class BigDecimalValueReader implements ValueReader {
    @Override
    public <T> boolean isCompatible(Class<T> clazz) {
        return BigDecimal.class.equals(clazz);
    }

    @Override
    public <T> T read(Class<T> clazz, Object value) {
        if (BigDecimal.class.isInstance(value)) {
            return (T) value;
        }
        if (Number.class.isInstance(value)) {
            return (T) BigDecimal.valueOf(Number.class.cast(value).doubleValue());
        } else {
            return (T) BigDecimal.valueOf(Double.valueOf(value.toString()));
        }
    }
}
