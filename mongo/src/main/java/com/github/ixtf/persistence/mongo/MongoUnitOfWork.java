package com.github.ixtf.persistence.mongo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.IEntity;
import com.github.ixtf.persistence.api.AbstractUnitOfWork;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-02-18
 */
@Slf4j
public class MongoUnitOfWork extends AbstractUnitOfWork {
    private final Jmongo jmongo;
    private Iterable<BulkWriteResult> bulkWriteResults;

    MongoUnitOfWork(Jmongo jmongo) {
        this.jmongo = jmongo;
    }

    @Override
    synchronized public MongoUnitOfWork commit() {
        if (bulkWriteResults != null) {
            return this;
        }
        bulkWriteResults = Flux.concat(newListWriteModel(), dirtyListWriteModel(), deleteListWriteModel())
                .collect(groupingBy(Pair::getKey, LinkedHashMap::new, mapping(Pair::getRight, toList())))
                .flatMapIterable(Map::entrySet)
                .flatMap(entry -> {
                    final MongoCollection<Document> collection = jmongo.collection(entry.getKey());
                    return collection.bulkWrite(entry.getValue());
                })
                .collect(toList()).block();
        if (log.isDebugEnabled()) {
            log(bulkWriteResults);
        }
        J.emptyIfNull(newList).stream().forEach(entity ->
                callbackStream(entity, PostPersist.class).forEach(it -> it.callback(entity))
        );
        J.emptyIfNull(dirtyList).stream().forEach(entity ->
                callbackStream(entity, PostUpdate.class).forEach(it -> it.callback(entity))
        );
        J.emptyIfNull(deleteList).stream().forEach(entity ->
                callbackStream(entity, PostRemove.class).forEach(it -> it.callback(entity))
        );
        return this;
    }

    private Publisher<Pair<String, WriteModel<Document>>> newListWriteModel() {
        return Flux.fromIterable(newList).map(o -> {
            callbackStream(o, PrePersist.class).forEach(it -> it.callback(o));
            if (J.isBlank(o.getId())) {
                o.setId(new ObjectId().toHexString());
            }
            final Document document = jmongo.toDocument(o);
            return Pair.of(collectionName(o), new InsertOneModel<>(document));
        });
    }

    private Publisher<Pair<String, WriteModel<Document>>> dirtyListWriteModel() {
        return Flux.fromIterable(dirtyList).map(o -> {
            callbackStream(o, PreUpdate.class).forEach(it -> it.callback(o));
            final Document document = jmongo.toDocument(o);
            document.remove(ID_COL);
            final Document $set = new Document("$set", document);
            final UpdateOneModel<Document> model = new UpdateOneModel<>(new Document(ID_COL, idValue(o)), $set);
            return Pair.of(collectionName(o), model);
        });
    }

    private Publisher<Pair<String, WriteModel<Document>>> deleteListWriteModel() {
        return Flux.fromIterable(deleteList).map(o -> {
            callbackStream(o, PreRemove.class).forEach(it -> it.callback(o));
            final DeleteOneModel<Document> model = new DeleteOneModel<>(new Document(ID_COL, idValue(o)));
            return Pair.of(collectionName(o), model);
        });
    }

    private String collectionName(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o.getClass());
        return classRepresentation.getTableName();
    }

    @SneakyThrows
    private Object idValue(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o.getClass());
        final Optional<FieldRepresentation> id = classRepresentation.getId();
        final String nativeFieldName = id.map(FieldRepresentation::getFieldName).get();
        return PropertyUtils.getProperty(o, nativeFieldName);
    }

    @Override
    synchronized public MongoUnitOfWork rollback() {
        // todo mongo 需要集群才支持事务，后续实现
        return this;
    }

    @SneakyThrows
    @Override
    protected boolean exists(IEntity o) {
        return jmongo.exists(o).block();
    }

    private void log(Iterable<BulkWriteResult> bulkWriteResults) {
        final AtomicInteger newCount = new AtomicInteger();
        final AtomicInteger dirtyCount = new AtomicInteger();
        final AtomicInteger deleteCount = new AtomicInteger();
        bulkWriteResults.forEach(bulkWriteResult -> {
            final int insertedCount = bulkWriteResult.getInsertedCount();
            newCount.addAndGet(insertedCount);
            final int modifiedCount = bulkWriteResult.getModifiedCount();
            dirtyCount.addAndGet(modifiedCount);
            final int deletedCount = bulkWriteResult.getDeletedCount();
            deleteCount.addAndGet(deletedCount);
            final String join = String.join(";",
                    "newList=" + J.emptyIfNull(newList) + ",newCount=" + newCount.get(),
                    "dirtyList=" + J.emptyIfNull(dirtyList) + ",dirtyCount=" + dirtyCount.get(),
                    "deleteList=" + J.emptyIfNull(deleteList) + ",deleteCount=" + deleteCount.get()
            );
            log.info(join);
        });
    }
}
