package com.github.ixtf.persistence.api;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.IEntity;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation;

/**
 * @author jzb 2019-02-18
 */
public abstract class AbstractUnitOfWork implements UnitOfWork {
    private static final LoadingCache<Class<? extends IEntity>, Multimap<Class<? extends Annotation>, EntityCallback>> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public Multimap<Class<? extends Annotation>, EntityCallback> load(Class<? extends IEntity> entityClass) throws Exception {
            final Multimap<Class<? extends Annotation>, EntityCallback> multimap = ArrayListMultimap.create();
            Stream<EntityCallback> stream = streamAnnotation().flatMap(it -> streamCallback(entityClass, it));
            final EntityListeners entityListeners = entityClass.getAnnotation(EntityListeners.class);
            if (entityListeners != null) {
                for (Class listenerClass : entityListeners.value()) {
                    final Object listener = listenerClass.getDeclaredConstructor().newInstance();
                    final Stream<EntityCallback> stream1 = streamAnnotation().flatMap(it -> streamCallback(entityClass, listener, it));
                    stream = Stream.concat(stream, stream1);
                }
            }
            stream.forEach(it -> multimap.put(it.getAnnotationClass(), it));
            return multimap;
        }

        private Stream<Class<? extends Annotation>> streamAnnotation() {
            return Stream.of(PrePersist.class, PostPersist.class, PreUpdate.class, PostUpdate.class, PreRemove.class, PostRemove.class).distinct();
        }

        private Stream<EntityCallback> streamCallback(Class<? extends IEntity> entityClass, Class<? extends Annotation> annotationClass) {
            return getMethodsListWithAnnotation(entityClass, annotationClass, false, true).stream().map(method ->
                    new EntitySelfHandler(entityClass, annotationClass, method)
            );
        }

        private Stream<EntityCallback> streamCallback(Class<? extends IEntity> entityClass, Object listener, Class<? extends Annotation> annotationClass) {
            return getMethodsListWithAnnotation(listener.getClass(), annotationClass, false, true).stream().map(method ->
                    new EntityListenerHandler(entityClass, annotationClass, listener, method)
            );
        }
    });
    protected final List<IEntity> newList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> dirtyList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> cleanList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> deleteList = Collections.synchronizedList(Lists.newArrayList());

    @SneakyThrows
    protected Collection<EntityCallback> getCallback(IEntity o, Class<? extends Annotation> annotationClass) {
        return getCallback(J.actualClass(o.getClass()), annotationClass);
    }

    @SneakyThrows
    protected Collection<EntityCallback> getCallback(Class<? extends IEntity> entityClass, Class<? extends Annotation> annotationClass) {
        final Multimap<Class<? extends Annotation>, EntityCallback> multimap = cache.get(entityClass);
        return multimap.get(annotationClass);
    }

    public interface EntityCallback {
        Class<? extends IEntity> getEntityClass();

        Class<? extends Annotation> getAnnotationClass();

        void callback(IEntity o);
    }

    private static class EntitySelfHandler implements EntityCallback {
        @Getter
        private final Class<? extends IEntity> entityClass;
        @Getter
        private final Class<? extends Annotation> annotationClass;
        private final Method method;

        private EntitySelfHandler(Class<? extends IEntity> entityClass, Class<? extends Annotation> annotationClass, Method method) {
            this.entityClass = entityClass;
            this.annotationClass = annotationClass;
            this.method = method;
            method.setAccessible(true);
        }

        @SneakyThrows
        @Override
        public void callback(IEntity o) {
            method.invoke(o);
        }
    }

    private static class EntityListenerHandler implements EntityCallback {
        @Getter
        private final Class<? extends IEntity> entityClass;
        @Getter
        private final Class<? extends Annotation> annotationClass;
        private final Object listener;
        private final Method method;

        private EntityListenerHandler(Class<? extends IEntity> entityClass, Class<? extends Annotation> annotationClass, Object listener, Method method) {
            this.entityClass = entityClass;
            this.annotationClass = annotationClass;
            this.listener = listener;
            this.method = method;
            method.setAccessible(true);
        }

        @SneakyThrows
        @Override
        public void callback(IEntity o) {
            method.invoke(listener, o);
        }
    }

    @Override
    synchronized public UnitOfWork registerNew(IEntity o) {
        if (!newList.contains(o)) {
            newList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerDirty(IEntity o) {
        if (!dirtyList.contains(o)) {
            dirtyList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerClean(IEntity o) {
        if (!cleanList.contains(o)) {
            cleanList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerDelete(IEntity o) {
        if (!deleteList.contains(o)) {
            deleteList.add(o);
            newList.remove(o);
            dirtyList.remove(o);
            cleanList.remove(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerSave(IEntity o) {
        if (existsByEntity(o)) {
            registerDirty(o);
        } else {
            registerNew(o);
        }
        return this;
    }

    protected abstract boolean existsByEntity(IEntity o);

}
