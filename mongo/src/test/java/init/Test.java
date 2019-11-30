package init;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.conversions.Bson;
import orm.Jmongo3000;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static com.mongodb.client.model.Filters.*;

/**
 * @author jzb 2019-05-16
 */
public class Test {
    //        private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);
    private static final Jmongo jmongo = Jmongo.of(Jmongo3000.class);

    @SneakyThrows
    public static void main(String[] args) {
        final MongoCollection<Document> T_SilkBarcode = jmongo.collection("T_SilkBarcode");
        final MongoCollection<Document> T_Silk = jmongo.collection("T_Silk");
        final MongoCollection<Document> T_PackageBox = jmongo.collection("T_PackageBox");

        final Bson lineMachineFilter = eq("lineMachine", "5bffa63d8857b85a437d216f");
        final Bson codeDateFilter1 = gte("codeDate", J.date(LocalDate.of(2019, 11, 13)));
        final Bson codeDateFilter2 = lte("codeDate", J.date(LocalDate.of(2019, 11, 18)));
        final Bson filter = and(lineMachineFilter, codeDateFilter1, codeDateFilter2);
        Flux.from(T_SilkBarcode.find(filter)).toStream().forEach(document -> {
            final LocalDate codeDate = J.localDate(document.getDate("codeDate"));
            final String doffingNum = document.getString("doffingNum");

            final String silkCode = document.getString("code") + "08B";
            final Document silkDoc = Mono.from(T_Silk.find(eq("code", silkCode))).block();
            if (silkDoc == null) {
                System.out.println(codeDate + "\t" + doffingNum + "\t" + silkCode + "\t" + "无");
                return;
            }
            final String packageBoxId = silkDoc.getString("packageBox");
            if (J.nonBlank(packageBoxId)) {
                final Document packageBoxDoc = Mono.from(T_PackageBox.find(eq(ID_COL, packageBoxId))).block();
                final String packageBoxCode = packageBoxDoc.getString("code");
                System.out.println(codeDate + "\t" + doffingNum + "\t" + silkCode + "\t" + packageBoxCode);
                return;
            }

            System.out.println(codeDate + "\t" + doffingNum + "\t" + silkCode + "\t" + "查丝车");
        });
    }


}
