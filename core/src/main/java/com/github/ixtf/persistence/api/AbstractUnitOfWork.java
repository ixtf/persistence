package com.github.ixtf.persistence.api;

import com.github.ixtf.persistence.IEntity;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.List;

/**
 * @author jzb 2019-02-18
 */
public abstract class AbstractUnitOfWork implements UnitOfWork {
    protected final List<IEntity> newList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> dirtyList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> cleanList = Collections.synchronizedList(Lists.newArrayList());
    protected final List<IEntity> deleteList = Collections.synchronizedList(Lists.newArrayList());

    @SneakyThrows
    @Override
    synchronized public UnitOfWork registerNew(IEntity o) {
        if (!newList.contains(o)) {
//            final ClassRepresentation<?> classRepresentation = ClassRepresentations.create(o);
//            final String idFieldName = classRepresentation.getId().map(FieldRepresentation::getFieldName).get();
//            final Object idValue = PropertyUtils.getProperty(o, idFieldName);
//            if (idValue == null) {
//                PropertyUtils.setProperty(o, idFieldName, newId());
//            }
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

//    protected abstract Object newId();
}
