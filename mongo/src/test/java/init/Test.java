package init;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.bson.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.mongodb.MongoCredential.createScramSha1Credential;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2019-05-16
 */
public class Test {
    private static final Jmongo jmongo = new Jmongo(new JmongoOptions() {
        private final MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
//                        .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
//                        .applyConnectionString(new ConnectionString("mongodb://10.61.0.13"))
                        .applyConnectionString(new ConnectionString("mongodb://10.2.0.212"))
                        .credential(createScramSha1Credential("mes-auto", "admin", "mes-auto-mongo@com.hengyi.japp".toCharArray()))
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

    public static void main(String[] args) {
        check("{\"spindle\":[{\"spindleCode\":\"008L00BIK12F\",\"weight\":\"0\"},{\"spindleCode\":\"008L000MQ01F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096S08F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096S09F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096S10F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096S11F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L12F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C07F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C08F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C09F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C10F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C11F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C12F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096607F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096608F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096609F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096610F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096612F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H01F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H02F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H03F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H04F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H05F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0091H06F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR01F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR02F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR03F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR04F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR05F\",\"weight\":\"0\"},{\"spindleCode\":\"008L008ZR06F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0094Y01F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0094Y02F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0094Y03F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0094Y04F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0094Y06F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L01F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L02F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L03F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L04F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L05F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0095L06F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C02F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C03F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C04F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0090C05F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096601F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096602F\",\"weight\":\"0\"},{\"spindleCode\":\"008L0096606F\",\"weight\":\"0\"}],\"boxCode\":\"010220190606GF0107011F1339\",\"netWeight\":\"732\",\"grossWeight\":\"765.3\",\"grade\":\"AA\",\"automaticPackeLine\":\"1\",\"classno\":\"1\"}");
    }

    @SneakyThrows
    private static void check(String json) {
        final JsonNode jsonNode = MAPPER.readTree(json);
        final String boxCode = jsonNode.get("boxCode").asText();
        final MongoCollection<Document> t_silk = jmongo.collection("T_Silk");
        final Collection<Mono<Document>> silks = Lists.newArrayList();
        for (JsonNode spindleNode : jsonNode.get("spindle")) {
            final String spindleCode = spindleNode.get("spindleCode").asText();
            final Mono<Document> silk = Mono.from(t_silk.find(eq("code", spindleCode)));
            silks.add(silk);
        }
        Flux.merge(silks).toStream().forEach(document -> {
            final String code = document.getString("code");
            final String batchId = document.getString("batch");
            String.join("\t", boxCode, code, batchId);
        });
    }
}
