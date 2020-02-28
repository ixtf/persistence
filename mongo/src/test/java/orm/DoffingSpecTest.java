package orm;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.MongoUnitOfWork;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import orm.domain.*;
import orm.domain.DoffingSpec.CheckSpec;
import orm.domain.DoffingSpec.LineMachineSpec;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static orm.domain.SilkCarSideType.A;
import static orm.domain.SilkCarSideType.B;

/**
 * @author jzb 2020-02-22
 */
public class DoffingSpecTest {
    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);

    @SneakyThrows
    public static void main(String[] args) {
//        init();
        test();
//        final DoffingSpec doffingSpec = jmongo.find(DoffingSpec.class, "5e50db69bf923711daad21e1").block();
//        System.out.println(doffingSpec);


//        final Field field = DoffingSpec.class.getDeclaredField("lineMachineSpecsSet");
//        final Class<?> type = field.getType();
//        System.out.println(type);
//        final Type genericType = field.getGenericType();
//        System.out.println(genericType);
//        if (genericType instanceof ParameterizedType) {
//            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
//            final Type subType = parameterizedType.getActualTypeArguments()[0];
//            System.out.println(subType);
//        }


    }

    private static void test() {
        final Workshop workshop = jmongo.find(Workshop.class, "5e50b2d9f2f4585f93c24634").block();
        final DoffingSpec doffingSpec = new DoffingSpec();
        doffingSpec.setWorkshop(workshop);
        doffingSpec.setDoffingType(DoffingType.MANUAL);
        doffingSpec.setLineMachineCount(3);
        doffingSpec.setSpindleNum(10);
        doffingSpec.setRow(3);
        doffingSpec.setCol(5);
        doffingSpec.setCheckSpecs(generateCheckSpecs());
        doffingSpec.setLineMachineSpecs(generateLineMachineSpecsSet());
        doffingSpec.checkValid();
        jmongo.uow().registerNew(doffingSpec).commit();
        System.out.println(doffingSpec);
    }

    private static Set<Set<LineMachineSpec>> generateLineMachineSpecsSet() {
        final LineMachineSpec lineMachineSpec1 = new LineMachineSpec();
        lineMachineSpec1.setOrderBy(1);
        lineMachineSpec1.addItem(A, 2, 1, 1);
        lineMachineSpec1.addItem(A, 2, 2, 2);
        lineMachineSpec1.addItem(A, 2, 3, 3);
        lineMachineSpec1.addItem(A, 2, 4, 4);
        lineMachineSpec1.addItem(A, 2, 5, 5);
        lineMachineSpec1.addItem(A, 3, 5, 6);
        lineMachineSpec1.addItem(A, 3, 4, 7);
        lineMachineSpec1.addItem(A, 3, 3, 8);
        lineMachineSpec1.addItem(A, 3, 2, 9);
        lineMachineSpec1.addItem(A, 3, 1, 10);

        final LineMachineSpec lineMachineSpec2 = new LineMachineSpec();
        lineMachineSpec2.setOrderBy(2);
        lineMachineSpec2.addItem(B, 1, 5, 1);
        lineMachineSpec2.addItem(B, 1, 4, 2);
        lineMachineSpec2.addItem(B, 1, 3, 3);
        lineMachineSpec2.addItem(B, 1, 2, 4);
        lineMachineSpec2.addItem(B, 1, 1, 5);
        lineMachineSpec2.addItem(A, 1, 5, 6);
        lineMachineSpec2.addItem(A, 1, 4, 7);
        lineMachineSpec2.addItem(A, 1, 3, 8);
        lineMachineSpec2.addItem(A, 1, 2, 9);
        lineMachineSpec2.addItem(A, 1, 1, 10);

        final LineMachineSpec lineMachineSpec3 = new LineMachineSpec();
        lineMachineSpec3.setOrderBy(3);
        lineMachineSpec3.addItem(B, 1, 5, 1);
        lineMachineSpec3.addItem(B, 1, 4, 2);
        lineMachineSpec3.addItem(B, 1, 3, 3);
        lineMachineSpec3.addItem(B, 1, 2, 4);
        lineMachineSpec3.addItem(B, 1, 1, 5);
        lineMachineSpec3.addItem(B, 2, 1, 6);
        lineMachineSpec3.addItem(B, 2, 2, 7);
        lineMachineSpec3.addItem(B, 2, 3, 8);
        lineMachineSpec3.addItem(B, 2, 4, 9);
        lineMachineSpec3.addItem(B, 2, 5, 10);

        final TreeSet<LineMachineSpec> treeSet = Sets.newTreeSet();
        treeSet.add(lineMachineSpec1);
        treeSet.add(lineMachineSpec2);
        treeSet.add(lineMachineSpec3);
        return Set.of(treeSet);
    }

    private static TreeSet<CheckSpec> generateCheckSpecs() {
        final CheckSpec checkSpec1 = new CheckSpec();
        checkSpec1.setOrderBy(1);
        IntStream.of(2, 3).forEach(row -> {
            Stream.of(1, 2, 4, 5).forEach(col -> {
                checkSpec1.addPosition(A, row, col);
            });
        });

        final CheckSpec checkSpec2 = new CheckSpec();
        checkSpec2.setOrderBy(2);
        IntStream.of(1).forEach(row -> {
            Stream.of(1, 2, 4, 5).forEach(col -> {
                checkSpec2.addPosition(A, row, col);
            });
        });
        IntStream.of(3).forEach(row -> {
            Stream.of(1, 2, 4, 5).forEach(col -> {
                checkSpec2.addPosition(B, row, col);
            });
        });

        final CheckSpec checkSpec3 = new CheckSpec();
        checkSpec3.setOrderBy(3);
        IntStream.of(1, 2).forEach(row -> {
            Stream.of(1, 2, 4, 5).forEach(col -> {
                checkSpec3.addPosition(B, row, col);
            });
        });

        final TreeSet<CheckSpec> ret = Sets.newTreeSet();
        ret.add(checkSpec1);
        ret.add(checkSpec2);
        ret.add(checkSpec3);
        return ret;
    }

    private static void init() {
        final MongoUnitOfWork uow = jmongo.uow();
        final Corporation corporation = new Corporation();
        uow.registerNew(corporation);
        corporation.setCode("TEST");
        corporation.setName("测试");
        final Workshop workshop = new Workshop();
        uow.registerNew(workshop);
        workshop.setCorporation(corporation);
        workshop.setCode("A");
        workshop.setName("A车间");
        final Line line = new Line();
        uow.registerNew(line);
        line.setWorkshop(workshop);
        line.setName("A1");
        line.setDoffingType(DoffingType.AUTO);
        uow.commit();
    }
}
