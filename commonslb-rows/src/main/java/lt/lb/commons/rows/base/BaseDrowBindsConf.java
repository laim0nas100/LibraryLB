package lt.lb.commons.rows.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lt.lb.commons.iteration.For;
import lt.lb.commons.rows.Drow;
import lt.lb.commons.rows.DrowConf;
import lt.lb.commons.rows.Drows;
import lt.lb.commons.rows.OrderedRunnable;
import lt.lb.commons.rows.UpdateHolder;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseDrowBindsConf<R extends Drow, C, N, L, U extends Updates, Conf extends BaseDrowBindsConf> implements DrowConf<R, C, N, L, U>, UpdateHolder<U, Conf, R> {

    protected Map<String, U> updateMap = new HashMap<>();

    protected List<UnresolvedConsumer<R>> unresolved = new ArrayList<>();

    @Override
    public Map<String, U> getUpdateMap() {
        return updateMap;
    }

    protected Optional<R> confupdatesFor = Optional.empty();

    @Override
    public void configureUpdates(Map<String, U> updates, R object) {
        object.initUpdates();
        confupdatesFor = Optional.of(object);
        For.entries().iterate(updateMap, (type, up) -> {
            List<OrderedRunnable> updateListeners = up.getUpdateListeners();
            for (OrderedRunnable run : updateListeners) {
                object.withUpdate(type, run);
            }

        });
        for (UnresolvedConsumer<R> consumer : unresolved) {
            object.withUpdate(consumer.type, new OrderedRunnable(consumer.order, () -> consumer.cons.accept(object)));
        }
        confupdatesFor = Optional.empty();
    }

    @Override
    public U ensureUpdate(String type) {
        final R current = confupdatesFor.get();
        return getUpdateMap().computeIfAbsent(type, k -> createUpdates(type, current));
    }

    @Override
    public Conf withUpdate(String type, int order, Consumer<R> run) {
        unresolved.add(new UnresolvedConsumer<>(type, order, run));
        return me();
    }

    public static class UnresolvedConsumer<R> {

        public Consumer<R> cons;
        public String type;
        public int order;

        public UnresolvedConsumer(String type, int order, Consumer<R> cons) {
            this.cons = cons;
            this.type = type;
            this.order = order;
        }

    }

    /**
     * Do some base checks, derender line if was rendered, set new last visible
     * index if index was changed
     *
     * @param line
     * @param rowIndex
     * @param dirty
     * @return whether to continue rendering process
     */
    protected boolean baseDerenderContinue(BaseLine line, int rowIndex, boolean dirty) {
        int lastIndex = line.getLastVisibleIndex();
        if (lastIndex < 0 && rowIndex < 0) { // was not redered
            return false;
        }

        line.setLastVisibleIndex(rowIndex);
        if (lastIndex >= 0 && rowIndex < 0) {
            //not visible anymore
            line.derender();
            return false;
        } else {
            boolean rerender = (dirty || lastIndex != rowIndex);
            if (rerender) {
                line.derender();
            }
            return rerender;
        }

    }

}
