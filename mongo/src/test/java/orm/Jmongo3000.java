package orm;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import orm.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.mongodb.MongoCredential.createScramSha1Credential;
import static com.mongodb.client.model.Filters.*;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-06-24
 */
public class Jmongo3000 extends JmongoOptions {
    @Override
    protected MongoClient client() {
        return MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://10.2.0.212"))
                        .credential(createScramSha1Credential("mes-auto", "admin", "mes-auto-mongo@com.hengyi.japp".toCharArray()))
                        .build()
        );
    }

    @Override
    public String dbName() {
        return "mes-auto";
    }

    private static final Jmongo jmongo = Jmongo.of(Jmongo3000.class);

    public static void main(String[] args) {
        final MongoCollection<Document> T_Silk = jmongo.collection(Silk.class);
        final MongoCollection<Document> T_SilkBarcode = jmongo.collection(SilkBarcode.class);
        final Map<String, LineMachine> lineMachineMap = jmongo.find(LineMachine.class).filter(lineMachine -> {
            final Line line = lineMachine.getLine();
            @NotNull final Workshop workshop = line.getWorkshop();
            return Objects.equals(workshop.getCode(), "B");
        }).collect(toMap(LineMachine::getId, Function.identity())).block();
        final Bson codeDateFilter1 = gte("codeDate", J.date(LocalDate.of(2019, 11, 1)));
        final Bson codeDateFilter2 = lte("codeDate", J.date(LocalDate.of(2019, 11, 30)));
        final Bson lineMachineFilter = in("lineMachine", lineMachineMap.keySet());

        final Long count = Flux.from(T_SilkBarcode.find(and(codeDateFilter1, codeDateFilter2, lineMachineFilter)))
                .filterWhen(document -> {
                    final String code = document.getString("code");
                    final String silkCode = code + "01B";
                    return Mono.from(T_Silk.countDocuments(eq("code", silkCode)))
                            .map(it -> it > 0);
                })
                .reduce(0l, (acc, cur) -> {
                    final String lineMachineId = cur.getString("lineMachine");
                    final LineMachine lineMachine = lineMachineMap.get(lineMachineId);
                    return acc + lineMachine.getSpindleNum();
                })
                .block();
        System.out.println(count);
    }
}
