package init;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-05-16
 */
public class Test {
//        private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);
//    private static final Jmongo jmongo = Jmongo.of(Jmongo3000.class);

    public static void main(String[] args) {
        IntStream.rangeClosed(258, 269)
                .mapToObj(it -> "010120190819GF031501100" + it)
                .forEach(Test::undoPackageBox);
    }

    @SneakyThrows
    private static void undoPackageBox(String code) {
        final HttpClient httpClient = HttpClient.newHttpClient();
        final ObjectNode node = MAPPER.createObjectNode().put("code", code);
        final String body = MAPPER.writeValueAsString(node);
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create("http://10.2.0.215:9999/warehouse/PackageBoxFetchEvent"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//        final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//        final String body = response.body();
//        System.out.println(body);
    }

}
