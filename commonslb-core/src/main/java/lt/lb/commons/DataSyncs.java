package lt.lb.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.containers.values.SetOnce;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class DataSyncs {

    public static interface Valid<M> {

        public boolean isValid(M from);

        public default boolean isInvalid(M from) {
            return !isValid(from);
        }

        public void showInvalidation(M from);

        public void clearInvalidation(M from);
    }

    public static abstract class NodeValid<T, N> implements Valid<T> {

        public Supplier<List<N>> referenceSupl;
        public Function<? super T, String> errorSupl;
        public Predicate<T> isValid;

        @Override
        public boolean isValid(T from) {
            return isValid.test(from);
        }

    }

    public static interface SyncValidation<M, V extends Valid<M>>
            extends DisplayValidation<M, V>, PersistValidation<M, V> {

    }

    public static interface DisplayValidation<M, V extends Valid<M>> {

        /**
         * Add a display validation strategy
         * @param validation 
         */
        public void withDisplayValidation(V validation);

        /**
         * If managed value can be displayed, don't fire validation
         *
         * @return
         */
        public boolean validDisplay();

        /**
         * If managed value can be displayed, fire EVERY validation
         *
         * @return
         */
        public boolean validDisplayFull();

        /**
         * If managed value can NOT be displayed, fire FIRST validation
         * @return 
         */
        public default boolean invalidDisplay() {
            return !validDisplay();
        }

        /**
         * If managed value can NOT be displayed, fire EVERY validation
         *
         * @return
         */
        public default boolean invalidDisplayFull() {
            return !validDisplayFull();
        }

        /**
         * Check only conditions, don't actually show anything
         *
         * @param from
         * @return
         */
        public boolean isValidDisplay(M from);

        /**
         * Check only conditions, don't actually show anything
         *
         * @param from
         * @return
         */
        public default boolean isInvalidDisplay(M from) {
            return !isValidDisplay(from);
        }

        /**
         * Clear validation
         *
         * @param from
         * @return
         */
        public void clearInvalidationDisplay(M from);

    }

    public static interface PersistValidation<M, V extends Valid<M>> {

        public void withPersistValidation(V validation);

        /**
         * If managed value can be persisted
         *
         * @return
         */
        public boolean validPersist();

        /**
         * If managed value can be persisted, fire every validation
         *
         * @return
         */
        public boolean validPersistFull();

        public default boolean invalidPersist() {
            return !validPersist();
        }

        public default boolean invalidPersistFull() {
            return !validPersistFull();
        }

        public boolean isValidPersist(M from);

        public default boolean isInvalidPersist(M from) {
            return !isValidPersist(from);
        }

        public void clearInvalidationPersist(M from);

    }

    public static interface SyncManaged<M> extends ValueProxy<M> {

        public void setManaged(M managed);

        public M getManaged();

        @Override
        public default void set(M v) {
            setManaged(v);
        }

        @Override
        public default M get() {
            return getManaged();
        }

    }

    public static interface DataSyncPersist<P, M> extends SyncManaged<M> {

        public void withPersistSync(Consumer<? super P> persSync);

        public void withPersistSup(Supplier<? extends P> persistSup);

        public void withPersistGet(Function<? super P, ? extends M> func);

        public void withPersistSet(Function<? super M, ? extends P> func);

        public void syncPersist();

        public void syncManagedFromPersist();
    }

    public static interface DataSyncDisplay<D, M> extends SyncManaged<M> {

        public void withDisplaySync(Consumer<? super D> displaySync);

        public void withDisplaySup(Supplier<? extends D> displaySup);

        public void withDisplayGet(Function<? super D, ? extends M> func);

        public void withDisplaySet(Function<? super M, ? extends D> func);

        public void syncDisplay();

        public void syncManagedFromDisplay();
    }

    public static interface DataSyncManaged<P, M, D> extends DataSyncPersist<P, M>, DataSyncDisplay<D, M> {

    }

    public static interface DataSync<P, D> extends DataSyncManaged<P, P, D> {

    }

    public static interface DataSyncManagedValidation<P, M, D, V extends Valid<M>> extends DataSyncManaged<P, M, D>, SyncValidation<M, V> {

    }

    public static interface DataSyncValidation<P, D, V extends Valid<P>> extends DataSync<P, D>, SyncValidation<P, V> {

    }

    public static class UnmanagedValidation<V extends Valid<Object>> extends BaseValidation<Object, V> {

        @Override
        public Object getManaged() {
            return null;
        }

    }

    public static abstract class BaseValidation<M, V extends Valid<M>> implements SyncValidation<M, V> {

        protected List<V> validateDisplay = new ArrayList<>(0);
        protected List<V> validatePersistence = new ArrayList<>(0);

        public abstract M getManaged();

        @Override
        public void withDisplayValidation(V validation) {
            this.validateDisplay.add(validation);
        }

        @Override
        public void withPersistValidation(V validation) {
            this.validatePersistence.add(validation);
        }

        @Override
        public boolean validDisplay() {
            return doValidation(validateDisplay, false, getManaged());
        }

        @Override
        public boolean validDisplayFull() {
            return doValidation(validateDisplay, true, getManaged());
        }

        @Override
        public boolean validPersist() {
            return doValidation(validatePersistence, false, getManaged());
        }

        @Override
        public boolean validPersistFull() {
            return doValidation(validatePersistence, true, getManaged());
        }

        @Override
        public boolean isValidDisplay(M from) {
            return checkValidation(validateDisplay, from);
        }

        @Override
        public void clearInvalidationDisplay(M from) {
            clearValidation(validateDisplay, from);
        }

        @Override
        public boolean isValidPersist(M from) {
            return checkValidation(validatePersistence, from);
        }

        @Override
        public void clearInvalidationPersist(M from) {
            clearValidation(validatePersistence, from);
        }

        public static <T> boolean iterateFindFirst(Iterable<T> list, boolean full, Predicate<T> satisfied) {

            if (full) {
                BooleanValue found = BooleanValue.FALSE();
                F.iterate(list, (i, item) -> found.setOr(satisfied.test(item)));
                return found.get();
            } else {
                return F.find(list, (i, item) -> satisfied.test(item)).isPresent();
            }
        }

        public static <T extends Valid<M>, M> void clearValidation(List<T> list, M managed) {
            iterateFindFirst(list, true, c -> {
                c.clearInvalidation(managed);
                return false;
            });
        }

        public static <T extends Valid<M>, M> boolean checkValidation(List<T> list, M managed) {
            return !iterateFindFirst(list, false, c -> c.isInvalid(managed));//find first invalid
        }

        public static <T extends Valid<M>, M> boolean doValidation(List<T> list, boolean full, M managed) {
            boolean invalid = iterateFindFirst(list, full, val -> {
                val.clearInvalidation(managed);
                if (val.isInvalid(managed)) {
                    val.showInvalidation(managed);
                    return true;
                }
                return false;

            });
            return !invalid;
        }

    }

    public static abstract class ExplicitDataSync<P, M, D, V extends Valid<M>> extends BaseValidation<M, V> implements DataSyncManagedValidation<P, M, D, V> {

        protected SetOnce<Supplier<? extends D>> displaySupp = new SetOnce<>();
        protected SetOnce<Consumer<? super D>> displaySync = new SetOnce<>();
        protected SetOnce<Supplier<? extends P>> persistenceSupp = new SetOnce<>();
        protected SetOnce<Consumer<? super P>> persistenceSync = new SetOnce<>();
        protected SetOnce<Function<? super P, ? extends M>> persistGet = new SetOnce<>();
        protected SetOnce<Function<? super M, ? extends P>> persistSet = new SetOnce<>();
        protected SetOnce<Function<? super D, ? extends M>> displayGet = new SetOnce<>();
        protected SetOnce<Function<? super M, ? extends D>> displaySet = new SetOnce<>();

        protected M managed;

        /**
         * Gateway to put data formatted for display layer
         *
         * @param displaySync
         */
        @Override
        public void withDisplaySync(Consumer<? super D> displaySync) {
            this.displaySync.set(displaySync);
        }

        /**
         * Gateway to put data formatted for persistence layer
         *
         * @param persSync
         */
        @Override
        public void withPersistSync(Consumer<? super P> persSync) {
            this.persistenceSync.set(persSync);
        }

        public void withPersistProxy(ValueProxy<P> proxy) {
            this.persistenceSupp.set(proxy);
            this.persistenceSync.set(proxy);
        }

        public void withDisplayProxy(ValueProxy<D> proxy) {
            this.displaySupp.set(proxy);
            this.displaySync.set(proxy);
        }

        /**
         * A gateway to extract data from display layer
         *
         * @param displaySup
         */
        @Override
        public void withDisplaySup(Supplier<? extends D> displaySup) {
            this.displaySupp.set(displaySup);
        }

        /**
         * A gateway to extract data from persistence layer
         *
         * @param persistSup
         */
        @Override
        public void withPersistSup(Supplier<? extends P> persistSup) {
            this.persistenceSupp.set(persistSup);
        }

        /**
         * Adapter to convert data from persistence layer to managed
         *
         * @param func
         */
        @Override
        public void withPersistGet(Function<? super P, ? extends M> func) {
            this.persistGet.set(func);
        }

        /**
         * Adapter to convert data from display to managed
         *
         * @param func
         */
        @Override
        public void withDisplayGet(Function<? super D, ? extends M> func) {
            this.displayGet.set(func);
        }

        /**
         * Adapter to convert data from managed to persistence layer
         *
         * @param func
         */
        @Override
        public void withPersistSet(Function<? super M, ? extends P> func) {
            this.persistSet.set(func);
        }

        /**
         * Adapter to convert data from managed to display layer
         *
         * @param func
         */
        @Override
        public void withDisplaySet(Function<? super M, ? extends D> func) {
            this.displaySet.set(func);
        }

        /**
         * Set managed value
         *
         * @param managed
         */
        @Override
        public void setManaged(M managed) {
            this.managed = managed;
        }

        /**
         * Get managed value
         *
         * @return
         */
        @Override
        public M getManaged() {
            return managed;
        }

        /**
         * Format the managed value and sync to the persistence gateway
         */
        @Override
        public void syncPersist() {
            if (this.persistSet.isNotNull() && this.persistenceSync.isNotNull()) {
                Function<? super M, ? extends P> toPersist = this.persistSet.get();
                P newPersist = toPersist.apply(this.getManaged());
                this.persistenceSync.get().accept(newPersist);
            } else {
                //explicitly launch to throw exceptions
                this.persistSet.get();
                this.persistenceSync.get();
            }
        }

        /**
         * Format the managed value and sync to the display gateway
         */
        @Override
        public void syncDisplay() {
            if (this.displaySet.isNotNull() && this.displaySync.isNotNull()) {
                Function<? super M, ? extends D> toDisplay = this.displaySet.get();
                D newDisplay = toDisplay.apply(this.getManaged());
                this.displaySync.get().accept(newDisplay);
            } else {
                //explicitly launch to throw exceptions
                this.displaySet.get();
                this.displaySync.get();
            }
        }

        /**
         * Get the value from display layer, format it and set it to managed
         */
        @Override
        public void syncManagedFromDisplay() {
            if (this.displayGet.isNotNull() && this.displaySupp.isNotNull()) {
                Supplier<? extends D> get = this.displaySupp.get();
                D display = get.get();

                Function<? super D, ? extends M> toManaged = this.displayGet.get();
                M newManaged = toManaged.apply(display);
                this.setManaged(newManaged);

            } else {
                //explicitly launch to throw exceptions
                this.displayGet.get();
                this.displaySupp.get();
            }
        }

        /**
         * Get the value form persistence layer, format it and set it to managed
         */
        @Override
        public void syncManagedFromPersist() {
            if (this.persistGet.isNotNull() && this.persistenceSupp.isNotNull()) {
                Supplier<? extends P> get = this.persistenceSupp.get();
                P persist = get.get();

                Function<? super P, ? extends M> toManaged = this.persistGet.get();
                M newManaged = toManaged.apply(persist);
                this.setManaged(newManaged);
            } else {
                //explicitly launch to throw exceptions
                persistGet.get();
                persistenceSupp.get();
            }
        }

        /**
         * Marks that persistence layer is the same as managed, i.e.
         * there's no conversion
         */
        public void withIdentityPersist() {
            this.persistSet.set(v -> F.cast(v));
            this.persistGet.set(v -> F.cast(v));
        }

        /**
         * Marks that display layer is the same as managed, i.e. there's
         * no conversion
         */
        public void withIdentityDisplay() {
            this.displaySet.set(v -> F.cast(v));
            this.displayGet.set(v -> F.cast(v));
        }

        /**
         * Marks display and persistence layers the same type as managed, i.e.
         * there's no conversion
         */
        public void withNoConversion() {
            withIdentityDisplay();
            withIdentityPersist();
        }

    }

    public static abstract class GenDataSync<P, D, V extends Valid<P>> extends ExplicitDataSync<P, P, D, V> {

        public GenDataSync() {
        }

    }

    public static abstract class NodeSync<P, D, N, V extends NodeValid<P, N>> extends GenDataSync<P, D, V> {

        public NodeSync(N node) {
            this(ReadOnlyIterator.of(node).toArrayList());
        }

        public NodeSync(List<N> nodes) {
            this.nodes = nodes;
        }

        public final List<N> nodes;

        /**
         * Copy validation strategy except the nodes.
         *
         * @param valid
         */
        public void addPersistValidation(V valid) {
            V v = createValidation();
            v.errorSupl = valid.errorSupl;
            v.isValid = valid.isValid;
            v.referenceSupl = () -> nodes;
            withPersistValidation(v);
        }

        /**
         * Create base class, so that we can supply with nodes, error message
         * etc..
         *
         * @return
         */
        protected abstract V createValidation();

        public void addPersistValidation(String error, Predicate<P> isValid) {
            addPersistValidation(m -> error, isValid);
        }

        public void addPersistValidation(Function<? super P, String> error, Predicate<P> isValid) {
            V valid = createValidation();
            valid.errorSupl = error;
            valid.isValid = isValid;
            valid.referenceSupl = () -> nodes;
            withPersistValidation(valid);
        }

    }

}
