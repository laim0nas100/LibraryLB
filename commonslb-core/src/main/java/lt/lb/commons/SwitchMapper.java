package lt.lb.commons;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class SwitchMapper<T , V, M extends SwitchMapper<T, V, M>> {

    protected final Map<T, Supplier<V>> mapping;
    protected Supplier<V> defaultCase = () -> null;

    public SwitchMapper(Map<T, Supplier<V>> mapping) {
        this.mapping = mapping;
    }
    
    public abstract M me();

    public Optional<V> toVal(T en) {
        return Optional
                .ofNullable(mapping.getOrDefault(en, null))
                .map(m -> m.get());
    }

    public Optional<V> toMaybe(T en) {
        return SafeOpt.ofNullable(en)
                .map(mapping::get)
                .orSafe(() -> SafeOpt.ofNullable(defaultCase))
                .map(m -> m.get()).asOptional();
    }

    public Optional<T> toKey(V val) {
        return F.find(mapping, (key, v) -> {
            return Objects.equals(v.get(), val);
        }).map(m -> m.getG1());
    }

    public M with(T e, V val) {
        return with(e, () -> val);
    }

    public M with(T e, Supplier<V> val) {
        M me = me();
        me.mapping.put(e, val);
        return me;
    }

    public M withDefaultCase(Supplier<V> val) {
        M me = me();
        me.defaultCase = val;
        return me;
    }

    public M withDefaultCase(V val) {
        return withDefaultCase(() -> val);
    }
}
