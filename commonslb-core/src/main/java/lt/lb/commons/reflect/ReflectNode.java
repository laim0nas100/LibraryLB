/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.*;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectNode {

    protected FullFieldHolder holder;

    protected ReflectNode parent;
    protected ReflectNode superClassNode;
    protected ReflectNode shadows;
    protected Map<String, ReflectNode> children = new HashMap<>();
    protected Map<String, ReflectNode> values = new HashMap<>();
    protected ReferenceCounter<ReflectNode> references = new ReferenceCounter<>();
    protected Object obj;
    protected Class declaredClass;
    protected Class realClass;

    protected String name;
    protected String fieldName;
    protected boolean repeated;

    protected boolean populated = false;
    protected boolean fullyPopulated = false;

    public ReflectNode(String name, String fieldName, Object ob, Class clz, ReferenceCounter<ReflectNode> references) {
        this.name = name;
        this.fieldName = fieldName;
        this.obj = ob;
        this.declaredClass = clz;
        this.realClass = clz;
        populated = isNull();
        fullyPopulated = isNull();
        if (!isNull() && clz.isInterface()) { //get true class
            realClass = ob.getClass();
        }
        holder = new FullFieldHolder<>(realClass);
        if (references != null) {
            this.references = references;
        }
    }

    public ReflectNode(Object ob) {
        //root node
        this(ob.getClass().getSimpleName(), null, ob, ob.getClass(), new ReferenceCounter<>());
    }

    protected void fullPopulate() {
        if (fullyPopulated) {
            return;
        }
        try {
            populate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // populate nested children
        this.collectThroughSuperNode(n -> n.getChildren(), putShadowing);

        //populate nested values
        this.collectThroughSuperNode(n -> n.getValues(), putShadowing);

        this.fullyPopulated = true;

    }

    protected void populate() throws Exception {
        if (populated) {
            return;
        }
        children.clear();
        values.clear();
        FieldMap localFields = holder.getLocal().getFields();

        for (Map.Entry<String, Field> entry : localFields.entrySet()) {
            Field f = entry.getValue();
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            String nodeFieldName = entry.getKey();

            Object value = f.get(obj);

            Class type = f.getType();
            if (FieldHolder.IS_COMMON.test(f)) {
                ReflectNode node = new FinalReflectNode(this.getName() + "." + nodeFieldName, nodeFieldName, value, type, this.references);
                values.put(nodeFieldName, node);
                node.parent = this;
            } else if (type.isArray()) {
                ReflectNode node = new ArrayReflectNode(this.getName() + "." + nodeFieldName, nodeFieldName, value, type, this.references);
                children.put(nodeFieldName, node);
                node.parent = this;
            } else {
                if (this.references.contains(value)) {
                    ReflectNode get = this.references.get(value);
                    boolean equalReference = get.getValue() == value;
                    if (equalReference) {
                        Log.print("Found equal ref", f);
                        if (!get.repeated) {
                            get.repeated = true;
                        }
                        children.put(nodeFieldName, new RepeatedReflectNode(this.getName() + "." + nodeFieldName, nodeFieldName, value, type, get, this.references));

                    } else {
                        ReflectNode node = new ReflectNode(this.getName() + "." + nodeFieldName, nodeFieldName, value, type, this.references);
                        children.put(nodeFieldName, node);
                        node.parent = this;
                        references.registerIfAbsent(value, node);
                    }
                } else {
                    ReflectNode node = new ReflectNode(this.getName() + "." + nodeFieldName, nodeFieldName, value, type, this.references);
                    children.put(nodeFieldName, node);
                    node.parent = this;
                    if (value != null) {
                        references.registerIfAbsent(value, node);
                    }

                }

            }
            f.setAccessible(accessible);
        }
        Class superCls = this.getRealClass().getSuperclass();
        if (superCls != null) {
            String newNodeName = this.getName() + "_" + superCls.getSimpleName();

            this.superClassNode = new ReflectNode(newNodeName, this.getFieldName(), this.obj, superCls, this.references);
            this.superClassNode.populate();
        }

        populated = true;

    }

    protected void setShadowedNested(ReflectNode toHide) {
        if (this.getShadowed() == null) {
            this.shadows = toHide;
        } else {
            this.getShadowed().setShadowedNested(toHide);
        }
    }

    public Map<String, ReflectNode> getChildren() {
        try {
            populate();
        } catch (Exception ex) {
            throw new Error(ex);
        }
        return this.children;
    }

    private HashMap<String, ReflectNode> collectThroughSuperNode(NodeGetter getter, NodeVisitor visiter) {
        ReflectNode node = this;
        HashMap<String, ReflectNode> accumulator = new HashMap<>();
        while (node != null) {
            for (Map.Entry<String, ReflectNode> entry : getter.get(node).entrySet()) {
                visiter.visit(accumulator, entry);
            }
            node = node.superClassNode;

        }
        return accumulator;
    }

    public static interface NodeVisitor extends Visitor<Map<String, ReflectNode>, Map.Entry<String, ReflectNode>> {
    }

    public static interface NodeGetter extends Getter<ReflectNode, Map<String, ReflectNode>> {
    }

    private static NodeVisitor putIfAbsentVisitor = (map, entry) -> {
        map.putIfAbsent(entry.getKey(), entry.getValue());
    };

    private static NodeVisitor putShadowing = (map, entry) -> {
        String key = entry.getKey();
        ReflectNode newChild = entry.getValue();
        if (map.containsKey(key)) {
            ReflectNode child = map.get(key);
            child.setShadowedNested(newChild);
        } else {
            map.put(key, newChild);
        }
    };

    public Map<String, ReflectNode> getAllChildren() {
        this.fullPopulate();
        return this.collectThroughSuperNode((ReflectNode f) -> f.getChildren(), putIfAbsentVisitor);
    }

    public Map<String, ReflectNode> getValues() {
        try {
            populate();
        } catch (Exception ex) {
            throw new Error(ex);
        }
        return this.values;
    }

    public Map<String, ReflectNode> getAllValues() {
        this.fullPopulate();
        return this.collectThroughSuperNode((ReflectNode f) -> f.getValues(), putIfAbsentVisitor);
    }

    public FieldHolder getStaticFieldHolder() {
        return this.holder.staticHolder;
    }

    public FieldHolder getLocalFieldHolder() {
        return this.holder.localHolder;
    }

    public ReflectNode getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    public final String getFieldName() {
        return this.fieldName;
    }

    public Object getValue() {
        return this.obj;
    }

    public final Class getRealClass() {
        return realClass;
    }

    public final Class getDeclaredClass() {
        return this.declaredClass;
    }

    public ReflectNode getShadowed() {
        this.fullPopulate();
        return this.shadows;
    }

    public final boolean isNull() {
        return obj == null;
    }

    public final boolean isShadowing() {
        this.fullPopulate();
        return shadows != null;
    }

    public final boolean isArray() {
        return this.getRealClass().isArray();
    }

    public final boolean isRepeated() {
        return this.repeated;
    }

}