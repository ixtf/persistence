package com.github.ixtf.persistence.mongo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.mongo.api.DocumentEntityConverter;
import com.github.ixtf.persistence.mongo.api.MongoUnitOfWork;
import com.github.ixtf.persistence.mongo.spi.MongoProvider;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
public final class Jmongo {

    public static final String ID_COL = "_id";

    public static MongoUnitOfWork uow() {
        return new MongoUnitOfWork();
    }

    public static boolean existsById(Class<?> entityClass, Object id) {
        return Optional.ofNullable(id)
                .map(it -> count(entityClass, eq(ID_COL, id)) > 0)
                .orElse(false);
    }

    @SneakyThrows
    public static boolean existsByEntity(Object entity) {
        final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(entity);
        final String idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
        final Object id = PropertyUtils.getProperty(entity, idFieldName);
        return existsById(entity.getClass(), id);
    }

    @SneakyThrows
    public static <T> Optional<T> findById(Class<T> entityClass, Object id) {
        return query(entityClass, eq(ID_COL, id), 0, 1).findFirst().run().toCompletableFuture().get();
    }

    public static <T> PublisherBuilder<T> listById(Class<T> entityClass, Iterable ids) {
        return IterableUtils.isEmpty(ids) ? ReactiveStreams.empty() : query(entityClass, in(ID_COL, ids));
    }

    public static <T> PublisherBuilder<T> listAll(Class<T> entityClass) {
        return query(entityClass, null);
    }

    public static <T> PublisherBuilder<T> query(Class<T> entityClass, Bson condition) {
        return query(null, entityClass, condition, 0, Integer.MAX_VALUE);
    }

    @SneakyThrows
    public static long count(Class entityClass, Bson condition) {
        final MongoCollection<Document> collection = collection(entityClass);
        final Publisher<Long> publisher = condition == null ? collection.countDocuments() : collection.countDocuments(condition);
        return ReactiveStreams.fromPublisher(publisher).findFirst().run().toCompletableFuture().get().get();
    }

    public static <T> PublisherBuilder<T> query(Class<T> entityClass, Bson condition, int skip, int limit) {
        return query(null, entityClass, condition, skip, limit);
    }

    @SneakyThrows
    public static <T> PublisherBuilder<T> query(ClientSession session, Class<T> entityClass, Bson condition, int skip, int limit) {
        final MongoCollection<Document> collection = collection(entityClass);
        final FindPublisher<Document> publisher;
        if (session == null) {
            publisher = condition == null ? collection.find() : collection.find(condition);
        } else {
            publisher = condition == null ? collection.find(session) : collection.find(session, condition);
        }
        final EntityConverter entityConverter = DocumentEntityConverter.get(entityClass);
        return ReactiveStreams.fromPublisher(publisher)
                .skip(skip)
                .limit(limit)
                .map(document -> entityConverter.toEntity(entityClass, document));
    }

    public static MongoClient client() {
        return Holder.MONGO_PROVIDERS.get().client();
    }

    public static MongoCollection<Document> collection(Class entityClass) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public static MongoCollection<Document> collection(String name) {
        return Holder.MONGO_PROVIDERS.get().collection(name);
    }

    @SneakyThrows
    public static ClientSession session() {
        return ReactiveStreams.of(client())
                .flatMapRsPublisher(MongoClient::startSession)
                .findFirst().run().toCompletableFuture().get().get();
    }

    private static class Holder {
        private static final MongoClientProviderRegistry MONGO_PROVIDERS = new MongoClientProviderRegistry();
    }

    private static class MongoClientProviderRegistry {
        private final List<MongoProvider> mongoProviders;

        private MongoClientProviderRegistry() {
            mongoProviders = StreamSupport.stream(ServiceLoader.load(MongoProvider.class).spliterator(), false)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }

        private MongoProvider get() {
            if (J.isEmpty(mongoProviders)) {
                throw new RuntimeException("没有 MongoProvider");
            }
            if (mongoProviders.size() == 1) {
                return mongoProviders.get(0);
            }
            throw new RuntimeException("多个 MongoProvider");
        }
    }

    private Jmongo() {
    }
}
