package org.vip.Lexer;

public enum TokenType {
    STRING,
    SEMICOLON,
    COMMA,
    POWER,
    MOD,
    AND,
    OR,
    AT,
    DOT,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    LPAR,
    RPAR,
    LBRACE,
    RBRACE,
    L_SQ_BRACE,
    R_SQ_BRACE,
    CHECK,
    GREATER_THAN,
    ASSIGN,
    NOT,
    LESS_THAN,
    EQUALS,
    LESS_THAN_EQ,
    GREATER_THAN_EQ,
    ERROR,
    EXTENDS,
    DEPENDS,
    ENUM,
    RETURN,
    TRUE,
    FALSE,
    NULL_T,
    NAMESPACE,
    INT_T,
    FLOAT_T,
    BOOL_T,
    VOID_T,
    STRING_T,
    IF,
    ELSE,
    WHEN,
    END,
    DEF,
    WHILE,
    FOR_EACH,
    FLOAT,
    INT,
    IDENTIFIER,
    EOL,
    STATIC,
    FIELD,
    CONSTRUCTOR,
    CUSTOM,
    SQUARE_ROOT, // it is normally bitwise xor
    NOT_EQUAL,
    SELF,
    CONTAINER, OBJECT_T
}
