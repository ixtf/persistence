package com.github.ixtf.persistence.reflection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author jzb 2019-02-14
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
abstract class AbstractFieldRepresentation implements FieldRepresentation {
    @EqualsAndHashCode.Include
    @ToString.Include
    @Getter
    protected final FieldType type;
    @EqualsAndHashCode.Include
    @ToString.Include
    @Getter
    protected final Field nativeField;
    @EqualsAndHashCode.Include
    @ToString.Include
    @Getter
    protected final String colName;
    @Getter
    protected final String fieldName;
    @Getter
    private final Class<?> fieldType;
    @ToString.Include
    protected final Class<? extends AttributeConverter> converter;

    @SneakyThrows
    AbstractFieldRepresentation(FieldType type, String colName, Field nativeField, Class<? extends AttributeConverter> converter) {
        this.type = type;
        this.colName = colName;
        this.nativeField = nativeField;
        this.fieldName = nativeField.getName();
        this.fieldType = nativeField.getType();
        this.converter = converter;
    }

    @Override
    public <T extends AttributeConverter> Optional<Class<? extends AttributeConverter>> getConverter() {
        return Optional.ofNullable(converter);
    }

}
