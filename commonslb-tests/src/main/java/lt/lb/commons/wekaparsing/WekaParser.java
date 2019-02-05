package lt.lb.commons.wekaparsing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.containers.Value;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.wekaparsing.WekaDefaultParsers.WekaTransformer;

/**
 *
 *
 * @author laim0nas100
 */
public class WekaParser<T> {

    protected HashMap<Class, WekaTransformer> printers = new HashMap<>();
    protected Class<T> cls;
    protected String className;

    public WekaParser(Class<T> cls, String className, String dateFormat) {
        printers.put(Boolean.class, WekaDefaultParsers.defaultBoolean);
        printers.put(Number.class, WekaDefaultParsers.defaultDouble);
        printers.put(Integer.class, WekaDefaultParsers.defaultDouble);
        printers.put(Double.class, WekaDefaultParsers.defaultDouble);
        printers.put(Long.class, WekaDefaultParsers.defaultDouble);
        printers.put(Float.class, WekaDefaultParsers.defaultDouble);
        printers.put(String.class, WekaDefaultParsers.defaultString);
        printers.put(Date.class, WekaDefaultParsers.defaultDate(dateFormat));

        this.cls = cls;
        this.className = className;
        F.iterate(cls.getFields(), (i, f) -> {
            if (f.getType().isEnum()) {
                printers.put(f.getType(), WekaDefaultParsers.defaultEnum(F.cast(f.getType())));
            }
        });
    }

    public WekaParser(Class<T> cls, String className) {
        this(cls, className, "yyyy-MM-dd HH:mm:ss");
    }

    protected WekaTransformer ensureWekaPrinter(Class cls) {
        Optional<WekaTransformer> wekaPrinter = resolveWekaPrinter(cls);
        if (wekaPrinter.isPresent()) {
            return wekaPrinter.get();
        }
        throw new IllegalArgumentException("Printer not found for type:" + cls);
    }

    protected Optional<WekaTransformer> resolveWekaPrinter(Class cls) {
        if (printers.containsKey(cls)) {
            return Optional.of(printers.get(cls));
        }
        //try to match derived class
        return F.find(printers, (c, pr) -> {
            return F.instanceOf(cls, c);
        }).map(m -> m.g2);
    }

    protected String wekaAttributePrint(Object attr) {
        return ensureWekaPrinter(attr.getClass()).asString(attr);
    }

    public ArrayList<Field> getFields() {
        Field[] fields = cls.getFields();
        Value<Field> classField = new Value<>();
        ArrayList<Field> arr = new ArrayList<>();
        F.iterate(fields, (i, field) -> {

            if (field.getName().equals(className)) {
                classField.set(field);
            } else {
                arr.add(field);
            }
        });
        ExtComparator<Field> ofFieldName = ExtComparator.ofValue(Field::getName, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(arr, ofFieldName);
        if (classField.get() != null) {
            arr.add(classField.get());
        }
        return arr;
    }

    /**
     *
     * @return attribute part of a .arff file
     */
    public ArrayList<String> wekaReadyAttributes() {
        ArrayList<Field> arr = this.getFields();
        ArrayList<String> str = new ArrayList<>();
        F.iterate(arr, (i, field) -> {
            str.add("@ATTRIBUTE " + field.getName() + " " + ensureWekaPrinter(field.getType()).typeInfo());
        });
        return str;
    }

    /**
     * Create an object form attributes. Order is very important. Override
     * method getFields if you want a specific order.
     *
     * @param attributes
     * @return
     * @throws Exception
     */
    public T objectFromAttributes(List<String> attributes) throws Exception {
        T newInstance = this.cls.newInstance();
        ArrayList<Field> fields = this.getFields();

        if (attributes.size() > fields.size()) {
            throw new IllegalArgumentException("Class field count:" + fields.size() + " attribute count:" + attributes.size());
        }
        F.iterate(attributes, (i, f) -> {
            if (!StringOp.equals("?", f)) { // leave null otherwise
                F.unsafeRun(() -> {
                    Field field = fields.get(i);
                    WekaTransformer wek = this.ensureWekaPrinter(field.getType());
                    field.set(newInstance, wek.asObject(f));
                });
            }
        });
        return newInstance;
    }

    /**
     * tries to split the line by ',' and parse a object from specified
     * attributes.
     *
     * @param line
     * @return
     * @throws Exception
     */
    public T objectFromLine(String line) throws Exception {
        return this.objectFromAttributes(Arrays.asList(line.split(",")));
    }

    /**
     *
     * @param col
     * @return return @Data part of .arff file
     */
    public ArrayList<String> wekaReadyDataLines(Collection<T> col) {
        ArrayList<String> wekaReady = new ArrayList<>(col.size() + 2);

        ArrayList<Field> fields = this.getFields();
        if (fields.isEmpty()) {
            return wekaReady;
        }
        F.iterate(col, (i, item) -> {
            String[] param = new String[fields.size()];
            F.iterate(fields, (j, f) -> {
                F.unsafeRun(() -> {
                    param[j] = wekaAttributePrint(f.get(item));
                });
            });

            String s = param[0];
            for (int k = 1; k < param.length; k++) {
                s += "," + param[k];
            }
            wekaReady.add(s);
        });
        return wekaReady;
    }

    /**
     * @param relationName
     * @param col
     * @return weka-ready list of strings. Just print to a file and you're good
     * to go.
     * @throws Exception
     */
    public ArrayList<String> wekaReadyLines(String relationName, Collection<T> col) throws Exception {
        ArrayList<String> wekaReadyAttributes = this.wekaReadyAttributes();
        ArrayList<String> wekaReady = new ArrayList<>(5 + wekaReadyAttributes.size() + col.size()); // ensure size

        wekaReady.add("@Relation " + relationName);
        wekaReady.add("");
        wekaReady.addAll(wekaReadyAttributes);
        wekaReady.add("");
        wekaReady.add("@DATA");
        wekaReady.add("");
        wekaReady.addAll(this.wekaReadyDataLines(col));
        return wekaReady;

    }
}