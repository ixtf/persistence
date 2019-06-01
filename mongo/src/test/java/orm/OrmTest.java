package orm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.github.ixtf.persistence.mongo.MongoUnitOfWork;
import com.google.common.collect.Lists;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.SneakyThrows;
import orm.domain.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-14
 */
public class OrmTest {
    private static final Jmongo jmongo = new Jmongo(new JmongoOptions() {
        private final MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
//                    .applyConnectionString(new ConnectionString("mongodb://10.61.0.13"))
//                    .applyConnectionString(new ConnectionString("mongodb://10.2.0.212"))
//                    .credential(createScramSha1Credential("mes-auto", "admin", "mes-auto-mongo@com.hengyi.japp".toCharArray()))
//                    .credential(MongoCredential.createScramSha1Credential("test", "admin", "test".toCharArray()))
                        .build()
        );

        //    private static final MongoClient mongoClient = MongoClients.create(
//            MongoClientSettings.builder()
//                    .applyConnectionString(new ConnectionString("mongodb://10.61.0.13"))
////                    .applyConnectionString(new ConnectionString("mongodb://10.2.0.212"))
//                    .credential(createScramSha1Credential("mes-auto", "admin", "mes-auto-mongo@com.hengyi.japp".toCharArray()))
//                    .build()
//    );
        @Override
        public MongoClient client() {
            return mongoClient;
        }

        @Override
        public String dbName() {
            return "mes-auto";
        }
    });

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        final Mono<SilkCarRecord> mono = jmongo.find(SilkCarRecord.class, "test");
        final SilkCarRecord silkCarRecord = mono.defaultIfEmpty(new SilkCarRecord()).block();
        System.out.println(silkCarRecord);

        final SilkCar silkCar = jmongo.find(SilkCar.class, "5bffa7917979c4000146fd3d").block();
        silkCar.setNumber(silkCar.getNumber() + "-test");
        silkCar.setTestTypes(Lists.newArrayList(SilkCarType.values()));
        jmongo.uow().registerDirty(silkCar).commit();

        var line = jmongo.find(Line.class, "5c8782bba3f0a0602365d796").block();
        final Workshop.WorkshopEmbeddable workshopEmbeddable = Optional.ofNullable(line.getWorkshopEmbeddable()).orElse(new Workshop.WorkshopEmbeddable());
        workshopEmbeddable.setId("5c877549a3f0a02467a817f0");
        workshopEmbeddable.setName("test");
        final Corporation.CorporationEmbeddable corporationEmbeddable = new Corporation.CorporationEmbeddable();
        corporationEmbeddable.setId("5c81424f4e90f84e4542f097");
        corporationEmbeddable.setName("name");
        workshopEmbeddable.setCorporationEmbeddable(corporationEmbeddable);
        line.setWorkshopEmbeddable(workshopEmbeddable);
        jmongo.uow().registerDirty(line).commit();
    }

    @SneakyThrows
    private static void writeTest() {
        final SilkCarRecord silkCarRecord = jmongo.find(SilkCarRecord.class, "5bfde010d939c40001934416").block();
        final SilkCar silkCar = silkCarRecord.getSilkCar();

//        final SilkCar silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df").get();
        silkCar.setCol(6);
//        silkCar.setDoffingType(DoffingType.AUTO);
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
        final Optional<SilkCar> silkCar = jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df").blockOptional();
        ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<Silk> silk = jmongo.find(Silk.class, "5bfde010d939c400019343eb").blockOptional();
        objectNode = silk.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<SilkCarRecord> silkCarRecord = jmongo.find(SilkCarRecord.class, "5bfde010d939c40001934416").blockOptional();
        objectNode = silkCarRecord.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);
    }

}
