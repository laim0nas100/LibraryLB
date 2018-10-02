/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.containers.Tuple;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.interfaces.Equator.HashEquator;
import lt.lb.commons.interfaces.Iter;
import lt.lb.commons.interfaces.Iter.IterMap;
import lt.lb.commons.interfaces.Iter.IterMapNoStop;
import lt.lb.commons.threads.Promise;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * @author laim0nas100
 */
public class F {

    public static <T, V> Predicate<T> castPredicate(Predicate<V> predicate) {
        return (T t) -> {
            return predicate.test(F.cast(t));
        };
    }

    public static void unsafeRun(UnsafeRunnable r) {
        try {
            r.unsafeRun();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unsafeRunWithHandler(Consumer<Exception> cons, UnsafeRunnable run) {
        try {
            run.unsafeRun();
        } catch (Exception e) {
            cons.accept(e);
        }
    }

    public static <T extends E, E> T cast(E ob) throws ClassCastException {
        return (T) ob;
    }

    public static <T, K extends T> void addCast(Collection<T> from, Collection<K> to) {
        for (T t : from) {
            to.add((K) t);
        }
    }

    public static boolean instanceOf(Object ob, Class... cls) {
        if (ob == null) {
            return false;
        }
        Class obClass = ob.getClass();
        return instanceOf(obClass, cls);
    }

    public static boolean instanceOf(Class obClass, Class... cls) {
        if (obClass == null) {
            return false;
        }
        return ArrayOp.any(c -> c.isAssignableFrom(obClass), cls);
    }

    public static <T> void merge(List<T> l1, List<T> l2, List<T> addTo, Comparator<T> cmp) {
        Iterator<T> i1 = l1.iterator();
        Iterator<T> i2 = l2.iterator();
        Integer c = null;
        T o1 = null;
        T o2 = null;
        while (i1.hasNext() || i2.hasNext()) {

            if (!i1.hasNext()) {
                addTo.add(i2.next());
            } else if (!i2.hasNext()) {
                addTo.add(i1.next());
            } else {
                if (c == null) {
                    o1 = i1.next();
                    o2 = i2.next();
                } else {
                    if (c > 0) {//added o2
                        o2 = i2.next();
                    } else {
                        o1 = i1.next();
                    }
                }
                c = cmp.compare(o1, o2);
                if (c > 0) {
                    addTo.add(o2);
                } else {
                    addTo.add(o1);
                }

            }
        }

    }

    public static double lerp(double start, double end, double percent) {
        return start + percent * (end - start);
    }

    public static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void swap(List arr, int i, int j) {
        arr.set(i, arr.set(j, arr.get(i)));
    }

    public static double sigmoid(final double x) {
        return 2.0 / (1.0 + Math.exp(-4.9 * x)) - 1.0;
//        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static int StringNumCompare(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == len2) {
            return s1.compareTo(s2);
        }
        return len1 - len2;
    }

    public static <T> List<T> filterParallel(Collection<T> col, Predicate<T> pred, Executor exe) {
        int size = col.size();
        boolean[] satisfied = new boolean[size];

        AtomicInteger satisfiedCount = new AtomicInteger(0);
        ArrayDeque<Promise> deque = new ArrayDeque<>(size);
        F.iterate(col, (i, item) -> {
              Promise<Void> prom = new Promise(() -> {
                  boolean test = pred.test(item);
                  satisfied[i] = test;
                  if (test) {
                      satisfiedCount.incrementAndGet();
                  }

              }).collect(deque).execute(exe);
          });

        Promise waiter = new Promise().waitFor(deque);

        F.unsafeRun(() -> {
            waiter.get();
        });
        if (size > satisfiedCount.get()) {
            return removeByConditionIndex(col, satisfied);
        }
        return new ArrayList<>();

    }

    private static <T> List<T> removeByConditionIndex(Collection<T> col, boolean[] satisfied) {
        ArrayList<T> removed = new ArrayList<>();
        if (col instanceof RandomAccess) { // rewrite
            ArrayList<T> kept = new ArrayList<>();
            F.iterate(col, (i, item) -> {
                  if (satisfied[i]) {
                      kept.add(item);
                  } else {
                      removed.add(item);
                  }
              });
            col.clear();
            col.addAll(kept);

        } else {
            Iterator<T> iterator = col.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                T next = iterator.next();
                if (!satisfied[i]) {
                    iterator.remove();
                    removed.add(next);
                }
                i++;
            }
        }
        return removed;
    }

