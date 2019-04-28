package com.github.ixtf.persistence.api.reader;

import com.github.ixtf.persistence.api.ValueReader;

import java.math.BigInteger;

/**
 * @author jzb 2019-02-15
 */
public final class BigIntegerValueReader implements ValueReader {

    @Override
    public <T> boolean isCompatible(Class<T> clazz) {
        return BigInteger.class.equals(clazz);
    }

    @Override
    public <T> T read(Class<T> clazz, Object value) {
        if (BigInteger.class.isInstance(value)) {
            return (T) value;
        }
        if (Number.class.isInstance(value)) {
            return (T) BigInteger.valueOf(Number.class.cast(value).longValue());
        } else {
            return (T) BigInteger.valueOf(Long.valueOf(value.toString()));
        }
    }
}
