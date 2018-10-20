/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.util.Comparator;

/**
 *
 * Comparator with use friendly methods to compare items
 * @author laim0nas100
 */
public interface ExtComparator<T> extends Comparator<T> {

    /**
     *
     * @param o1
     * @param o2
     * @return think of it like: o1 &lt o2
     */
    public default boolean lessThan(T o1, T o2) {
        return this.compare(o1, o2) < 0;
    }

    /**
     *
     * @param o1
     * @param o2
     * @return think of it like: o1 &gt o2
     */
    public default boolean greaterThan(T o1, T o2) {
        return this.compare(o1, o2) > 0;
    }
    

    /**
     * Create ExtComparator using Comparator 
     * @param <F>
     * @param cmp
     * @return 
     */
    public static <F> ExtComparator<F> of(Comparator<F> cmp) {
        return (F o1, F o2) -> cmp.compare(o1, o2);
    }

    
    /**
     * Create ExtComparator using known Comparable class as basis of order
     * @param <F>
     * @return 
     */
    public static <F extends Comparable> ExtComparator<F> ofComparable() {
        return (F o1, F o2) -> {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return -o2.compareTo(o1);
            }

            return o1.compareTo(o2);
        };

    }
    
    /**
     * Create comparable object using this as basis for order
     * @param obj
     * @return 
     */
    public default Comparable<T> asComparable(T obj){
        return (T o) -> this.compare(obj, o);
    }

}
