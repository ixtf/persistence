package com.github.ixtf.persistence.mongo;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ixtf.persistence.EntityDTO;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Optional.ofNullable;

/**
 * @author jzb 2019-02-14
 */
public abstract class Jmongo {
    public static final String ID_COL = "_id";
    private static final LoadingCache<Class<? extends JmongoOptions>, Jmongo> CACHE = Caffeine.newBuilder().build(clazz -> {
        final var options = clazz.getDeclaredConstructor().newInstance();
        final var client = options.client();
        final var dbName = options.dbName();
        return new Jmongo() {
            @Override
            public MongoClient client() {
                return client;
            }

            @Override
            public MongoDatabase database() {
                return client.getDatabase(dbName);
            }

            @Override
            public EntityCacheOptions entityCacheOptions() {
                return options.entityCacheOptions();
            }
        };
    });
    private final LoadingCache<Pair<Class<?>, Object>, Object> entityCache;
    private final EntityConverter entityConverter;

    private Jmongo() {
        entityConverter = DocumentEntityConverter.get(this);
        entityCache = !entityCacheOptions().isCacheable() ? null : Caffeine.newBuilder()
                .maximumSize(entityCacheOptions().getMaximumSize())
                .build(key -> find(key.getLeft(), eq(ID_COL, key.getRight())).block());
    }

    @SneakyThrows
    public static Jmongo of(Class<? extends JmongoOptions> clazz) {
        return CACHE.get(clazz);
    }

    public abstract MongoClient client();

    public abstract MongoDatabase database();

    public abstract EntityCacheOptions entityCacheOptions();

    public boolean isCacheable(Class<?> entityClass) {
        if (entityCache == null) {
            return false;
        }
        final var classRepresentation = ClassRepresentations.create(entityClass);
        return classRepresentation.isCacheable();
    }

    public MongoCollection<Document> collection(String name) {
        return database().getCollection(name);
    }

    public Document toDocument(Object o) {
        return entityConverter.toDbData(new Document(), o);
    }

    public <T> T toEntity(Class<T> entityClass, Document document) {
        return entityConverter.toEntity(entityClass, document);
    }

    public MongoCollection<Document> collection(Class entityClass) {
        final var classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public MongoUnitOfWork uow() {
        return new MongoUnitOfWork(this);
    }

    public <T> Flux<T> find(Class<T> entityClass) {
        return Flux.from(collection(entityClass).find()).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public <T> Mono<T> find(Class<T> entityClass, Object id) {
        final var classRepresentation = ClassRepresentations.create(entityClass);
        if (isCacheable(entityClass)) {
            final Pair<Class<?>, Object> key = Pair.of(classRepresentation.getEntityClass(), id);
            return Mono.fromCallable(() -> entityCache.get(key)).cast(entityClass);
        }
        return find(entityClass, eq(ID_COL, id));
    }

    public void refresh(Class<?> entityClass, Object id) {
        if (isCacheable(entityClass)) {
            final Pair<Class<?>, Object> key = Pair.of(entityClass, id);
            final var ifPresent = entityCache.getIfPresent(key);
            if (ifPresent != null) {
                Mono.from(collection(entityClass).find(eq(ID_COL, id))).subscribe(it -> {
                    entityConverter.fillEntity(ifPresent, it);
                    entityCache.refresh(key);
                });
            }
        }
    }

    public <T> Mono<T> find(Class<T> entityClass, EntityDTO dto) {
        return ofNullable(dto).map(EntityDTO::getId)
                .map(id -> find(entityClass, id))
                .orElseGet(Mono::empty);
    }

    public <T> Mono<T> find(Class<T> entityClass, Principal principal) {
        return ofNullable(principal).map(Principal::getName)
                .map(id -> find(entityClass, id))
                .orElseGet(Mono::empty);
    }

    public <T> Flux<T> find(Class<T> entityClass, Publisher ids) {
        return Flux.from(ids).flatMap(id -> find(entityClass, id));
    }

    // 按条件查询
    public <T> Mono<T> find(Class<T> entityClass, Bson filter) {
        return Flux.from(collection(entityClass).find(filter))
                .map(it -> entityConverter.toEntity(entityClass, it))
                .collectList()
                .flatMap(list -> {
                    if (list.size() == 1) {
                        return Mono.just(list.get(0));
                    } else if (list.size() < 1) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new RuntimeException("多值返回"));
                    }
                });
    }

    // 查询所有
    public <T> Flux<T> query(Class<T> entityClass) {
        final var condition = eq("deleted", false);
        return Flux.from(collection(entityClass).find(condition)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public <T> Flux<T> query(Class<T> entityClass, int skip, int limit) {
        final var condition = eq("deleted", false);
        return Flux.from(collection(entityClass).find(condition).skip(skip).limit(limit)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    // 按条件查询
    public <T> Flux<T> query(Class<T> entityClass, Bson filter) {
        final var deletedFilter = eq("deleted", false);
        final var condition = and(filter, deletedFilter);
        return Flux.from(collection(entityClass).find(condition)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public <T> Flux<T> query(Class<T> entityClass, Bson filter, int skip, int limit) {
        final var deletedFilter = eq("deleted", false);
        final var condition = and(filter, deletedFilter);
        return Flux.from(collection(entityClass).find(condition).skip(skip).limit(limit)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public <T> Flux<T> query(Class<T> entityClass, Optional<Bson> filterOpt) {
        return filterOpt.map(filter -> query(entityClass, filter)).orElseGet(() -> query(entityClass));
    }

    public <T> Flux<T> query(Class<T> entityClass, Optional<Bson> filterOpt, int skip, int limit) {
        return filterOpt.map(filter -> query(entityClass, filter, skip, limit)).orElseGet(() -> query(entityClass, skip, limit));
    }

    public Mono<Long> count(Class<?> entityClass) {
        final var condition = eq("deleted", false);
        return Mono.from(collection(entityClass).countDocuments(condition));
    }

    public Mono<Long> count(Class<?> entityClass, Bson filter) {
        final var deletedFilter = eq("deleted", false);
        final var condition = and(filter, deletedFilter);
        return Mono.from(collection(entityClass).countDocuments(condition));
    }

    public Mono<Long> count(Class<?> entityClass, Optional<Bson> filterOpt) {
        return filterOpt.map(filter -> count(entityClass, filter)).orElseGet(() -> count(entityClass));
    }

    public Mono<Boolean> exists(Class<?> entityClass, Object id) {
        final var condition = eq(ID_COL, id);
        return Mono.from(collection(entityClass).countDocuments(condition)).map(it -> it > 0);
    }

    @SneakyThrows
    public Mono<Boolean> exists(Object entity) {
        final var classRepresentation = ClassRepresentations.create(entity);
        final var idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
        final var id = PropertyUtils.getProperty(entity, idFieldName);
        return exists(entity.getClass(), id);
    }

}
