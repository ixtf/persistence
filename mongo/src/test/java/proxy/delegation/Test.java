package proxy.delegation;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author jzb 2019-11-25
 */
public class Test {
    public static void main(String[] args) {
        new Test().testProxy();
    }

    @SneakyThrows
    private void testProxy() {
        final Class<? extends Foo> fooProxy = new ByteBuddy()
                .subclass(Foo.class)
                .method(named("sayHelloFoo")
                        .and(isDeclaredBy(Foo.class)
                                .and(returns(String.class))))
                .intercept(MethodDelegation.to(Bar.class))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();
        final Foo foo = fooProxy.getDeclaredConstructor().newInstance();
        String r = foo.sayHelloFoo();
        System.out.println(r);
    }


}

