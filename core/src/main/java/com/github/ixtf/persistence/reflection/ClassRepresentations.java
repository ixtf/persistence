package com.github.ixtf.persistence.reflection;

import com.github.ixtf.japp.core.J;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-14
 */
public final class ClassRepresentations {
    private static final LoadingCache<Class, ClassRepresentation> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public ClassRepresentation load(Class entityClass) throws Exception {
            final Constructor constructor = makeAccessible(entityClass);
            final String tableName = tableName(entityClass);

            final Predicate<Field> hasColumnAnnotation = f -> f.getAnnotation(Column.class) != null;
            final Predicate<Field> hasIdAnnotation = f -> f.getAnnotation(Id.class) != null;
            final Predicate<Field> fieldPredicate = hasColumnAnnotation.or(hasIdAnnotation);
            final List<FieldRepresentation> fields = FieldUtils.getAllFieldsList(entityClass).stream()
                    .filter(fieldPredicate).map(ClassRepresentations::to).collect(toList());

            return new DefaultClassRepresentation(tableName, entityClass, constructor, fields);
        }
    });

    public static <T> ClassRepresentation<T> create(T o) {
        return create((Class<T>) o.getClass());
    }

    @SneakyThrows
    public static <T> ClassRepresentation<T> create(Class<T> entityClass) {
        final String className = entityClass.getName();
        if (J.contains(className, "CGLIB")) {
            final String actualClassName = J.split(className, "$$")[0];
            entityClass = (Class<T>) Class.forName(actualClassName);
        }
        return cache.get(entityClass);
    }

    private static FieldRepresentation to(Field field) {
        final FieldType fieldType = FieldType.of(field);
        final boolean id = field.getAnnotation(Id.class) != null;
        final String columnName = id ? null : getColumnName(field);
        FieldRepresentationBuilder builder = FieldRepresentation.builder().withColName(columnName)
                .withField(field)
                .withType(fieldType)
                .withId(id);
        final Convert convert = field.getAnnotation(Convert.class);
        if (nonNull(convert)) {
            builder.withConverter(convert.converter());
        }
        switch (fieldType) {
            case COLLECTION:
            case MAP:
                return builder.buildGeneric();
            case EMBEDDABLE:
                return builder.withEntityName(tableName(field.getType())).buildEmedded();
            default:
                return builder.buildDefault();
        }
    }

    private static String getColumnName(Field field) {
        requireNonNull(field);
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(J::nonBlank)
                .orElseGet(field::getName);
    }

    private static String tableName(Class<?> clazz) {
        final Entity annotation = clazz.getAnnotation(Entity.class);
        if (annotation == null) {
            return null;
        }
        return Optional.ofNullable(annotation.name())
                .filter(J::nonBlank)
                .orElseGet(() -> {
                    final String simpleName = clazz.getSimpleName();
                    return "T_" + simpleName;
                });
    }

    private static Constructor makeAccessible(Class clazz) {
        final List<Constructor> constructors = Stream.of(clazz.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .collect(toList());
        if (constructors.isEmpty()) {
            throw new ConstructorException(clazz);
        }

        return constructors.stream()
                .filter(c -> Modifier.isPublic(c.getModifiers()))
                .findFirst()
                .orElseGet(() -> {
                    Constructor constructor = constructors.get(0);
                    constructor.setAccessible(true);
                    return constructor;
                });
    }
}
