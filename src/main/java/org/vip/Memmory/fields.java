package org.vip.Memmory;

public class fields {
    String  fieldName;
    int classId;
    String implementation;
    String referenceName; // in case of var a : new classA(input : " hello world");
    boolean is_static=false;

    public boolean isStatic() {
        return is_static;
    }

    public void setIs_static(boolean is_static) {
        this.is_static = is_static;
    }

    public fields(String fieldName, int classId, String expression) {
        this.fieldName = fieldName;
        this.implementation = expression;
        this.classId = classId;
    }
}
