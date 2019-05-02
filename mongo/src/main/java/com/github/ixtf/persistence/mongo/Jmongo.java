package com.github.ixtf.persistence.mongo;

import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
public class Jmongo {
    public static final String ID_COL = "_id";
    private final JmongoOptions options;
    private final EntityConverter entityConverter;

    public Jmongo(JmongoOptions options) {
        this.options = options;
        entityConverter = DocumentEntityConverter.get(this);
    }

    public MongoUnitOfWork uow() {
        return new MongoUnitOfWork(this);
    }

    public MongoCollection<Document> collection(Class entityClass) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public MongoCollection<Document> collection(String name) {
        return options.collection(name);
    }

    // 按条件查询
    public <T> Flux<T> find(Class<T> entityClass, Bson filter) {
        final MongoCollection<Document> collection = collection(entityClass);
        final FindPublisher<Document> publisher = filter == null ? collection.find() : collection.find(filter);
        return Flux.from(publisher).map(document -> entityConverter.toEntity(entityClass, document));
    }

    // 查询所有
    public <T> Flux<T> find(Class<T> entityClass) {
        return find(entityClass, null);
    }

    // 按条件查询——分页
    public <T> Flux<T> find(Class<T> entityClass, Bson filter, int skip, int limit) {
        final MongoCollection<Document> collection = collection(entityClass);
        final FindPublisher<Document> publisher = filter == null ? collection.find() : collection.find(filter);
        return Flux.from(publisher.skip(skip).limit(limit)).map(document -> entityConverter.toEntity(entityClass, document));
    }

    // 查询所有——分页
    public <T> Flux<T> find(Class<T> entityClass, int skip, int limit) {
        return find(entityClass, null, skip, limit);
    }

    public <T> Mono<T> find(Class<T> entityClass, Object id) {
        return Mono.from(find(entityClass, eq(ID_COL, id)));
    }

    public <T> Flux<T> list(Class<T> entityClass, Iterable ids) {
        return Flux.fromIterable(ids).flatMap(id -> find(entityClass, id));
    }

    public Mono<Long> count(Class<?> entityClass, Bson filter) {
        final MongoCollection<Document> collection = collection(entityClass);
        final Publisher<Long> publisher = filter == null ? collection.countDocuments() : collection.countDocuments(filter);
        return Mono.from(publisher);
    }

    public Mono<Long> count(Class<?> entityClass) {
        return count(entityClass, null);
    }

    public Mono<Boolean> exists(Class<?> entityClass, Object id) {
        if (id == null) {
            return Mono.empty();
        }
        final Publisher<Long> publisher = count(entityClass, eq(ID_COL, id));
        return Mono.from(publisher).map(it -> it > 0);
    }

    @SneakyThrows
    public Mono<Boolean> exists(Object entity) {
        final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(entity);
        final String idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
        final Object id = PropertyUtils.getProperty(entity, idFieldName);
        return exists(entity.getClass(), id);
    }

    public Document toDocument(Object o) {
//        final ClassRepresentation classRepresentation = ClassRepresentations.create(o);
//        if (!classRepresentation.hasId()) {
//            throw new RuntimeException("Class[" + o.getClass() + "]，id不存在");
//        }
        return entityConverter.toDbData(new Document(), o);
    }

}
