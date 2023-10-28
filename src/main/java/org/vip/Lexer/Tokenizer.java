package org.vip.Lexer;

import java.util.*;


public class Tokenizer {
    private final ArrayList<String> codeLines;
    private int currentLine;
    private final int currentColumn;
    private final Map<String, TokenType> symbolMap;
    private final Map<String, TokenType> keywordMap;

    public Tokenizer(ArrayList<String> input) {
        currentLine = 0;
        currentColumn = 0;
        symbolMap = new HashMap<>();
        keywordMap = new HashMap<>();
        this.codeLines = input;
        initializeSymbolMap();
        initializeKeywordMap();
    }

    private void initializeSymbolMap() {
        symbolMap.put(";", TokenType.SEMICOLON);
        symbolMap.put(",", TokenType.COMMA);
        symbolMap.put("^", TokenType.POWER);
        symbolMap.put("%", TokenType.MOD);
        symbolMap.put("&", TokenType.AND);
        symbolMap.put("|", TokenType.OR);
        symbolMap.put("@", TokenType.AT);
        symbolMap.put(".", TokenType.DOT);
        symbolMap.put("+", TokenType.PLUS);
        symbolMap.put("-", TokenType.MINUS);
        symbolMap.put("*", TokenType.STAR);
        symbolMap.put("/", TokenType.SLASH);
        symbolMap.put("(", TokenType.LPAR);
        symbolMap.put(")", TokenType.RPAR);
        symbolMap.put("{", TokenType.LBRACE);
        symbolMap.put("}", TokenType.RBRACE);
        symbolMap.put("[", TokenType.L_SQ_BRACE);
        symbolMap.put("]", TokenType.R_SQ_BRACE);
        symbolMap.put("?", TokenType.CHECK);
        symbolMap.put(">", TokenType.GREATER_THAN);
        symbolMap.put("=", TokenType.ASSIGN);
        symbolMap.put("!", TokenType.NOT);
        symbolMap.put("~", TokenType.NOT);
        symbolMap.put("<", TokenType.LESS_THAN);
    }

    private void initializeKeywordMap() {
        keywordMap.put("var", TokenType.VAR);
        keywordMap.put("and", TokenType.AND);
        keywordMap.put("or", TokenType.OR);
        keywordMap.put("extends", TokenType.EXTENDS);
        keywordMap.put("depends", TokenType.DEPENDS);
        keywordMap.put("enum", TokenType.ENUM);
        keywordMap.put("return", TokenType.RETURN);
        keywordMap.put("true", TokenType.TRUE);
        keywordMap.put("false", TokenType.FALSE);
        keywordMap.put("null", TokenType.NULL_T);
        keywordMap.put("not", TokenType.NOT);
        keywordMap.put("int", TokenType.INT_T);
        keywordMap.put("string", TokenType.STRING_T);
        keywordMap.put("float", TokenType.FLOAT_T);
        keywordMap.put("boolean", TokenType.BOOL_T);
        keywordMap.put("void", TokenType.VOID_T);
        keywordMap.put("container", TokenType.CONTAINER);
        keywordMap.put("if", TokenType.IF);
        keywordMap.put("else", TokenType.ELSE);
        keywordMap.put("when", TokenType.WHEN);
        keywordMap.put("end", TokenType.END);
        keywordMap.put("while", TokenType.WHILE);
        keywordMap.put("foreach", TokenType.FOR_EACH);
        keywordMap.put("custom", TokenType.CUSTOM);
        keywordMap.put("equals",TokenType.EQUALS); // equals
        keywordMap.put("greater_than",TokenType.GREATER_THAN);
        keywordMap.put("less_than",TokenType.LESS_THAN);
        keywordMap.put("Object",TokenType.OBJECT_T);
        keywordMap.put("object",TokenType.OBJECT_T);
        keywordMap.put("self",TokenType.SELF);
    }


    private boolean isSymbol(String lexeme) {
        return symbolMap.containsKey(lexeme);
    }

    private TokenType getSymbolType(String lexeme) {
        return symbolMap.get(lexeme);
    }

    private boolean isKeyword(String str) {
        return keywordMap.containsKey(str);
    }

