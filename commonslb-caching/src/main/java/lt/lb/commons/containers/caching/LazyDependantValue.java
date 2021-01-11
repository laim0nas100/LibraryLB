package lt.lb.commons.containers.caching;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 *
 * LazyValue, but with dependant connections
 */
public class LazyDependantValue<T> extends LazyValue<T> {

    public LazyDependantValue(Supplier<? extends T> supply) {
        super(supply);
    }

    public LazyDependantValue(T value) {
        super(value);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one
     *
     * @param <U>
     * @param <L>
     * @param child
     * @return
     */
    public <U, L extends LazyValue<U>> L createDependantChild(L child) {
        return createDependantChild(child, false);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one with parameter which determines whether parent
     * is being called in in child's supplier, so that no repeated parent call
     * must be made
     *
     * @param <U>
     * @param <L>
     * @param child
     * @param addParentInSupply wether child gets this value as an explicit
     * dependency
     * @return
     */
    public <U, L extends LazyValue<U>> L createDependantChild(L child, boolean addParentInSupply) {
        LazyDependantValue<T> me = this;
        child.addCondition(now -> me.isLoadedBefore(now)&& me.getLoaded() <= child.getLoaded());

        if (addParentInSupply) {
            child.addDependency(me);
        }
        return child;
    }

    /**
     * Create explicit mapping that updates child when parent is updated
     *
     * @param <U>
     * @param mapper
     * @return
     */
    public <U> LazyDependantValue<U> map(Function<? super T, ? extends U> mapper) {
        LazyDependantValue<T> me = this;
        LazyDependantValue<U> lazyDepVal = new LazyDependantValue<>(() -> mapper.apply(me.get()));
        return createDependantChild(lazyDepVal, false);
    }

}
