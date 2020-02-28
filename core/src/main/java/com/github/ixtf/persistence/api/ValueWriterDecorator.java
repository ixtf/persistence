package com.github.ixtf.persistence.api;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author jzb 2019-02-15
 */
public final class ValueWriterDecorator implements ValueWriter {
    private static final ValueWriterDecorator INSTANCE = new ValueWriterDecorator();
    private final List<ValueWriter> writers = Lists.newArrayList();

    {
        ServiceLoader.load(ValueWriter.class).forEach(writers::add);
    }

    private ValueWriterDecorator() {
    }

    public static ValueWriterDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isCompatible(Class clazz) {
        return writers.stream().anyMatch(writerField -> writerField.isCompatible(clazz));
    }

    @Override
    public Object write(Object object) {
        if (object == null) {
            return null;
        }
        Class clazz = object.getClass();
        ValueWriter valueWriter = writers.stream().filter(r -> r.isCompatible(clazz)).findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("类型[" + clazz + "]不支持"));
        return valueWriter.write(object);
    }

}
