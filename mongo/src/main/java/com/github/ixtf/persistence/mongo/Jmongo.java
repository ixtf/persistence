package com.github.ixtf.persistence.mongo;

import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
public abstract class Jmongo {
    public static final String ID_COL = "_id";
    private final EntityConverter entityConverter;
    private static final LoadingCache<Class<? extends JmongoOptions>, Jmongo> CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public Jmongo load(Class<? extends JmongoOptions> clazz) throws Exception {
                    final JmongoOptions options = clazz.getDeclaredConstructor().newInstance();
                    final MongoClient client = options.client();
                    final String dbName = options.dbName();
                    return new Jmongo() {
                        @Override
                        public MongoClient client() {
                            return client;
                        }

                        @Override
                        public MongoDatabase database() {
                            return client.getDatabase(dbName);
                        }
                    };
                }
            });

    private Jmongo() {
        entityConverter = DocumentEntityConverter.get(this);
    }

    @SneakyThrows
    public static Jmongo of(Class<? extends JmongoOptions> clazz) {
        return CACHE.get(clazz);
    }

    public abstract MongoClient client();

    public abstract MongoDatabase database();

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
        final ClassRepresentation classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public MongoUnitOfWork uow() {
        return new MongoUnitOfWork(this);
    }

    public <T> Mono<T> find(Class<T> entityClass, Object id) {
        return find(entityClass, eq(ID_COL, id));
    }

    public <T> Flux<T> find(Class<T> entityClass, Flux ids) {
        return ids.flatMap(id -> find(entityClass, id));
    }

    // 按条件查询
    public <T> Mono<T> find(Class<T> entityClass, Bson filter) {
        return Mono.from(query(entityClass, filter, 0, 1));
    }

    public <T> Flux<T> query(Class<T> entityClass, Bson filter, int skip, int limit) {
        return Flux.from(collection(entityClass).find(filter).skip(skip).limit(limit)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public <T> Flux<T> query(Class<T> entityClass, int skip, int limit) {
        return Flux.from(collection(entityClass).find().skip(skip).limit(limit)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    // 按条件查询
    public <T> Flux<T> query(Class<T> entityClass, Bson filter) {
        return Flux.from(collection(entityClass).find(filter)).map(it -> entityConverter.toEntity(entityClass, it));
    }

    // 查询所有
    public <T> Flux<T> query(Class<T> entityClass) {
        return Flux.from(collection(entityClass).find()).map(it -> entityConverter.toEntity(entityClass, it));
    }

    public Mono<Long> count(Class<?> entityClass, Bson filter) {
        return Mono.from(collection(entityClass).countDocuments(filter));
    }

    public Mono<Long> count(Class<?> entityClass) {
        return Mono.from(collection(entityClass).countDocuments());
    }

    public Mono<Boolean> exists(Class<?> entityClass, Object id) {
        return count(entityClass, eq(ID_COL, id)).map(it -> it > 0);
    }

    @SneakyThrows
    public Mono<Boolean> exists(Object entity) {
        final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(entity);
        final String idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
        final Object id = PropertyUtils.getProperty(entity, idFieldName);
        return exists(entity.getClass(), id);
    }

}
