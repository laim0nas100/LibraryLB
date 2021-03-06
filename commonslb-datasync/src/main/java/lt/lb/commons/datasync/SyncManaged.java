package lt.lb.commons.datasync;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public interface SyncManaged<M> extends ValueProxy<M> {

    /**
     * Set managed value
     *
     * @param managed
     */
    public void setManaged(M managed);

    /**
     * Get managed value
     *
     * @return
     */
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
