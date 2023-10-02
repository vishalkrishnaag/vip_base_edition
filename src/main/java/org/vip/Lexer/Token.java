package org.vip.Lexer;
public class Token {
    TokenType type;
    String lexme;
    TokenKind kind;

    public TokenKind getKind() {
        return kind;
    }

    public void setKind(TokenKind kind) {
        this.kind = kind;
    }

    int line_no;
    int col_no;

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getLexme() {
        return lexme;
    }

    public void setLexme(String lexme) {
        this.lexme = lexme;
    }

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public int getCol_no() {
        return col_no;
    }

    public void setCol_no(int col_no) {
        this.col_no = col_no;
    }

    public Token(TokenType type,TokenKind kind, int line_no, int col_no, String lexme) {
        this.type = type;
        this.kind = kind;
        this.lexme = lexme;
        this.line_no = line_no;
        this.col_no = col_no;
    }
    public Token() {}

}
