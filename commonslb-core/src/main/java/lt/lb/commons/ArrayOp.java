/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.interfaces.Accumulator;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ArrayOp {

    public static <T> T accumulate(Accumulator<T> acc, T... array) {
        if (array.length == 0) {
            return null;
        }
        T total = array[0];
        for (int i = 1; i < array.length; i++) {
            total = acc.accumulate(total, array[i]);
        }
        return total;
    }

    public static <T> boolean any(Predicate<T> test, T... array) {
        for (T t : array) {
            if (test.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static boolean and(Boolean... b) {
        return all(c -> c, b);
    }

    public static boolean or(Boolean... b) {
        return any(c -> c, b);
    }

    public static <T> boolean all(Predicate<T> test, T... array) {
        return !any(test.negate(), array);
    }

    public static <T> void arrayCopy(T[] src, int srcPos, T[] dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static <T> void arrayCopyFullAt(T[] src, T[] dest, int destPos) {
        System.arraycopy(src, 0, dest, destPos, src.length);
    }

    public static <T> T[] merge(T[] one, T... two) {
        int size = one.length + two.length;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        arrayCopyFullAt(one, newArray, 0);
        arrayCopyFullAt(two, newArray, one.length);
        return newArray;

    }

    public static <T> T[] merge(T[] one, T[]... two) {
        int size = one.length;
        for (T[] t : two) {
            size += t.length;
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        int i = one.length;
        arrayCopyFullAt(one, newArray, 0);

        for (T[] t : two) {
            arrayCopyFullAt(t, newArray, i);
            i += t.length;
        }

        return newArray;

    }

    public static <T> T[] castArray(Object[] array, Class<T> clz) {
        T[] a = makeArray(array.length, clz);
        for (int i = 0; i < array.length; i++) {
            a[i] = (T) array[i];
        }
        return a;
    }

    public static <T> T[] makeArray(Integer size, Class<T> clz) {
        if (clz.isPrimitive()) {
            throw new IllegalArgumentException("Primitives, like " + clz.getName() + " are not supported, use makePrimitiveArray()");
        }
        T[] a = (T[]) java.lang.reflect.Array.newInstance(clz, size);
        return a;
    }

    public static Object makePrimitiveArray(Integer size, Class clz) {
        if (clz.isPrimitive()) {
            return java.lang.reflect.Array.newInstance(clz, size);
        } else {
            throw new IllegalArgumentException(clz.getName() + " is not primitive, use makeArray()");
        }
    }

    public static <T> T[] newArray(List<T> list, Class<T> clz) {
        return list.toArray(makeArray(list.size(), clz));
    }

    public static Object[] newArray(List list) {
        return newArray(list, Object.class);
    }

    public static <T> T[] remove(T[] one, T... two) {
        ArrayList<T> list = new ArrayList<>(one.length);
        HashSet<T> set = new HashSet<>();

        for (T t : two) {
            set.add(t);
        }
        for (T t : one) {
            if (!set.contains(t)) {
                list.add(t);
            }

        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);
    }

    public static <T> T[] removeByIndex(T[] one, Integer... two) {
        ArrayList<T> list = new ArrayList<>(one.length);
        HashSet<Integer> set = new HashSet<>();
        for (int index : two) {
            set.add(index);
        }
        int i = 0;
        for (T t : one) {
            if (!set.contains(i)) {
                list.add(t);
            }
            i++;
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);
    }

    public static <T> T[] addAt(T[] one, Integer where, T... what) {
        int size = one.length + what.length;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        ArrayOp.arrayCopy(one, 0, newArray, 0, where); // copy first part
        ArrayOp.arrayCopyFullAt(what, newArray, where);
        ArrayOp.arrayCopy(one, where, newArray, where + what.length, one.length - where);
        return newArray;

    }

    public static <T> T[] removeStrip(T[] one, Integer from, Integer to) {

        int strip = to - from;
        int size = one.length - strip;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        ArrayOp.arrayCopy(one, 0, newArray, 0, from); // copy first part
        ArrayOp.arrayCopy(one, to, newArray, from, size - from);
        return newArray;
    }

    public static <T> int count(Predicate<T> test, T... array) {
        int count = 0;
        for (T t : array) {
            if (test.test(t)) {
                count++;
            }
        }
        return count;
    }

    public static <T> T[] asArray(T... vals) {
        return vals;
    }

    public static <T> T[] replicate(Integer times, T... values) {
        int arraySize = times * values.length;
        Class<T> cls = (Class<T>) values.getClass().getComponentType();

        if (cls.isPrimitive()) {
            throw new IllegalArgumentException("Primitive values are not supported");
        }
        T[] array = makeArray(arraySize, cls);
        for (int i = 0; i < arraySize; i++) {
            int valIndex = i % values.length;
            array[i] = values[valIndex];
        }

        return array;
    }

    public static <T> T[] replicate(Integer times, Supplier<T>... values) {
        int arraySize = times * values.length;
        if (values.length == 0) {
            return null;
        }
        Class<T> cls = (Class<T>) values[0].get().getClass();

        if (cls.isPrimitive()) {
            throw new IllegalArgumentException("Primitive values are not supported");
        }
        T[] array = makeArray(arraySize, cls);
        for (int i = 0; i < arraySize; i++) {
            int valIndex = i % values.length;
            array[i] = values[valIndex].get();
        }

        return array;
    }

}
