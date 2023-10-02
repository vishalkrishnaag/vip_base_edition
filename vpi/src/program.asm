add:
    add

mul:
    mul

main:
    push 10
    push 20
    call add
    push 30
    call mul
    halt



label add1:
    add
    ret

label mul1:
    mul
    ret

label main:
    push 10
    push 20
    add
    push 30
    str 0
    rts 0
    print
    call add1
    pushf 30.02
    pushs "java world"
    print
    call mul1
    halt