    /**
     *
     * @param <T> type
     * @param col collection to be modified
     * @param equator equality condition with hashing, so we can use
     * LinkedHashMap
     * @return all removed elements
     */
    public static <T> List<T> filterDistinct(Collection<T> col, HashEquator<T> equator) {
        if (col instanceof RandomAccess) {
            return filterDistinctRewrite(col, equator);
        }

        LinkedHashMap<Object, T> kept = new LinkedHashMap<>();
        LinkedList<T> removed = new LinkedList<>();
        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            Object hash = equator.getHashable(next);
            if (kept.containsKey(hash)) {
                removed.add(next);
                iterator.remove();
            } else {
                kept.put(hash, next);
            }
        }

        return removed;
    }

    /**
     *
     * @param <T> type
     * @param col collection to be modified
     * @param equator equality condition
     * @return all removed elements
     */
    public static <T> List<T> filterDistinct(Collection<T> col, Equator<T> equator) {

        if (col instanceof RandomAccess) {
            return filterDistinctRewrite(col, equator);
        }
        LinkedList<T> kept = new LinkedList<>();
        LinkedList<T> removed = new LinkedList<>();
        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            Optional<Tuple<Integer, T>> find = F.find(kept, (i, item) -> {
                                                  return equator.equals(next, item);
                                              });
            if (find.isPresent()) {
                removed.add(next);
                iterator.remove();
            } else {
                kept.add(next);
            }
        }

        return removed;
    }

    /**
     *
     * @param <T> type
     * @param col collection where removing elements in the middle is expensive,
     * collection is simply rewritten
     * @param equator equality condition
     * @return all removed elements
     */
    public static <T> List filterDistinctRewrite(Collection<T> col, Equator<T> equator) {
        LinkedList<T> kept = new LinkedList<>();
        LinkedList<T> removed = new LinkedList<>();

        Iterator<T> iterator = col.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            Optional<Tuple<Integer, T>> find = F.find(kept, (i, item) -> {
                                                  return equator.equals(next, item);
                                              });
            if (find.isPresent()) {
                removed.add(next);
            } else {
                kept.add(next);
            }
        }

        col.clear();
        col.addAll(kept);
        return removed;
    }

    /**
     *
     * @param <T> type
     * @param col collection where removing elements in the middle is expensive,
     * collection is simply rewritten
     * @param equator equality condition with hashing, so we can use
     * LinkedHashMap
     * @return all removed elements
     */
    public static <T> List filterDistinctRewrite(Collection<T> col, HashEquator<T> equator) {
        LinkedHashMap<Object, T> kept = new LinkedHashMap<>();
        LinkedList<T> removed = new LinkedList<>();

        F.find(col, (i, next) -> (boolean) equator.equals(next, next));

        F.iterate(col, (i, next) -> {
              Object hash = equator.getHashable(next);
              if (kept.containsKey(hash)) {
                  removed.add(next);
              } else {
                  kept.put(hash, next);
              }
          });

        col.clear();
        col.addAll(kept.values());
        return removed;
    }

    public static <K, V> Optional<Tuple<K, V>> find(Map<K, V> map, IterMap<K, V> iter) {
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        for (Map.Entry<K, V> entry : entrySet) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (iter.visit(k, v)) {
                return Optional.of(new Tuple<>(k, v));
            }
        }
        return Optional.empty();
    }

    public static <K, V> Optional<Tuple<K, V>> iterate(Map<K, V> map, IterMapNoStop<K, V> iter) {
        return find(map, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(List<T> list, Integer from, Iter<T> iter) {
        ListIterator<T> iterator = list.listIterator(list.size());
        int index = list.size() - 1;
        while (iterator.hasPrevious()) {
            T next = iterator.previous();
            if (index <= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
            index--;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Iter.IterNoStop<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(List<T> list, Iter<T> iter) {
        return findBackwards(list, list.size(), iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(List<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(list, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Integer from, Iter<T> iter) {
        from = Math.max(from, array.length - 1);
        for (int i = from; i >= 0; i--) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> findBackwards(T[] array, Iter<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Iter.IterNoStop<T> iter) {
        return findBackwards(array, array.length - 1, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterateBackwards(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return findBackwards(array, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Integer from, Iter<T> iter) {
        Iterator<T> iterator = list.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (index >= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
            index++;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Integer from, Iter<T> iter) {
        for (int i = from; i < array.length; i++) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> find(T[] array, Iter<T> iter) {
        return find(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> find(Collection<T> list, Iter<T> iter) {
        return find(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Iter.IterNoStop<T> iter) {
        return find(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Iter.IterNoStop<T> iter) {
        return find(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return find(array, from, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return find(list, from, iter);
    }

}
