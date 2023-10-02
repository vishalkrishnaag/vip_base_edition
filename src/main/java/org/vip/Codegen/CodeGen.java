package org.vip.Codegen;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.vip.Exception.VipCompilerException;
import org.vip.Memmory.Symbol;
import org.vip.Memmory.event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CodeGen {
    /*
     *
     * class_name -> id , shared = true
     * */
    List<Tripples<String, Integer, Boolean>> classList;
    /*
     *
     * filedName,fieldId ,class_id
     * */
    List<Tripples<String, Integer, Integer>> fieldList;
    /*
     *
     * methodName -> methodId , classId
     * */
    List<Tripples<String, Integer, Integer>> methodList;
    /*
     *   objectName , ObjectId, ClassId
     *
     *
     */
    List<Tripples<String, Integer, Integer>> ObjectList;
    private int currentEvent;

    private String currentClassName;
    private final int totalSize;
    private Symbol cevent;

    //    FileOutputStream file;
    PrintWriter file;

    private final List<Symbol> events;
    private Map<Integer, String> builtinMethods;
    private int mainFound = 0;
    private int objectIndex = 0;
    private Integer currentClassId = null;
    private Boolean isCurrentClassShared = null;
    private boolean paddingEnabled = false;
    private boolean write_count;
    private int label;

    public CodeGen(List<Symbol> events) throws Exception {
        this.events = events;
        objectIndex = 0;
        label=0;
        write_count =false;
        classList = new ArrayList<>();
        fieldList = new ArrayList<>();
        methodList = new ArrayList<>();
        ObjectList = new ArrayList<>();
        System.out.println("total " + events.size() + " events");
        //implement write logic here
        totalSize = events.size();
        advanceEvent();
        fillLists();
        if (mainFound == 0) {
            throw new VipCompilerException("Main method Not found");
        }
        if (mainFound > 1) {
            throw new VipCompilerException("Multiple Main Declartion found");
        }
        file = new PrintWriter("src/test/java/output.vpx");
        this.Generator();
        file.close();

    }

    private void fillLists() throws VipCompilerException {
        int currentField = 0;
        int currentMethod = 0;
        int currentClassId = 0;
        boolean isSharedClas = false;
        classList.add(create_class("System", 0, false));
        fieldList.add(create_Field("in", 0, 0));
        methodList.add(create_tripple("println", 0, 0));
        methodList.add(create_tripple("print", 1, 0));
        methodList.add(create_tripple("scan", 2, 0));
        methodList.add(create_tripple("scanln", 3, 0));
        currentClassId++;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).eventType == event.CLASS_DECL) {
                is_class_exist(events.get(i).Elements.get(0));
                classList.add(create_class(events.get(i).Elements.get(0), currentClassId, false));
                currentClassName = events.get(i).Elements.get(0);
                currentClassId = currentClassId + 1;
                currentField = 0;
                currentMethod = 0;
            } else if (events.get(i).eventType == event.SHARED_CLASS_DECL) {
                is_class_exist(events.get(i).Elements.get(0));
                // to
                classList.add(create_class(events.get(i).Elements.get(0), currentClassId, true));
                currentClassName = events.get(i).Elements.get(0);
                isSharedClas = true;
                currentClassId = currentClassId + 1;
            } else if (events.get(i).eventType == event.METHOD_DECL_BEGIN) {
                if (events.get(i).Elements.get(0).equals("main") || events.get(i).Elements.get(0).equals("Main")) {
                    mainFound = mainFound + 1;
                    if (!isSharedClas) {
                        throw new VipCompilerException("main method should be called in a shared class");
                    }
                }
                is_method_exist(events.get(i).Elements.get(0), getClassId(currentClassName));
                methodList.add(create_tripple(events.get(i).Elements.get(0), currentMethod, getClassId(currentClassName)));
                currentMethod++;
            } else if (events.get(i).eventType == event.VAR_DECL_BEGIN) {
                if (events.get(i + 1).eventType == event.VAR_DECL_IASSIGN) {
                    List<String> varList = events.get(i + 1).getElements();
                    if (varList == null) {
                        throw new VipCompilerException("Invalid field declaration on primary analysis");
                    }
                    for (String iter : varList) {
                        fieldList.add(create_Field(iter, currentField, getClassId(currentClassName)));
                        currentField++;
                    }
                } else if (events.get(i + 2).eventType == event.VAR_DECL_IASSIGN) {
                    List<String> varList = events.get(i + 2).getElements();
                    int classId = getClassId(currentClassName);
                    for (String iter : varList) {
                        fieldList.add(create_Field(iter, currentField, classId));
                        currentField++;
                    }
                } else {
                    throw new VipCompilerException("expected field name got " + events.get(i + 1).eventType);
                }

            }
        }
    }

    @Contract(mutates = "this")
    private @NotNull String getLabel() {
        label++;
        return "L" + label;
    }

    private void emit(String data) throws IOException {
            this.file.println(data);
        this.write_count=true;
    }

    private void Generator() throws Exception {
        while (currentEvent < totalSize) {
            if (cevent.eventType == event.CLASS_DECL) {
                System.out.println("analyzing '" + cevent.Elements.get(0) + "'.class ");
                currentClassId = getClassId(cevent.Elements.get(0));
                currentClassName=cevent.Elements.get(0);
                isCurrentClassShared = false;
                GenerateCodeForClassDecl();
            } else if (cevent.eventType == event.SHARED_CLASS_DECL) {
                System.out.println("analyzing " + cevent.Elements.get(0) + ".class ");
                currentClassId = getClassId(cevent.Elements.get(0));
                currentClassName=cevent.Elements.get(0);
                isCurrentClassShared = true;
                GenerateCodeForClassDecl();
            } else {
                throw new RuntimeException("Error in code analysis got " + cevent.eventType);
            }
        }
        emit("call main");
    }

    private void is_class_exist(String s) throws VipCompilerException {
        if (s == null) {
            throw new NullPointerException("class Name is Null");
        }

        for (Tripples<String, Integer, Boolean> tripples : classList) {
            if (tripples.contains(s)) {
                throw new VipCompilerException("class '" + s + "' Already Declared");
            }
        }
    }

    private Tripples getClass(String s) throws VipCompilerException {
        if (s == null) {
            throw new NullPointerException("class Name is Null");
        }
        Tripples t = null;
        for (Tripples<String, Integer, Boolean> tripples : classList) {
            if (tripples.contains(s)) {
                t = tripples;
                break;
            }
        }
        if (t == null) {
            throw new VipCompilerException("class '" + s + "' is not found");
        }
        return t;
    }

    private void is_method_exist(String s, Integer classId) throws VipCompilerException {
        if (s == null) {
            throw new NullPointerException("Method Name is Null");
        }

        for (Tripples<String, Integer, Integer> tripples : methodList) {
            if (tripples.contains(s) && tripples.hasValue1(classId)) {
                throw new VipCompilerException("Method '" + s + "' Already Declared");
            }
        }
    }

    private int getClassId(String s) throws VipCompilerException {
        if (s == null) {
            throw new NullPointerException("class Name is Null");
        }
        Integer id = null;
        for (Tripples<String, Integer, Boolean> tripples : classList) {
            if (tripples.contains(s)) {
                id = tripples.value;
                break;
            }
        }
        if (id == null) {
            throw new VipCompilerException("Class '" + s + "' not found");
        }
        return id;
    }

    private Pair<String, Integer>
    create_pair
            (String s, int currentClassId) {
        return new Pair<String, Integer>(s, currentClassId);
    }

    private Quintet<String, Integer, Integer, Integer, Boolean>
    create_quintet
            (String fieldName, int filedId, int classId, Integer reference_id, boolean ref_is_field) {
        return new Quintet<>(fieldName, filedId, classId, reference_id, ref_is_field);
    }

    private Quads<String, Integer, Integer, Integer>
    create_Quads
            (String ObjectName, int ObjectId, int classId, Integer methodId) {
        return new Quads<>(ObjectName, ObjectId, classId, methodId);
    }

    private void GenerateCodeForClassDecl() throws Exception {
        if (isCurrentClassShared == null) {
            throw new VipCompilerException("class Not found");
        }
        if (isCurrentClassShared) {
            expect(event.SHARED_CLASS_DECL, true);
        } else {
            ;
            expect(event.CLASS_DECL, true);
        }

        emit(Instruction.begin+"");

        while (cevent.eventType != event.CLASS_END) {
            if (cevent.eventType == event.VAR_DECL_BEGIN) {
                GenerateCodeForVarDecl();
            } else if (cevent.eventType == event.CLASS_INVOKE_BEGIN) {
                GenerateCodeForClassInvoking();
            } else if (cevent.eventType == event.METHOD_DECL_BEGIN) {
                GenerateCodeForMethodDecl();
            } else if (cevent.eventType == event.EXTENDS_EVENT) {
                throw new VipCompilerException("extends not supported in vip");
            } else {
                throw new VipCompilerException("unsupported data type got " + cevent.eventType);
            }
        }
        emit("end");
        expect(event.CLASS_END);
    }

    private void GenerateCodeForClassInvoking() throws VipCompilerException, IOException {
        expect(event.CLASS_INVOKE_BEGIN);
        List<String> ref_class = cevent.Elements;
        expect(event.RERENCE_CLASS);
        String taget_class = cevent.Elements.get(0);
        expect(event.TARGET_CLASS);
        for (String objectName : ref_class) {
            int RefClassId = getClassId(taget_class);
            Tripples cls = getClass(taget_class);
            if (cls.hasValue1(true)) {
                throw new VipCompilerException("cannot invoke a shared class '" + taget_class + "' in vip ");
            }
            ObjectList.add(create_Object(objectName, objectIndex, RefClassId));
            emit(Instruction.make_obj + " " + objectIndex + " " + RefClassId);
            objectIndex++;
        }
        expect(event.CLASS_INVOKE_END);
    }

    private void GenerateCodeForExtendClasses() throws VipCompilerException {
        Symbol symbol = cevent;
        expect(event.EXTENDS_EVENT);
        throw new VipCompilerException("vip does'nt support support class extending ");
        // copy parent classes to the child
    }

    private void GenerateCodeForMethodDecl() throws Exception {
        Symbol symbol = cevent;
        expect(event.METHOD_DECL_BEGIN);
        String methodName = symbol.Elements.get(0);
        Integer methodId= getMethodId(methodName,currentClassId);
        emit( "label "+methodName);
        this.paddingEnabled = true;
        this.write_count =false;
        GenerateCodeForBlockStatements();
        if(!write_count)
        {
            throw new VipCompilerException("method '"+methodName+"' has no body either comment it else remove it");
        }
        expect(event.METHOD_DECL_END);
        this.paddingEnabled = false;
    }

    private Tripples<String, Integer, Integer> create_tripple(String methodName, Integer methodId, Integer classId) {
        return new Tripples<>
                (methodName, methodId, classId);
    }

    private Tripples<String, Integer, Integer> create_Object(String ObjectName, Integer objectId, Integer classId) {
        return new Tripples<>
                (ObjectName, objectId, classId);
    }

    private Tripples<String, Integer, Integer> create_Field(String fieldName, Integer fieldId, Integer classId) {
        return new Tripples<>
                (fieldName, fieldId, classId);
    }

    private Tripples<String, Integer, Boolean> create_class(String className, Integer classId, Boolean isShared) {
        return new Tripples<>
                (className, classId, isShared);
    }

    private void expect(event type) throws VipCompilerException {
        if (type == cevent.eventType) {
            if (currentEvent < totalSize) {
                advanceEvent();
            }
        } else {
            throw new RuntimeException("expected " + type + " , got " + cevent.eventType);
        }
    }

    private void expect_(event type) throws VipCompilerException {
        if (type == cevent.eventType) {
            advanceEvent();
        }
    }

    private boolean match(event type) throws VipCompilerException {
        if (type != cevent.eventType) {
            throw new VipCompilerException("expected '" + type + "' but got " + cevent.eventType);
        } else {
            return true;
        }
    }

    private void expect(event type, boolean strict) throws VipCompilerException {
        if (type == cevent.eventType) {
            advanceEvent();
        } else {
            if (strict) {
                throw new RuntimeException("expected " + type);
            }
        }
    }

    private void expect(event type, String err) throws VipCompilerException {
        if (type == cevent.eventType) {
            advanceEvent();
        } else {
            throw new RuntimeException(err);
        }
    }

    private void GenerateCodeForStatements() throws Exception {
        switch (cevent.eventType) {
            case WHILE_BEGIN -> GenerateCodeForWhileStatements();
            case VAR_ASSIGN_BEGIN -> GenerateCodeForVarAssignment();
//            case CHAIN_STMT_BEGIN -> GenerateCodeForChainStatements();
            case CHAIN_METHOD_BEGIN -> GenerateCodeForChainMethodCall();
            case METHOD_CALL_BEGIN -> GenerateCodeForMethodCall();
            case RETURN_BEGIN -> GenerateCodeForReturnStatement();
            case IF_COND_BEGIN -> GenerateCodeForIfStatements();
            case ELSE_BEGIN -> throw new VipCompilerException("else detected without if condition ");
            case METHOD_DECL_BEGIN -> throw new VipCompilerException("vip does'nt support nested method declarations ");
            case VAR_DECL_BEGIN ->
                    throw new VipCompilerException("vip does'nt support variable declaration on method ");
            case CLASS_DECL -> throw new VipCompilerException("vip does'nt support nested class declarations ");
            default -> throw new Exception("statement undefined got " + cevent.eventType);
        }

    }

    private void GenerateCodeForBlockStatements() throws Exception {
        expect(event.BLOCK_BEGIN);
        while (cevent.eventType != event.BLOCK_END) {
            GenerateCodeForStatements();
        }
        expect(event.BLOCK_END);
    }

    private void GenerateCodeForReturnStatement() throws Exception {
        expect(event.RETURN_BEGIN);
        if (cevent.eventType == event.EXPR_BEGIN) {
            // return x+a;
            GenerateCodeForExpression(currentClassId);
        }
        emit(Instruction.ret+"");
        expect(event.RETURN_END);
    }

    private void GenerateCodeForChainStatements() throws Exception {
        expect(event.CHAIN_STMT_BEGIN);
        if (cevent.eventType == event.METHOD_CALL_BEGIN) {
            GenerateCodeForMethodCall();
        }
        if (cevent.eventType == event.EXPR_LIST_BEGIN) {
            // a.b.c.d();
//            GenerateCodeForExpressionList();
        }
        expect(event.CHAIN_STMT_END);
    }

    private void GenerateCodeForChainMethodCall() throws Exception {
        String referenceCls = cevent.Elements.get(0);
        String caller = cevent.Elements.get(1);
        String callMethod = cevent.Elements.get(2);
        expect(event.CHAIN_METHOD_BEGIN);
        Tripples<String, Integer, Integer> t = null;
        Boolean isObject = false;
        boolean isShared = false;
        for (Tripples<String, Integer, Boolean> tripples : classList) {
            if (tripples.contains(referenceCls)) {
                t = new Tripples<>();
                t.key = tripples.key;
                t.value = tripples.value;
                isShared = tripples.value1;
                break;
            }
        }
        if (t == null) {
            for (Tripples<String, Integer, Integer> tripples : ObjectList) {
                if (tripples.contains(referenceCls)) {
                    t = new Tripples<>();
                    t = tripples;
                    isObject = true;
                    break;
                }
            }
        }
        if (t == null) {
            throw new VipCompilerException("Class or object '" + referenceCls + "' not found");
        }
        if (isObject) {
            // an invoked objec.method(); format
            if (isShared) {
                throw new VipCompilerException("cannot invoke a shared class use directly in  " + callMethod);
            }
            // get classId from object and get methodId
            int methodId = getMethodId(caller, t.value1);
            /*
             *
             * callling callMethodObject with methodId and objectId so it change datasource to the method
             * value -> methodId
             * value1 -> objectId
             */
            GenerateCodeForExpressionList(t.value1);
            emit(Instruction.call_obj + " " + methodId + " " + t.value);
        } else {
            // shared class.method()
            GenerateCodeForExpressionList(t.value);
            //todos : because we are now using string instead of int that's why methodId is not used
            int methodId = getMethodId(caller, t.value);
            emit(Instruction.call + " " + caller);
        }
        expect(event.CHAIN_METHOD_END);
    }

    private void GenerateCodeForMethodCall() throws Exception {
        Symbol cevent1 = cevent;
        expect(event.METHOD_CALL_BEGIN);
        GenerateCodeForExpressionList(currentClassId);
        expect(event.METHOD_CALL_END);
        emit(Instruction.call + " " + getMethodId(cevent1.Elements.get(0), currentClassId));
    }

    private void GenerateCodeForExpressionList(int classId) throws Exception {
        expect(event.EXPR_LIST_BEGIN);
        while (cevent.eventType != event.EXPR_LIST_END) {
            GenerateCodeForExpression(classId);
        }
        expect(event.EXPR_LIST_END);
    }

    private void GenerateCodeForExpression(int classId) throws Exception {
        expect(event.EXPR_BEGIN);
        if (cevent.eventType != event.EXPR_END) {
            GenerateCodeForTerminal(classId);
            while (cevent.eventType == event.OP_DECL) {
                if (cevent.eventType == event.EXPR_END) {
                    break;
                }
                Symbol mEvent = cevent;
                advanceEvent();
                GenerateCodeForTerminal(classId);
                emit(mEvent.Elements.get(0));
            }
        } else {
            advanceEvent();
            return;
        }
        expect(event.EXPR_END);
    }

    private void GenerateCodeForFieldTerm() throws Exception {
        if (cevent.eventType == event.STATIC_FILED) {
            String stat_field = cevent.Elements.get(0);
            if (cevent.Elements.get(1) == "STRING") {
                emit(Instruction.pushf + " " + stat_field);
            } else if (cevent.Elements.get(1) == "INT") {

                emit(Instruction.push + " " + stat_field);
            } else if (cevent.Elements.get(1) == "FLOAT") {

                emit(Instruction.pushf + " " + stat_field);
            } else if (cevent.Elements.get(1) == "NULL_T") {

                emit(Instruction.push + " " + Instruction.NUL);
            } else {
                // todo : it is wrong rts required
                emit(Instruction.push + " " + getFieldId(stat_field, currentClassId));
            }
            advanceEvent();
        }
        if (cevent.eventType == event.CHAIN_METHOD_BEGIN) {
            throw new VipCompilerException("chain.method() is not allowed in field declarations");
        }
        if (cevent.eventType == event.VAR_MAPPING) {
            // system.println(in : example) here in is the classId of method and example requires another classId ie current-class-id
//            emitField(Instruction.SV + " " + getFieldId(cevent.Elements.get(0),classId));
            throw new VipCompilerException("identifier is not allowed in field declarations");
        }
        if (cevent.eventType == event.EXPR_LIST_BEGIN) {
            throw new VipCompilerException("expressions are not allowed in field declarations");
        }
        if (cevent.eventType == event.IF_COND_BEGIN) {
            throw new VipCompilerException("if condition does not allowed in field declarations");
        }
        if (cevent.eventType == event.VAR_ASSIGN_BEGIN) {
            throw new VipCompilerException("assignment expressions are not allowed in field declarations");
        }
        if (cevent.eventType == event.SELF_FIELD) {
            throw new VipCompilerException("self is not allowed in field declarations");
        }
    }

    private void GenerateCodeForTerminal(int classId) throws Exception {
        expect(event.TERM_BEGIN);
        if (cevent.eventType == event.STATIC_FILED) {
            // todo:change it
            String stat_field = cevent.Elements.get(0);
            if (cevent.Elements.get(1) == "STRING") {
//                    for (char c : stat_field.toCharArray())
//                    {
//                        emit(Character.getNumericValue(c));
//                        System.out.print(c);
//                    }
                emit(Instruction.pushs + " " + stat_field);
            } else if (cevent.Elements.get(1) == "INT") {

                emit(Instruction.push + " " + stat_field);
            } else if (cevent.Elements.get(1) == "NULL_T") {

//                emit(Instruction.push + " " + Instruction.NUL);
                // null by default no need to push
            } else if (cevent.Elements.get(1) == "TRUE") {

                emit(Instruction.pushb + "  true");
            } else if (cevent.Elements.get(1) == "FALSE") {
                emit(Instruction.pushb + "  false");
            } else {
               // todo: rts is required
                emit(Instruction.push + " " + stat_field);
            }
            advanceEvent();
        }
        if (cevent.eventType == event.CHAIN_METHOD_BEGIN) {
            GenerateCodeForChainMethodCall();
        }
        if (cevent.eventType == event.VAR_MAPPING) {
            // system.println(in : example) here in is the classId of method and example requires another classId ie current-class-id
            emit(Instruction.rts + " " + getFieldId(cevent.Elements.get(0), classId));
            advanceEvent();
        }
        if (cevent.eventType == event.EXPR_LIST_BEGIN) {
            GenerateCodeForExpressionList(classId);
        }
        if (cevent.eventType == event.IF_COND_BEGIN) {
            GenerateCodeForIfStatements();
        }
        if (cevent.eventType == event.VAR_ASSIGN_BEGIN) {
            GenerateCodeForMethodVarAssignment(classId);
        }
        if (cevent.eventType == event.SELF_FIELD) {
            GenerateCodeForSelf();
        }

        // Todo: should handle identifier.a = expr type statements
        expect(event.TERM_END);
    }

    private void GenerateCodeForSelf() throws VipCompilerException, IOException {
        String fieldName = cevent.Elements.get(0);
        emit(Instruction.rts + " " + getFieldId(cevent.Elements.get(0), currentClassId));
        expect(event.SELF_FIELD);
    }

    private Integer getFieldId(String s, Integer classId) throws VipCompilerException {
        if (s == null) {
            throw new VipCompilerException("field is not resolved");
        }
        Integer value = null;
        for (Tripples<String, Integer, Integer> tripples : fieldList) {
            if (tripples.contains(s) && tripples.hasValue1(classId)) {
                value = tripples.value;
                break;
            }
        }
        if (value == null) {
            throw new VipCompilerException("field '" + s + "' is not resolved");
        } else {
            return value;
        }
    }

    private List<Tripples> getFieldSList(Integer classId) throws VipCompilerException {
        List<Tripples> fields = new ArrayList<>();
        for (Tripples<String, Integer, Integer> tripples : fieldList) {
            if (tripples.hasValue1(classId)) {
                fields.add(tripples);
            }
        }
        return fields;
    }

    private Integer getMethodId(String s, Integer classId) throws VipCompilerException {
        if (s == null) {
            throw new VipCompilerException("method is not resolved");
        }
        Integer value = null;
        for (Tripples<String, Integer, Integer> tripples : methodList) {
            if (tripples.contains(s) && tripples.hasValue1(classId)) {
                value = tripples.value;
                break;
            }
        }
        if (value == null) {
            throw new VipCompilerException("Method '" + s + "' is not resolved");
        } else {
            return value;
        }
    }

    private void GenerateCodeForVarAssignment() throws Exception {
        expect(event.VAR_ASSIGN_BEGIN);
        // return x+a;
        GenerateCodeForExpression(currentClassId);
        // store register_data (expression) -> field_id
        emit(Instruction.str + " " + getFieldId(cevent.Elements.get(0), currentClassId));
        expect(event.VAR_MAPPING);
        expect(event.VAR_ASSIGN_END);
    }

    private void GenerateCodeForMethodVarAssignment(int classId) throws Exception {
        expect(event.VAR_ASSIGN_BEGIN);
        GenerateCodeForExpression(classId);
        // store register_data (expression) -> field_id
        emit(Instruction.str + " " + getFieldId(cevent.Elements.get(0), classId));
        expect(event.VAR_MAPPING);
        expect(event.VAR_ASSIGN_END);
    }

    private void GenerateCodeForIfStatements() throws Exception {
        expect(event.IF_COND_BEGIN);
        String labelName = getLabel();
        String bodyName = getLabel();
        String elseName = getLabel();
        emit(Instruction.call+" "+labelName);
        emit("je "+bodyName);
        emit("jne "+elseName);
        if (cevent.eventType == event.EXPR_LIST_BEGIN) {
            emit("label "+labelName);
            GenerateCodeForExpressionList(currentClassId);
            emit("label "+bodyName);
            GenerateCodeForBlockStatements();
        } else if (cevent.eventType == event.ELSE_BEGIN) {
            expect(event.ELSE_BEGIN);
            emit("label "+bodyName);
            GenerateCodeForBlockStatements();
            expect(event.ELSE_END);
        } else if (cevent.eventType == event.ELIF_BEGIN) {
            expect(event.ELIF_BEGIN);
            GenerateCodeForIfStatements();
            expect(event.ELIF_END);
        } else {
            throw new VipCompilerException("if condition is not good");
        }
        expect(event.IF_COND_END);
    }

    private void GenerateCodeForWhileStatements() throws Exception {
        expect(event.WHILE_BEGIN);
        // todo: implement while
        String labelName = getLabel();
        String bodyName = getLabel();
        emit("label "+labelName);
        GenerateCodeForExpressionList(currentClassId); // condition
        emit("stop");
        emit("label "+bodyName);
        GenerateCodeForBlockStatements(); // body
        emit("stop");
        emit(Instruction.je+ " "+bodyName);
        emit(Instruction.call+ " "+labelName);
        expect(event.WHILE_END);
    }

    private void GenerateCodeForVarDecl() throws Exception {
        expect(event.VAR_DECL_BEGIN);
        String fields = "";

        if (cevent.eventType == event.VAR_DECL_IASSIGN) {
            // a : data
            for (String it : cevent.getElements()) {
                fields += Instruction.str + " " + getFieldId(it, currentClassId) + "\n";
            }
            fields = fields.substring(0, fields.length() - 1); // for trimming \n
            advanceEvent();
        }

        if (cevent.eventType == event.EXPR_LIST_BEGIN) {
            // a,b.c = expression format
            GenerateCodeForExpressionList(currentClassId);
        } else {
            throw new VipCompilerException("error on variable declaration got " + cevent.eventType);
        }
        if (fields.isEmpty()) {
            throw new VipCompilerException("field name must be required " + cevent.eventType);
        }

        emit(fields);
        expect(event.VAR_DECL_END);
    }

    void advanceEvent() throws VipCompilerException {
        if (currentEvent < totalSize) {
            this.cevent = this.events.get(this.currentEvent);
            this.currentEvent++;
        } else {
            throw new VipCompilerException("halted before finishing compilation");
        }
    }

    void retreatEvent() {
        if (this.currentEvent > 0) {
            this.currentEvent--;
            if (this.events.size() <= this.currentEvent) {
                this.cevent = this.events.get(this.currentEvent);
            }
        }
    }
}
