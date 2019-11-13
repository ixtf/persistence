package elasticsearch;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

/**
 * @author jzb 2019-11-08
 */
public class ElasticsearchTest {

    private static final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.0.38", 9200, "http")));

    @SneakyThrows
    public static void main(String[] args) {
//        index();
//        get();
        search();
        System.in.read();
    }

    private static void search() {
        final BoolQueryBuilder bqBuilder = QueryBuilders.boolQuery();
        bqBuilder.must(QueryBuilders.termQuery("code", "code"));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(bqBuilder).fetchSource(false);
        final SearchRequest searchRequest = new SearchRequest("packageboxes").source(searchSourceBuilder);
        client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                System.out.println(searchResponse);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void get() {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("code", "code");
        final GetRequest getRequest = new GetRequest("packageboxes", "test")
                .fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
        client.getAsync(getRequest, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(GetResponse documentFields) {
                System.out.println(documentFields);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void index() throws IOException {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("code", "code");
        final IndexRequest indexRequest = new IndexRequest("packageboxes").id("test").source(map);
        client.index(indexRequest, RequestOptions.DEFAULT);
    }
}
