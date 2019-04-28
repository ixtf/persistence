package orm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.japp.poi.Jpoi;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.api.MongoUnitOfWork;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import orm.domain.*;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;

/**
 * @author jzb 2019-02-14
 */
public class OrmTest {

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        final MongoCollection<Document> t_silkCar = Jmongo.collection("T_SilkCar");
        final Bson condition = Filters.regex("code", "^YJ");
        final List<Document> documents = ReactiveStreams.fromPublisher(t_silkCar.find(condition))
                .toList().run().toCompletableFuture().get();
//        documents.forEach(it -> {
//            System.out.println(it.getString("code"));
//        });

        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFSheet sheet = wb.createSheet();
        IntStream.range(0,documents.size()).forEach(i->{
            final Document document = documents.get(i);
            final Row row = CellUtil.getRow(i, sheet);
             Cell cell = Jpoi.cell(row, 'A');
            cell.setCellValue(document.getString("code"));
            cell = Jpoi.cell(row, 'B');
            cell.setCellValue(document.getInteger("row"));
            cell = Jpoi.cell(row, 'C');
            cell.setCellValue(document.getInteger("col"));
            cell = Jpoi.cell(row, 'D');
            cell.setCellValue(document.getString("number"));
            cell = Jpoi.cell(row, 'E');
            cell.setCellValue(document.getString(ID_COL));
        });
        wb.write(new FileOutputStream("/home/jzb/silk_car_9200.xlsx"));


//        var query = Jmongo.findById(Line.class, "5bfd4b87716bb151bd059dcc").get();
//        final Workshop.WorkshopEmbeddable workshopEmbeddable = query.getWorkshopEmbeddable();
//        workshopEmbeddable.setId("5bfd4b87716bb151bd059db3");
//        workshopEmbeddable.setName("test");
//        final Corporation.CorporationEmbeddable corporationEmbeddable = new Corporation.CorporationEmbeddable();
//        corporationEmbeddable.setId("test");
//        corporationEmbeddable.setName("name");
//        workshopEmbeddable.setCorporationEmbeddable(corporationEmbeddable);
//        Jmongo.uow().registerDirty(query).commit();
//        System.out.println(query);
//        final MongoUnitOfWork uow = Jmongo.uow();
//        final Test test1 = new Test();
//        test1.setStrings(null);
//        uow.registerNew(test1).commit();
//        final Test sfafdafasfasf = Jmongo.findById(Test.class, "5c7d8d7f7e4eaa47a659cb15").orElse(null);
//        System.out.println(sfafdafasfasf);

//        readTest();
//        writeTest();

//        final List<String> test = ReactiveStreams.of("test").map(it -> {
//            if (1 == 1) {
////                throw new RuntimeException();
//            }
//            return it;
//        }).toList().run().whenComplete((v, e) -> {
//            System.out.println(e);
//        }).toCompletableFuture().get();
//        System.out.println(test);
        TimeUnit.DAYS.sleep(1);
    }

    private static void writeTest() {
        final SilkCarRecord silkCarRecord = Jmongo.findById(SilkCarRecord.class, "5bfde010d939c40001934416").get();
        final SilkCar silkCar = silkCarRecord.getSilkCar();

//        final SilkCar silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df").get();
        silkCar.setCol(6);
        silkCar.setDoffingType(DoffingType.AUTO);
        MongoUnitOfWork uow = Jmongo.uow();
        uow.registerDirty(silkCar).commit();

        uow = Jmongo.uow();
        final Test test = new Test();
        test.setId("5c78ba5264279801e82ce828");
//        test.setStrings(Lists.newArrayList("test1", "test2"));
        test.setStrings(Lists.newArrayList());
        uow.registerDirty(test);
        uow.commit();
    }

    private static void readTest() {
        final Optional<SilkCar> silkCar = Jmongo.findById(SilkCar.class, "5bfd4b4f67e7ad00013055df");
        ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<Silk> silk = Jmongo.findById(Silk.class, "5bfde010d939c400019343eb");
        objectNode = silk.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<SilkCarRecord> silkCarRecord = Jmongo.findById(SilkCarRecord.class, "5bfde010d939c40001934416");
        objectNode = silkCarRecord.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);
    }

}
