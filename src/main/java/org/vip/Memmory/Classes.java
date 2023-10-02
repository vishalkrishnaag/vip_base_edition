package org.vip.Memmory;

import java.util.ArrayList;

public class Classes {
    String className;
    public ArrayList<fields> fieldIds;
    public   ArrayList<String> methodIds;

    public ArrayList<fields> getFieldIds() {
        return fieldIds;
    }

    public void setFieldIds(ArrayList<fields> fieldIds) {
        this.fieldIds = fieldIds;
    }

    public ArrayList<String> getMethodIds() {
        return methodIds;
    }

    public void setMethodIds(ArrayList<String> methodIds) {
        this.methodIds = methodIds;
    }

    public Classes(String className, ArrayList<fields> fieldIds, ArrayList<String> methodIds) {
        this.fieldIds = new ArrayList<>();
        this.methodIds = new ArrayList<>();
        this.className = className;
        this.fieldIds = fieldIds;
        this.methodIds = methodIds;
    }
    public Classes(String className, fields field, String method) {
        this.fieldIds = new ArrayList<>();
        this.methodIds = new ArrayList<>();
        this.className = className;
        this.fieldIds.add(field);
        this.methodIds.add(method);
    }
    public Classes(String className) {
        this.fieldIds = new ArrayList<fields>();
        this.methodIds = new ArrayList<String>();
        this.className = className;
    }
}
