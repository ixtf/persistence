package cache;

import com.github.ixtf.persistence.IEntity;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.MongoUnitOfWork;
import lombok.Data;
import lombok.ToString;
import orm.JmongoDev;

import javax.persistence.*;

/**
 * @author jzb 2019-11-23
 */
public class CacheTest {
    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);

    public static void main(String[] args) {
//        initData();
        final Test oldTest = jmongo.find(Test.class, "1").block();
        final Test test2 = oldTest.getRef();
        System.out.println(test2);

        jmongo.refresh(Test.class, "2");
        System.out.println(oldTest.getRef());
    }

    private static void initData() {
        final MongoUnitOfWork uow = jmongo.uow();
        final Test test1 = new Test();
        uow.registerNew(test1);
        test1.setId("1");
        test1.setName("test[1]");
        final Test test2 = new Test();
        uow.registerNew(test2);
        test1.setRef(test2);
        test2.setId("2");
        test2.setName("test[2]");
        uow.commit();
    }

    @ToString(onlyExplicitlyIncluded = true)
    @Data
    @Cacheable
    @Entity
    public static class Test implements IEntity {
        @ToString.Include
        @Id
        private String id;
        @ToString.Include
        private String name;
        @ManyToOne
        private Test ref;
        private boolean deleted;

        @Column(name = "testsadfasdf")
        public String getTeee() {
            return "";
        }
    }
}
