package org.vip.Codegen;

public enum Instruction {
    begin, // class begin
    str, // declare field
    TIN, // type integer
    NUL, // type null
    push, // type int
    pushs, // type string
    pushf, // type float
    pushb, // type boolean

    rts, // fetch variable
    call,  // call method
    call_obj, // call method with objectId
    make_obj, // copy object
    ret, // return
    jne, // jump if not equal on last stack frame
    je // jump equal value on last stack frame
}
