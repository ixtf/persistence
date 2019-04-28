package com.github.ixtf.persistence.reflection;

import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ToString(onlyExplicitlyIncluded = true)
class DefaultClassRepresentation<T> implements ClassRepresentation<T> {
    @Getter
    private final Class<T> entityClass;
    @Getter
    private final Constructor<T> constructor;
    @ToString.Include
    @Getter
    private final String tableName;
    @ToString.Include
    private final FieldRepresentation id;
    @ToString.Include
    @Getter
    private final List<FieldRepresentation> fields;

    DefaultClassRepresentation(String tableName, Class<T> entityClass, Constructor constructor, List<FieldRepresentation> fields) {
        this.tableName = tableName;
        this.entityClass = entityClass;
        this.fields = Collections.unmodifiableList(fields);
        this.constructor = constructor;
        this.id = fields.stream().filter(FieldRepresentation::isId).findFirst().orElse(null);
    }

    @Override
    public Optional<FieldRepresentation> getId() {
        return Optional.ofNullable(id);
    }

}
