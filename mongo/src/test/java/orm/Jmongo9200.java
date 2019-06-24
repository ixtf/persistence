package orm;

import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import static com.mongodb.MongoCredential.createScramSha1Credential;

/**
 * @author jzb 2019-06-24
 */
public class Jmongo9200 extends JmongoOptions {
    @Override
    protected MongoClient client() {
        return MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://10.61.0.13"))
                        .credential(createScramSha1Credential("mes-auto", "admin", "mes-auto-mongo@com.hengyi.japp".toCharArray()))
                        .credential(createScramSha1Credential("test", "admin", "test".toCharArray()))
                        .build()
        );
    }

    @Override
    public String dbName() {
        return "mes-auto";
    }
}
