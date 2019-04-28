package com.github.ixtf.persistence.mongo.api;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.IEntity;
import com.github.ixtf.persistence.api.AbstractUnitOfWork;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
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
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-02-18
 */
@Slf4j
public class MongoUnitOfWork extends AbstractUnitOfWork {

    @SneakyThrows
    @Override
    synchronized public MongoUnitOfWork commit() {
        PublisherBuilder<Pair<String, WriteModel<Document>>> builder = ReactiveStreams.empty();
        builder = ReactiveStreams.concat(builder, ReactiveStreams.fromIterable(newList).map(this::newListWriteModel));
        builder = ReactiveStreams.concat(builder, ReactiveStreams.fromIterable(dirtyList).map(this::dirtyListWriteModel));
        builder = ReactiveStreams.concat(builder, ReactiveStreams.fromIterable(deleteList).map(this::deleteListWriteModel));
        final var completionStage = builder.collect(groupingBy(Pair::getKey, LinkedHashMap::new, mapping(Pair::getRight, toList()))).run();
        return ReactiveStreams.fromCompletionStage(completionStage)
                .flatMapIterable(Map::entrySet)
                .flatMapRsPublisher(entry -> {
                    final MongoCollection<Document> collection = Jmongo.collection(entry.getKey());
                    return collection.bulkWrite(entry.getValue());
                }).toList().run().thenApply(bulkWriteResults -> {
                    log.debug("" + bulkWriteResults);
                    // todo 更新的数量是否一致
                    return this;
                }).toCompletableFuture().get();
    }

    private Pair<String, WriteModel<Document>> newListWriteModel(IEntity o) {
        if (J.isBlank(o.getId())) {
            o.setId(new ObjectId().toHexString());
        }
        final Document document = toDocument(o);
        final InsertOneModel<Document> model = new InsertOneModel<>(document);
        return Pair.of(collectionName(o), model);
    }

    private Pair<String, WriteModel<Document>> dirtyListWriteModel(IEntity o) {
        final Document document = toDocument(o);
        document.remove(ID_COL);
        final Document $set = new Document("$set", document);
        final UpdateOneModel<Document> model = new UpdateOneModel<>(new Document(ID_COL, idValue(o)), $set);
        return Pair.of(collectionName(o), model);
    }

    private Pair<String, WriteModel<Document>> deleteListWriteModel(IEntity o) {
        final DeleteOneModel<Document> model = new DeleteOneModel<>(new Document(ID_COL, idValue(o)));
        return Pair.of(collectionName(o), model);
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

    private Document toDocument(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o);
        if (!classRepresentation.hasId()) {
            throw new RuntimeException("Class[" + o.getClass() + "]，id不存在");
        }
        final EntityConverter entityConverter = DocumentEntityConverter.get(o.getClass());
        return entityConverter.toDbData(new Document(), o);
    }

    @Override
    synchronized public MongoUnitOfWork rollback() {
        // todo mongo 需要集群才支持事务，后续实现
        return this;
    }

    @Override
    protected boolean existsByEntity(IEntity o) {
        return Jmongo.existsByEntity(o);
    }
}
