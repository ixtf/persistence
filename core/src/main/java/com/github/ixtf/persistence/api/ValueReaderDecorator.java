package com.github.ixtf.persistence.api;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceLoader;

public final class ValueReaderDecorator implements ValueReader {
    private static final ValueReaderDecorator INSTANCE = new ValueReaderDecorator();
    private final List<ValueReader> readers = Lists.newArrayList();

    {
        ServiceLoader.load(ValueReader.class).forEach(readers::add);
    }

    public static ValueReaderDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isCompatible(Class clazz) {
        return readers.stream().anyMatch(r -> r.isCompatible(clazz));
    }

    @Override
    public <T> T read(Class<T> clazz, Object value) {
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        ValueReader valueReader = readers.stream().filter(r -> r.isCompatible(clazz)).findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("类型[" + clazz + "]不支持"));
        return valueReader.read(clazz, value);
    }

}