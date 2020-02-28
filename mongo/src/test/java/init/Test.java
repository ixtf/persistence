package init;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.ObjectId;
import orm.Jmongo3000;
import orm.domain.LineMachine;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-16
 */
public class Test {
    //    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);
    private static final Jmongo jmongo = Jmongo.of(Jmongo3000.class);

    @SneakyThrows
    public static void main(String[] args) {
        final MongoCollection<Document> T_LineMachine = jmongo.collection(LineMachine.class);
        final List<Integer> spindleSeq = List.of(1, 2, 3, 4, 5, 6);
        final Date date = new Date();
        final List<Document> documents = IntStream.range(1, 48).mapToObj(item -> new Document()
                .append(ID_COL, ObjectId.get().toHexString())
                .append("item", item)
                .append("spindleSeq", spindleSeq)
                .append("spindleNum", 6)
                .append("line", "5e58c76af7cee300019f209a")
                .append("creator", "5bffc7e58857b84a917c62ee")
                .append("modifier", "5bffc7e58857b84a917c62ee")
                .append("cdt", date)
                .append("mdt", date)).collect(toList());
        final Success success = Mono.from(T_LineMachine.insertMany(documents)).block();
        System.out.println(success);
    }


}
