package lt.lb.commons.containers.caching;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lt.lb.commons.FastIDGen;
import lt.lb.commons.FastIDGen.FastID;
import lt.lb.commons.containers.values.Value;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {
    //should be good for a really long time

    public static FastIDGen counter = new FastIDGen(8);

    protected Value<FastID> loaded = new Value<>();
    protected Deque<Condition> conditions = new ArrayDeque<>();
    protected Supplier<T> supply;
    protected Deque<Supplier> dependants;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
        conditions.add(() -> {
            FastID val = loaded.get();
            FastID c = counter.get();
            return FastID.compare(val, c)<0;
//            return (c > val && c == Math.max(val, c));//ensure counter always the max
        });

    }

    public LazyValue(T value) {
        this(() -> value);
    }

    public void addContinion(Condition con) {
        conditions.add(con);
    }

    public void addDependency(Supplier dep) {
        if (dependants == null) {
            dependants = new ArrayDeque<>(1);
        }
        dependants.add(dep);
    }

    /**
     * Set value explicitly, update time
     *
     * @param val
     */
    @Override
    public void set(T val) {
        super.set(val);
        loaded.set(counter.getAndIncrement());
    }

    /**
     * Get computed value or init computation and wait for it
     *
     * @return
     */
    @Override
    public T get() {
        if (!isLoaded()) {
            return syncGet();
        }
        return super.get();
    }

    /**
     * check wether this LazyValue is loaded based on given conditions (must
     * satisfy all of them)
     *
     * @return
     */
    public boolean isLoaded() {
        for (Condition check : conditions) {
            if (check.isFalse()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sync dependencies, if there are any.
     */
    public void syncDependencies() {
        if (dependants == null) {
            return;
        }
        for (Supplier sup : dependants) {
            sup.get();
        }
    }

    /**
     *
     * @return the ID of this value that was set, returned by internal call counter
     */
    public FastID getLoaded() {
        return loaded.get();
    }

    protected synchronized T syncGet() {
        if (!isLoaded()) {
            syncDependencies();
            return super.setAndGet(supply);
        }

        return super.get();
    }

    /**
     * Invalidates value (needs recomputing)
     */
    public void invalidate() {
        loaded.set(null);
    }

}
