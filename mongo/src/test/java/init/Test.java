package init;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.poi.Jpoi;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.Document;
import org.bson.types.ObjectId;
import orm.domain.SilkCar;
import orm.domain.SilkCarType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static com.mongodb.MongoCredential.createScramSha1Credential;
import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-05-16
 */
public class Test {
    private static final Jmongo jmongo = new Jmongo(new JmongoOptions() {
        private final MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
//                        .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
                        .applyConnectionString(new ConnectionString("mongodb://10.61.0.13"))
//                    .applyConnectionString(new ConnectionString("mongodb://10.2.0.212"))
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
        final MongoCollection<Document> t_silkcar = jmongo.collection(SilkCar.class);
        final List<Document> documents = IntStream.rangeClosed(900, 1249).mapToObj(i -> {
            final String s = Strings.padStart("" + i, 4, '0');
            final String number = "P60" + s;
            final String code = "9200" + number;
            final Date currentDate = new Date();
            final Document silkCar = new Document().append(ID_COL, ObjectId.get().toHexString())
                    .append("type", SilkCarType.BIG_SILK_CAR.name())
                    .append("number", number)
                    .append("code", code)
                    .append("row", 3)
                    .append("col", 5)
                    .append("pliesNum", 2)
                    .append("creator", "5b384b2cd8712064f101e31e")
                    .append("cdt", currentDate)
                    .append("modifier", "5b384b2cd8712064f101e31e")
                    .append("mdt", currentDate);
            return silkCar;
        }).collect(toList());
        Mono.from(t_silkcar.insertMany(documents)).block();
    }

    @SneakyThrows
    private static Stream<Document> test(Map<String, String> groupMap, MongoCollection<Document> t_operator, MongoCollection<Document> t_login) {
        @Cleanup final Workbook wb = new HSSFWorkbook(new FileInputStream("/home/jzb/Documents/WeChat Files/jinzhaobo_ixtf/Files/人员名单.xls"));
        final Sheet sheet = wb.getSheetAt(0);
        return IntStream.rangeClosed(1, sheet.getLastRowNum())
                .mapToObj(sheet::getRow)
                .filter(row -> {
                    final String hrId = getString(row, 'A');
                    final Long count = Flux.concat(t_operator.find(eq("hrId", hrId)), t_login.find(eq("loginId", hrId)))
                            .count().block();
                    if (count > 0) {
                        System.out.println(hrId);
                    }
                    return count == 0;
                })
                .map(row -> {
                    final Document document = new Document()
                            .append("_id", ObjectId.get().toHexString())
                            .append("hrId", getString(row, 'A'))
                            .append("name", getString(row, 'B'));
                    final String eString = getString(row, 'E');
                    if (StringUtils.contains(eString, "打包")) {
                        document.append("roles", Sets.newHashSet("PACKAGE_BOX"));
                    }
                    final Set<String> groups = groupMap.entrySet().parallelStream()
                            .filter(entry -> {
                                final String key = entry.getKey();
                                final String iString = getString(row, 'I');
                                if (J.nonBlank(iString)) {
                                    final boolean b = StringUtils.contains(key, iString);
                                    if (b) {
                                        return true;
                                    }
                                }
                                final String kString = getString(row, 'K');
                                if (J.nonBlank(kString)) {
                                    final boolean b = StringUtils.contains(key, kString);
                                    if (b) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .map(Map.Entry::getValue)
                            .collect(toSet());
                    return document.append("groups", groups);
                });
    }

    private static String getString(Row row, char c) {
        final Cell cell = Jpoi.cell(row, c);
        final String ret = cell.getStringCellValue();
        return StringUtils.deleteWhitespace(ret);
    }
}
