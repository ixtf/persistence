package com.github.ixtf.persistence.reflection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Field;

/**
 * @author jzb 2019-02-14
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DefaultFieldRepresentation extends AbstractFieldRepresentation {
    @Getter
    private final boolean id;

    DefaultFieldRepresentation(FieldType type, Field field, String name, Class<? extends AttributeConverter> converter, boolean id) {
        super(type, name, field, converter);
        this.id = id;
    }
}
