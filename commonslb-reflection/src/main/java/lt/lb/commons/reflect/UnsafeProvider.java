package lt.lb.commons.reflect;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.Checked;
import sun.misc.Unsafe;

/**
 *
 * Utility to get Unsafe class
 *
 * @author laim0nas100
 */
public class UnsafeProvider {

    private static Unsafe THE_UNSAFE = null;

    public static Unsafe getUnsafe() {
        if (THE_UNSAFE == null) {
            Checked.uncheckedRun(() -> {

                Constructor<Unsafe> declaredConstructor = Unsafe.class.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                THE_UNSAFE = declaredConstructor.newInstance();

            });
        }
        return THE_UNSAFE;

    }

    public static <T> Function<Class, T> getUnsafeAllocator() {
        return cls -> Checked.uncheckedCall(() -> F.cast(getUnsafe().allocateInstance(cls)));
    }
}
