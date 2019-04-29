package orm;

import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

/**
 * @author jzb 2019-02-14
 */
public class TestMongoOptions extends JmongoOptions {
    private static final MongoClient mongoClient = MongoClients.create(
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
    public String dbName() {
        return "mes-auto";
    }

    @Override
    public MongoClient client() {
        return mongoClient;
    }

}
