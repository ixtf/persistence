package proxy;

import cache.CacheTest;
import com.github.ixtf.persistence.IEntity;
import com.github.ixtf.persistence.mongo.Jmongo;
import lombok.Data;
import lombok.ToString;
import orm.JmongoDev;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.lang.reflect.InvocationTargetException;

/**
 * @author jzb 2019-11-25
 */
public class ByteBuddyTest {
    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        final Class<? extends Test> TestProxyClass = new ByteBuddy().subclass(Test.class)
//                .defineField("document", Document.class)
//                .method(named("getName")).intercept(MethodDelegation.to(GetIdInterceptor.class))
//                .make()
//                .load(ByteBuddyTest.class.getClassLoader())
//                .getLoaded();
//
//        final Document document = Mono.from(jmongo.collection("T_Test").find(eq(ID_COL, "1"))).block();
//        final Test test = TestProxyClass.getDeclaredConstructor().newInstance();
//        System.out.println(test.getId());
//
//        final Class<?> loaded = new ByteBuddy().subclass(Object.class)
//                .name("example.Type")
//                .make()
//                .load(ByteBuddyTest.class.getClassLoader())
//                .getLoaded();
//        final Object o = loaded.getDeclaredConstructor().newInstance();
//        System.out.println(o.getClass().getName());

//        TypePool typePool = TypePool.Default.ofSystemLoader();
//        final Class<?> testClass = new ByteBuddy()
//                .redefine(typePool.describe("cache.CacheTest$Test").resolve(), // do not use 'Bar.class'
//                        ClassFileLocator.ForClassLoader.ofSystemLoader())
//                .defineField("document", Document.class)
//                .make()
//                .load(ClassLoader.getSystemClassLoader())
//                .getLoaded();
//        System.out.println(testClass);

//        ByteBuddyAgent.install();
//        final Class<? extends CacheTest.Test> testClass = new ByteBuddy().redefine(CacheTest.Test.class)
//                .defineField("document", Document.class)
//                .name(CacheTest.Test.class.getName())
//                .make()
//                .load(ByteBuddyTest.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent())
//                .getLoaded();
//        final CacheTest.Test test = jmongo.find(testClass, "1").block();
//        System.out.println(test);
    }

    @ToString(onlyExplicitlyIncluded = true)
    @Data
    @Cacheable
    @Entity
    public static abstract class Test implements IEntity {
        @ToString.Include
        private String name;
        @ManyToOne
        private CacheTest.Test ref;
    }

    public static class GetIdInterceptor {
//        public String getId(@SuperCall Callable<String> zuper)
//                throws Exception {
//            try {
//                return zuper.call();
//            } finally {
//                System.out.println("Returned from database");
//            }
//        }
//
//        public String getName(@SuperCall Callable<String> zuper)
//                throws Exception {
//            try {
//                return zuper.call();
//            } finally {
//                System.out.println("Returned from database");
//            }
//        }
    }
}
