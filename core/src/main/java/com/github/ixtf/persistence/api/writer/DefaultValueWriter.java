package com.github.ixtf.persistence.api.writer;

import com.github.ixtf.persistence.api.ValueWriter;

import java.math.BigDecimal;

/**
 * @author jzb 2019-02-15
 */
public class DefaultValueWriter implements ValueWriter<Object, Object> {

    @Override
    public <T> boolean isCompatible(Class<T> clazz) {
        return String.class.isAssignableFrom(clazz)
                || BigDecimal.class.isAssignableFrom(clazz)
                || Integer.class.isAssignableFrom(clazz)
                || Long.class.isAssignableFrom(clazz);
    }

    @Override
    public Object write(Object object) {
        return object;
    }
}
