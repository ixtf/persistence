package init;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import orm.JmongoDev;

/**
 * @author jzb 2019-05-16
 */
public class Test {
    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);
//    private static final Jmongo jmongo = Jmongo.of(Jmongo3000.class);

    public static void main(String[] args) {
        final MongoCollection<Document> t_operator = jmongo.collection("T_Operator");
        System.out.println(t_operator);
    }

}
