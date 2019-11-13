package com.github.ixtf.persistence.lucene;

import com.github.ixtf.persistence.IEntity;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

/**
 * @author jzb 2019-11-12
 */
@Data
public class LuceneCommand implements Serializable {
    private String className;
    private String id;

    @SneakyThrows(ClassNotFoundException.class)
    public Class<? extends IEntity> getClazz() {
        return (Class<? extends IEntity>) Class.forName(className);
    }
}
