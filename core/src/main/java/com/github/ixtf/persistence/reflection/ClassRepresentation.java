package com.github.ixtf.persistence.reflection;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

/**
 * 类表示 {@link Class} cached
 *
 * @author jzb 2019-02-14
 */
public interface ClassRepresentation<T> {

    String getTableName();

    Class<T> getEntityClass();

    Constructor<T> getConstructor();

    Optional<FieldRepresentation> getId();

    default boolean hasId() {
        return getId().isPresent();
    }

    List<FieldRepresentation> getFields();

}