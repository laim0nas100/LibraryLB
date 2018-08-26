/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

/**
 *
 * @author Lemmin
 */
public class RepeatedReflectNode extends FinalReflectNode {

    protected ReflectNode ref;

    public RepeatedReflectNode(String name, String fieldName, Object ob, Class clz, ReflectNode ref, ReferenceCounter<ReflectNode> references) {
        super(name, fieldName, ob, clz, references);
        this.ref = ref;
        this.repeated = true;
        this.children = ref.children;
        this.values = ref.values;
        this.holder = ref.holder;
        this.obj = ref.obj;
    }

    public ReflectNode getRef() {
        return ref;
    }

}