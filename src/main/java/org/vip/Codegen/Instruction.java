package org.vip.Codegen;

public enum Instruction {
    end,
    str, // declare field
    TIN, // type integer
    NUL, // type null
    push, // type int
    pushs, // type string
    pushf, // type float
    pushb, // type boolean

    rts, // fetch variable
    call,  // call method
    ret, // return
    jne, // jump if not equal on last stack frame
    add,
    sub,
    mul,
    div,
    greater_than,
    greater_than_or_equal,
    less_than,
    less_than_or_equal,
    and,
    or,
    not,
    not_equal,
    boolean_compare,
    halt,
    jump_if_equal,
    jump_if_not_equal,
    register_rule,
    call_field,
    label_begin,
    label_end,
    field_begin,
    field_end,
    array_begin,
    array_end,
    inner_array_begin,
    getInner_array_end,
}
