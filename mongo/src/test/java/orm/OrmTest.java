package orm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.MongoUnitOfWork;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import orm.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-14
 */
public class OrmTest {
    private static final Jmongo jmongo = new Jmongo(new TestMongoOptions());

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        var line = jmongo.findById(Line.class, "5c8782bba3f0a0602365d796").toCompletableFuture().get().get();
        final Workshop.WorkshopEmbeddable workshopEmbeddable = line.getWorkshopEmbeddable();
        workshopEmbeddable.setId("5c877549a3f0a02467a817f0");
        workshopEmbeddable.setName("test");
        final Corporation.CorporationEmbeddable corporationEmbeddable = new Corporation.CorporationEmbeddable();
        corporationEmbeddable.setId("5c81424f4e90f84e4542f097");
        corporationEmbeddable.setName("name");
        workshopEmbeddable.setCorporationEmbeddable(corporationEmbeddable);
        jmongo.uow().registerDirty(line).commit();
        System.out.println(line);
        final MongoUnitOfWork uow = jmongo.uow();
        final Test test1 = new Test();
        test1.setStrings(null);
//        uow.registerNew(test1).commit();
//        final Test sfafdafasfasf = jmongo.findById(Test.class, "5c7d8d7f7e4eaa47a659cb15").orElse(null);
//        System.out.println(sfafdafasfasf);

//        readTest();
//        writeTest();

        final List<String> test = ReactiveStreams.of("test").map(it -> {
            if (1 == 1) {
//                throw new RuntimeException();
            }
            return it;
        }).toList().run().whenComplete((v, e) -> {
            System.out.println(e);
        }).toCompletableFuture().get();
        System.out.println(test);
        TimeUnit.DAYS.sleep(1);
    }

    @SneakyThrows
    private static void writeTest() {
        final SilkCarRecord silkCarRecord = jmongo.findById(SilkCarRecord.class, "5bfde010d939c40001934416").toCompletableFuture().get().get();
        final SilkCar silkCar = silkCarRecord.getSilkCar();

//        final SilkCar silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df").get();
        silkCar.setCol(6);
        silkCar.setDoffingType(DoffingType.AUTO);
        MongoUnitOfWork uow = jmongo.uow();
        uow.registerDirty(silkCar).commit();

        uow = jmongo.uow();
        final Test test = new Test();
        test.setId("5c78ba5264279801e82ce828");
//        test.setStrings(Lists.newArrayList("test1", "test2"));
        test.setStrings(Lists.newArrayList());
        uow.registerDirty(test);
        uow.commit();
    }

    @SneakyThrows
    private static void readTest() {
        final Optional<SilkCar> silkCar = jmongo.findById(SilkCar.class, "5bfd4b4f67e7ad00013055df").toCompletableFuture().get();
        ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<Silk> silk = jmongo.findById(Silk.class, "5bfde010d939c400019343eb").toCompletableFuture().get();
        objectNode = silk.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<SilkCarRecord> silkCarRecord = jmongo.findById(SilkCarRecord.class, "5bfde010d939c40001934416").toCompletableFuture().get();
        objectNode = silkCarRecord.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);
    }

}