    private TokenType getKeywordType(String str) {
        return keywordMap.get(str);
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private boolean isDecimal(char c) {
        return c == '.';
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public List<Token> getTokens() {
        List<Token> tokens = new ArrayList<>();

        for (String line : codeLines) {
            int lineLength = line.length();
            int currentColumn = 0;

            for (int i = 0; i < lineLength; i++) {
                char c = line.charAt(i);

                if (Character.isWhitespace(c)) {
                    continue;  // Skip whitespace characters
                }
                else if (isSymbol(Character.toString(c)) && c=='=') {
                    if (line.charAt(i+1) == '=')
                    {
                        i++;
                        tokens.add(new Token(TokenType.EQUALS, TokenKind.SYMBOL, currentLine, currentColumn,"=="));
                    }
                    else if (line.charAt(i+1) == '>')
                    {
                        i++;
                        tokens.add(new Token(TokenType.GREATER_THAN_EQ, TokenKind.SYMBOL, currentLine, currentColumn,"=>"));
                    }
                    else if (line.charAt(i+1) == '<')
                    {
                        i++;
                        tokens.add(new Token(TokenType.LESS_THAN_EQ, TokenKind.SYMBOL, currentLine, currentColumn,"<="));
                    }
                    else
                    {
                        String symbolLexeme = Character.toString(c);
                        tokens.add(new Token(getSymbolType(symbolLexeme), TokenKind.SYMBOL, currentLine, currentColumn, symbolLexeme));
                    }
                }
                else if (isSymbol(Character.toString(c))) {
                    String symbolLexeme = Character.toString(c);
                    tokens.add(new Token(getSymbolType(symbolLexeme), TokenKind.SYMBOL, currentLine, currentColumn, symbolLexeme));
                }
                else if (isDigit(c)) {
                    StringBuilder number = new StringBuilder();
                    int startColumn = currentColumn;

                    while (i < lineLength && (isDigit(c) || isDecimal(c))) {
                        number.append(c);
                        i++;
                        currentColumn++;
                        if (i < lineLength) {
                            c = line.charAt(i);
                        }
                    }

                    if (isDecimal(number.charAt(number.length() - 1))) {
                        tokens.add(new Token(TokenType.FLOAT,TokenKind.SYMBOL, currentLine, startColumn, number.toString()));
                    } else {
                        tokens.add(new Token(TokenType.INT,TokenKind.SYMBOL, currentLine, startColumn, number.toString()));
                    }
                    i--;  // Adjust the index to account for the last character read

                } else if (isIdentifierStart(c)) {
                    StringBuilder identifier = new StringBuilder();
                    int startColumn = currentColumn;

                    while (i < lineLength && isIdentifierPart(c)) {
                        identifier.append(c);
                        i++;
                        currentColumn++;
                        if (i < lineLength) {
                            c = line.charAt(i);
                        }
                    }

                    String identifierStr = identifier.toString();
                    if (isKeyword(identifierStr)) {
                        tokens.add(new Token(getKeywordType(identifierStr),TokenKind.KEYWORD, currentLine, startColumn, identifierStr));
                    } else {
                        tokens.add(new Token(TokenType.IDENTIFIER,TokenKind.KEYWORD, currentLine, startColumn, identifierStr));
                    }
                    i--;  // Adjust the index to account for the last character read

                } else if (c == '"') {
                    StringBuilder stringLiteral = new StringBuilder();
                    int startColumn = currentColumn;

                    i++;
                    currentColumn++;
                    if (i < lineLength) {
                        c = line.charAt(i);
                    }

                    while (i < lineLength && c != '"') {
                        stringLiteral.append(c);
                        i++;
                        currentColumn++;
                        if (i < lineLength) {
                            c = line.charAt(i);
                        }
                    }

                    tokens.add(new Token(TokenType.STRING,TokenKind.SYMBOL, currentLine, startColumn, stringLiteral.toString()));

                } else if (c == '\'') {
                    StringBuilder stringLiteral = new StringBuilder();
                    int startColumn = currentColumn;

                    i++;
                    currentColumn++;
                    if (i < lineLength) {
                        c = line.charAt(i);
                    }

                    while (i < lineLength && c != '\'') {
                        stringLiteral.append(c);
                        i++;
                        currentColumn++;
                        if (i < lineLength) {
                            c = line.charAt(i);
                        }
                    }

                    tokens.add(new Token(TokenType.STRING,TokenKind.SYMBOL, currentLine, startColumn, stringLiteral.toString()));

                } else {
                    String lex = Character.toString(c);
                    tokens.add(new Token(TokenType.ERROR,TokenKind.SYMBOL, currentLine, currentColumn, lex));
                }

                currentColumn++;
            }

            currentLine++;
        }

        tokens.add(new Token(TokenType.EOL,TokenKind.SYMBOL, currentLine, currentColumn, "EOL"));
        return tokens;
    }

}
