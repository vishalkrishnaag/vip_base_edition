package org.vip.Memmory;

import org.vip.Exception.VipCompilerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolTable {
    /**
     * in vip symbol table is a small database
     * class_map -> class name | depends list | memmory_segment
     * var_map   -> method name | var name | type | memmory_location
     * method_map -> class name | method name  | list parms length & type | memmory_segment
     * field_map  -> class name | data type | link
     * label_map  -> label name | expression
     */

    private List<Classes> clsMap;
    public HashMap<String, String> LabelMap;


    public SymbolTable() {
        this.clsMap = new ArrayList<>();
        this.LabelMap = new HashMap<>();
    }

    public int getLastClassIndex() {
        return clsMap.size();
    }

    public int getLastFieldIndex(Integer classId) {
        return this.clsMap.get(classId).fieldIds.size();
    }

    public int getLastLabelIndex(Integer classId) {
        return this.clsMap.get(classId).methodIds.size();
    }

    public int getClassIndex(String className) {
        int currentIndex = 0;
        for (Classes key : clsMap) {
            if (key.className.equals(className)) {
                break;
            }
            currentIndex++;
        }
        return currentIndex;
    }

    public Classes getClass(Integer classId) {
        if (this.clsMap.isEmpty()) {
            return null;
        }
        if (this.clsMap.get(classId) != null) {
            return this.clsMap.get(classId);
        }
        return null;
    }

    public String getClassName(Integer classId) {
        if (this.clsMap.isEmpty()) {
            return null;
        }
        if (this.clsMap.get(classId) != null) {
            return this.clsMap.get(classId).className;
        }
        return null;
    }


    public boolean isClassExist(Integer classId) {
        if (clsMap.isEmpty()) {
            return false;
        }
        if (clsMap.get(classId) != null) {
            return true;
        }
        return false;
    }

    public boolean isClassExist(String className) {
        if (clsMap.isEmpty()) {
            return false;
        }
        boolean flag = false;
        for (Classes key : clsMap) {
            if (key.className.equals(className)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    public int getMethodIndex(String methodName, Integer classId) throws VipCompilerException {
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("try to define class before fetching method");
        }
        int currentIndex = -1;


        if (clsMap.get(classId).methodIds.contains(methodName)) {
            for (int i = 0; i <= clsMap.get(classId).methodIds.size(); i++) {
                if (clsMap.get(classId).methodIds.get(i).equals(methodName)) {
                    currentIndex = i;
                    break;
                }

            }
        }
        if (currentIndex == -1) {
            throw new VipCompilerException("method does not exist");
        }
        return currentIndex;
    }

    public boolean isMethodExist(String MethodName, Integer methodId, Integer classId) throws VipCompilerException {
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("try to define class before fetching method");
        }
        Classes key = clsMap.get(classId);
        if (key != null)
            if (key.methodIds.get(methodId) != null) {
                return true;
            }

        throw new VipCompilerException("method does not exist");
    }


    public int getFieldIndex(String fieldName, Integer classId) throws VipCompilerException {
        int currentIndex = -1;

        for (int i = 0; i < clsMap.get(classId).fieldIds.size(); i++) {
            if (clsMap.get(classId).fieldIds.get(i).fieldName.equals(fieldName)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) {
            throw new VipCompilerException("field does not exist");
        }
        return currentIndex;
    }


    public boolean isFieldExist(String fieldName, Integer classId) throws VipCompilerException {
        boolean flag = false;
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("symbol table is empty");
        }
        List<fields> field1 = this.clsMap.get(classId).fieldIds;
        if (field1.isEmpty()) {
            throw new VipCompilerException("fields are empty");
        } else {
            for (org.vip.Memmory.fields fields : this.clsMap.get(classId).fieldIds) {
                if (fields.fieldName.equals(fieldName)) {
                    flag = true;
                    break;
                }
            }

        }
        return flag;
    }

    public boolean isFieldExist(Integer fieldId, Integer classId) throws VipCompilerException {
        boolean flag = false;
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("symbol table is empty");
        }
        List<fields> field1 = this.clsMap.get(classId).fieldIds;
        if (field1.isEmpty()) {
            throw new VipCompilerException("fields are empty");
        } else {
            if (this.clsMap.get(classId).fieldIds != null) {
//                throw new VipCompilerException("fields are empty");
                flag = false;
            } else {
                if (this.clsMap.get(classId).fieldIds.get(fieldId) != null) {
                    flag = true;
                }
            }

        }
        return flag;
    }

    public boolean delClass(String className) {
        boolean flag = false;
        for (Classes key : clsMap) {
            if (key.className.equals(className)) {
                this.clsMap.remove(key);
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean delField(Integer fieldId, Integer classId) throws VipCompilerException {
        boolean flag = true;
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("symbol table is empty");
        }
        List<fields> methods = this.clsMap.get(classId).fieldIds;
        if (methods.isEmpty()) {
            throw new VipCompilerException("methods are empty");
        } else {
            this.clsMap.get(classId).fieldIds.remove(fieldId);
        }
        return flag;
    }


    public boolean delMethod(Integer methodId, Integer classId) throws VipCompilerException {
        boolean flag = true;
        if (this.clsMap.isEmpty()) {
            throw new VipCompilerException("symbol table is empty");
        }
        List<String> methods = this.clsMap.get(classId).methodIds;
        if (methods.isEmpty()) {
            throw new VipCompilerException("methods are empty");
        } else {
            this.clsMap.get(classId).methodIds.remove(methodId);
        }
        return flag;
    }


    public int AddClass(String className) throws VipCompilerException {
        Classes classes = new Classes(className);
        if (!isClassExist(className)) {
            this.clsMap.add(classes);
            return this.clsMap.size();
        } else {
            throw new VipCompilerException("class  '" + className + "already declared");
        }
    }

    public int AddClass(String className, fields field, String methodName) throws VipCompilerException {
        Classes classes = new Classes(className, field, methodName);
        if (!isClassExist(className)) {
            this.clsMap.add(classes);
            return this.clsMap.size();
        } else {
            throw new VipCompilerException("class  '" + className + "already declared");
        }
    }

    public int AddField(String className, String fieldName, String expr, Integer classId) throws VipCompilerException {
        fields fields = new fields(fieldName, classId, expr);
        if (isClassExist(classId)) {
            this.clsMap.get(classId).fieldIds.add(fields);
            return this.clsMap.get(classId).fieldIds.size();
        } else {
            throw new VipCompilerException("field  '" + fieldName + "already declared in class");
        }
    }

    public int AddFieldLabel(String fieldName, String expr, Integer classId) throws VipCompilerException {
        int fieldId = getFieldIndex(fieldName, classId);
        if (isFieldExist(fieldName, classId)) {
            this.clsMap.get(classId).fieldIds.get(fieldId);
            if (this.clsMap.get(classId).fieldIds.get(fieldId) != null) {
                this.clsMap.get(classId).fieldIds.get(fieldId).implementation = expr;
            } else {
                throw new VipCompilerException("field  '" + fieldName + " not found in class ");
            }
            return this.clsMap.get(classId).fieldIds.size();
        } else {
            throw new VipCompilerException("field  '" + fieldName + " not found in class ");
        }
    }


    public boolean AddLabel(String labelName, String value) throws VipCompilerException {
        if (!this.LabelMap.containsKey(labelName)) {
            this.LabelMap.put(labelName, value);
            return true;
        } else {
            throw new VipCompilerException("label  '" + labelName + "already declared");
        }
    }


    public boolean AddMethod(String className, String methodName, Integer classId) throws VipCompilerException {
        boolean flag = false;
        if (isClassExist(className)) {
            for (int i = 0; i <= clsMap.size(); i++) {
                if (clsMap.get(classId).methodIds.contains(methodName)) {
                    throw new VipCompilerException("method " + methodName + " is already exist");
                } else {
                    this.clsMap.get(i).getMethodIds().add(methodName);
                    flag = true;
                    break;
                }
            }
        } else {
            throw new VipCompilerException(" class or method  does not exist ");
        }
        return flag;
    }


    public void removeMethodFromClass(String className, Integer methodId) throws VipCompilerException {
        boolean flag = false;
        if (!isClassExist(className)) {
            throw new VipCompilerException("class '" + className + "' does not exist");
        }
        int index = 0;
        for (Classes key : clsMap) {
            index++;
            if (key.className.equals(className)) {
                key.getMethodIds().remove(methodId);
                this.clsMap.set(index, key);
                flag = true;
                break;
            }
        }
        if (flag == false) {
            throw new VipCompilerException("Method does not exist");
        }
    }


    public void removeFieldFromClass(String className, Integer fieldId) throws VipCompilerException {
        boolean flag = false;
        if (!isClassExist(className)) {
            throw new VipCompilerException("class '" + className + "' does not exist");
        }
        int index = 0;
        for (Classes key : clsMap) {
            index++;
            if (key.className.equals(className)) {
                key.getFieldIds().remove(fieldId);
                this.clsMap.set(index, key);
                flag = true;
                break;
            }
        }
        if (flag == false) {
            throw new VipCompilerException("Field does not exist");
        }
    }
}
