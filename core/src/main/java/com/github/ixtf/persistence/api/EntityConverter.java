package com.github.ixtf.persistence.api;

import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.persistence.AttributeConverter;

/**
 * @author jzb 2019-02-18
 */
public interface EntityConverter {
    LoadingCache<Class<? extends AttributeConverter>, AttributeConverter> CONVERTER_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public AttributeConverter load(Class<? extends AttributeConverter> converterClazz) throws Exception {
//            final Constructor constructor = makeAccessible(converterClazz);
            return converterClazz.getConstructor().newInstance();
        }
    });

    static AttributeConverter attributeConverter(FieldRepresentation fieldRepresentation) {
        return fieldRepresentation.getConverter()
                .map(it -> {
                    try {
                        return CONVERTER_CACHE.get(it);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }

    default <T> T toEntity(Class<T> entityClass, Object dbData) {
        final ClassRepresentation<T> classRepresentation = ClassRepresentations.create(entityClass);
        return toEntity(classRepresentation, dbData);
    }

    <T> T toEntity(ClassRepresentation<T> entityClass, Object dbData);

    <DB> DB toDbData(DB dbData, Object entityInstance);
}
