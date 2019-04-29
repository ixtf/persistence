package com.github.ixtf.persistence.mongo;

import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.IterableUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
public class Jmongo {
    public static final String ID_COL = "_id";
    private final JmongoOptions options;

    public Jmongo(JmongoOptions options) {
        this.options = options;
    }

    public MongoUnitOfWork uow() {
        return new MongoUnitOfWork(this);
    }

    public CompletionStage<Boolean> existsById(Class<?> entityClass, Object id) {
        return id == null ? CompletableFuture.completedStage(false) : count(entityClass, eq(ID_COL, id)).thenApply(it -> it > 0);
    }

    @SneakyThrows
    public CompletionStage<Boolean> existsByEntity(Object entity) {
        final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(entity);
        final String idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
        final Object id = PropertyUtils.getProperty(entity, idFieldName);
        return existsById(entity.getClass(), id);
    }

    @SneakyThrows
    public <T> CompletionStage<Optional<T>> findById(Class<T> entityClass, Object id) {
        return query(entityClass, eq(ID_COL, id), 0, 1).findFirst().run();
    }

    public <T> PublisherBuilder<T> listById(Class<T> entityClass, Iterable ids) {
        return IterableUtils.isEmpty(ids) ? ReactiveStreams.empty() : query(entityClass, in(ID_COL, ids));
    }

    public <T> PublisherBuilder<T> listAll(Class<T> entityClass) {
        return query(entityClass, null);
    }

    public <T> PublisherBuilder<T> query(Class<T> entityClass, Bson condition) {
        return query(null, entityClass, condition, 0, Integer.MAX_VALUE);
    }

    @SneakyThrows
    public CompletionStage<Long> count(Class entityClass, Bson condition) {
        final MongoCollection<Document> collection = collection(entityClass);
        final Publisher<Long> publisher = condition == null ? collection.countDocuments() : collection.countDocuments(condition);
        return ReactiveStreams.fromPublisher(publisher).findFirst().run().thenApply(Optional::get);
    }

    public <T> PublisherBuilder<T> query(Class<T> entityClass, Bson condition, int skip, int limit) {
        return query(null, entityClass, condition, skip, limit);
    }

    @SneakyThrows
    public <T> PublisherBuilder<T> query(ClientSession session, Class<T> entityClass, Bson condition, int skip, int limit) {
        final MongoCollection<Document> collection = collection(entityClass);
        final FindPublisher<Document> publisher;
        if (session == null) {
            publisher = condition == null ? collection.find() : collection.find(condition);
        } else {
            publisher = condition == null ? collection.find(session) : collection.find(session, condition);
        }
        final EntityConverter entityConverter = DocumentEntityConverter.get(this);
        return ReactiveStreams.fromPublisher(publisher)
                .skip(skip)
                .limit(limit)
                .map(document -> entityConverter.toEntity(entityClass, document));
    }

    public MongoClient client() {
        return options.client();
    }

    public MongoCollection<Document> collection(Class entityClass) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public MongoCollection<Document> collection(String name) {
        return options.collection(name);
    }

    @SneakyThrows
    public ClientSession session() {
        return ReactiveStreams.of(client())
                .flatMapRsPublisher(MongoClient::startSession)
                .findFirst().run().toCompletableFuture().get().get();
    }
}
