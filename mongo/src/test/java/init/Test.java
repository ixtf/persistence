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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        fixPackageBox();
//        fixDyeingPrepare();

        System.in.read();
//        fix(null);
    }

    private static void fixDyeingPrepare() {
        final MongoCollection<Document> collection = jmongo.collection("T_DyeingPrepare");
        final Bson b1 = gte("createDateTime", J.date(LocalDate.of(2019, 11, 9)));
        final Bson b2 = lt("createDateTime", J.date(LocalDateTime.of(2019, 11, 13, 0, 0)));
        Flux.from(collection.find(and(b1, b2)))
                .map(Test::fixDyeingPrepare)
                .onErrorResume(err -> Mono.just(false))
                .doOnComplete(() -> System.out.println("fixDyeingPrepare success"))
                .subscribe();
    }

    @SneakyThrows
    private static boolean fixDyeingPrepare(Document document) {
        final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        final String id = document.getString(ID_COL);
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g")
                .uri(URI.create("http://10.2.0.215:9998/api/dyeingPrepares/" + id + "/lucene"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() < 300) {
            return true;
        }
        System.out.println(id);
        return false;
    }

    private static void fixPackageBox() {
        final MongoCollection<Document> collection = jmongo.collection("T_PackageBox");
        final Bson b1 = gte("printDate", J.date(LocalDate.of(2019, 11, 1)));
        final Bson b2 = lt("printDate", J.date(LocalDateTime.of(2019, 11, 13, 0, 0)));
        Flux.from(collection.find(and(b1, b2)))
                .map(Test::fixPackageBox)
                .onErrorResume(err -> Mono.just(false))
                .doOnComplete(() -> System.out.println("fixPackageBox success"))
                .subscribe();
    }

    @SneakyThrows
    private static boolean fixPackageBox(Document document) {
        final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        final String id = document.getString(ID_COL);
        final HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEiLCJpYXQiOjE1NDcxMjk4MzEsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YzA0YTA0NGMzY2FlODEzYjUzMGNkZDEifQ.gO_IM7drZHaEn00kJ2a0kne3B3QrR7bcHVA5fI6ReWElMm2bOjatKogDQfYBs6l31uGTQqSzvGegtmgRsW_BRggUIwRgUEJJ99w1arueAQ_2TJQsIgFnNUoQri3uxrqxv039rKthgmwRmRVMqteJO0k-jZj9RfLARXHzqMPtmlb1j8ZQokrsTGCgouYC0uN1pq2ZhN2MYC3kPty_Rpabgq8RWmLqGAIc6436Lg9d-yEAm_UCYZcuisbjbepCNAUD3frq6qrlhRU8o8vhzYZhxoue7TI4QS-PEk0_crEK_H-Sofc9yoQqUsy9jLp2y2yHQvgpTi5ykveu2jIlitA51g")
                .uri(URI.create("http://10.2.0.215:9998/api/packageBoxes/" + id + "/print"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() < 300) {
            return true;
        }
        System.out.println(id);
        return false;
    }

}
