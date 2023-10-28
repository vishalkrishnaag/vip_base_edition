package org.vip.Parser;
import org.vip.Exception.VipCompilerException;
import org.vip.Lexer.TokenKind;
import org.vip.Memmory.Symbol;
import org.vip.Lexer.Token;
import org.vip.Lexer.TokenType;
import org.vip.Memmory.event;

import java.util.*;

public class Parser {
    private final List<Token> tokenizerOutput;
    private int currentToken;
    private Token token;
    private String containerName;
    private final List<Symbol> eventMap;
    private int label = 1;
    private ConversionHelper conversionHelper;


    public Parser(List<Token> tokens) {
        tokenizerOutput = new ArrayList<>(tokens);
        currentToken = 0;
        this.eventMap = new ArrayList<>();
        token = new Token();
        containerName = "";
      conversionHelper=new ConversionHelper();
    }


    public List<Symbol> getCompilationEngineOutput() throws VipCompilerException {
        compile();
        advanceToken();
        if (token.getType() != TokenType.EOL) {
            throw new VipCompilerException("No statements and expressions allowed in vip");
        }
//        System.out.println(token.getType()+" ,lex: "+token.getLexeme());
        return this.eventMap;
    }


    private void compile() throws VipCompilerException {
        advanceToken();
        String containerName = tokenizerOutput.get(0).getLexme();
        if(token.getType()==TokenType.CONTAINER)
        {
            eventMap.add(new Symbol(event.CLASS_DECL, listOf(containerName)));
            advanceToken();
        }
        else
        {
            throw new VipCompilerException("Expected a container declaration  got "+token.getType());
        }

        expect(TokenType.LBRACE, true);

        while (currentToken < tokenizerOutput.size()) {

            if (exitOnSee(TokenType.RBRACE)) {
                break;
                // in case of container main() {}
            }

            if (token.getType() == TokenType.VAR) {
                // should be a field
                this.compileVariableDeclaration();

            }
            else if(token.getType()==TokenType.IDENTIFIER)
            {
                this.compileFunctionStatement();
            }
            else  if(token.getType()==TokenType.OBJECT_T)
            {
                throw new VipCompilerException("unsupported datatype inside container " + token.getType());
            }
            else {
                throw new VipCompilerException("unsupported datatype inside container , expected variable or method " + token.getType());
            }

        }
        addEvent(event.CLASS_END, null);
    }

    private void compileFunctionStatement() throws VipCompilerException {
        addEvent(event.METHOD_DECL_BEGIN, listOf(token.getLexme()));
        advanceToken(); //  eat func_name
        compileBlockStatement();
        addEvent(event.METHOD_DECL_END, null);
    }

    private void compileStatements() throws VipCompilerException {
        switch (token.getType()) {
            case VAR:
                throw new VipCompilerException("vip does not support creating variables inside a method");
            case IDENTIFIER:
                // ramanan(word : "hello world");
                if (nextToken().getType() == TokenType.LPAR) {
                    compileSubroutineCall();
                } else if (nextToken().getType() == TokenType.DOT) {
                    compileChainMethod();
                } else if (nextToken().getType() == TokenType.ASSIGN) {
                    compileVariableAssign();
                } else if (nextToken().getType() == TokenType.LBRACE) {
                    throw new VipCompilerException("vip does not support nested functions");
                } else {
                    throw new VipCompilerException("undefined operation received");
                }
                break;
            case WHILE:
                compileWhileStatement();
                break;
            case RETURN:
                compileReturnStatement();
                break;
            case IF:
                compileIfStatement( false);
                break;
            case ELSE:
                this.compileIfStatement(true);
            case RBRACE:
                break;
            case CONSTRUCTOR:
                throw new VipCompilerException("vip does not support nested container");
            default:
                throw new VipCompilerException("Unknown statement type " + token.getType());
        }
    }

    private void compileChainMethod() throws VipCompilerException {
        String output=token.getLexme()+".";
        String offset1 = token.getLexme();
        expect(TokenType.IDENTIFIER);
        expect(TokenType.DOT);
        output+= token.getLexme();
        String offset2 = token.getLexme();
        expect(TokenType.IDENTIFIER);
        Token next = nextToken();
        if(token.getType()==TokenType.DOT && next.getType()==TokenType.IDENTIFIER)
        {
            throw new VipCompilerException("cannot invoke  field '"+output+"."+next.getLexme());
        }
        addEvent(getSymbol(event.CHAIN_METHOD_BEGIN, listOf(offset1,offset2,output)));
        compileExpressionList(true);
        expect(TokenType.SEMICOLON);
//            System.out.println("chain : " + output);
        addEvent(getSymbol(event.CHAIN_METHOD_END,null));
    }

    private void expect(TokenType type) {
        if (token.getType() == type) {
            advanceToken();
        }
    }

    //"expected function name"
    private void expect_var() throws VipCompilerException {
        if (token.getType() == TokenType.VAR) {
            advanceToken();
        } else {
            throw new VipCompilerException("expected var in variable declaration");
        }
    }


    private void expect(TokenType type, boolean strict) throws VipCompilerException {
        if (token.getType() == type) {
            advanceToken();
        } else {
            if (strict) {
                String err = "Compilation Error occurred type : [" + type + "] ";
                switch (type) {
                    case LBRACE -> err = "expected { in the container declaration ";
                    case LPAR -> err = "expected ( ";
                    case RPAR -> err = "expected ) ";
                    case RBRACE -> err = "expected } ";
                    case IDENTIFIER -> err = "identifier Expected";
                    case CONTAINER -> err = "container Expected";
                    case DOT -> err = "dot expected in this.expression ";
                    default -> {}
                }
                throw new VipCompilerException(err);
            }

        }
    }

    private void expect(TokenType type,String error) throws VipCompilerException {
        if (token.getType() == type) {
            advanceToken();
        } else {
                throw new VipCompilerException(error);
        }
    }
    private void compileBlockStatement() throws VipCompilerException {
        addEvent(event.BLOCK_BEGIN, null);
        expect(TokenType.LBRACE);

        while (currentToken < tokenizerOutput.size()) {
            this.compileStatements();
            if (exitOnSee(TokenType.RBRACE)) {
                break;
            }
        }
        expect(TokenType.RBRACE);
        addEvent(event.BLOCK_END, null);
    }

    public static String convertListToString(List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string).append(".");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private void compileChainStatements(String methodName) throws VipCompilerException {
        addEvent(getSymbol(event.CHAIN_STMT_BEGIN, null));
        List<String> tokenList = new ArrayList<>(100);
        String output;
        tokenList.add(token.getLexme());
        advanceToken();
        if (token.getType() == TokenType.DOT) {
            /* container.objectAB = expression is deprecated in vip
               you can use container.method() { return objectAb.something() }
             */
            while (token.getType() == TokenType.DOT || token.getType() == TokenType.IDENTIFIER) {
                // container.method(this.somethingFishy);
                if (exitOnSee(TokenType.LPAR)) {
                    break;
                }

                if (token.getType() == TokenType.DOT) {
                    advanceToken();
                }

                if (token.getType() == TokenType.IDENTIFIER) {
                    tokenList.add(token.getLexme());
                    advanceToken();
                }
            }

            output = convertListToString(tokenList);
            if (token.getType() == TokenType.LPAR) {
                // a.b.c.d.method call
                addEvent(getSymbol(event.METHOD_CALL_BEGIN, listOf(output)));
                this.compileExpressionList( true);
                addEvent(getSymbol(event.METHOD_CALL_END, null));
            } else if (token.getType() == TokenType.ASSIGN) {
                advanceToken();
                // a = 10 format
                addEvent(getSymbol(event.VAR_ASSIGN_BEGIN, null));
                this.compileExpression();
                addEvent(event.VAR_MAPPING, listOf(output));
                addEvent(getSymbol(event.VAR_ASSIGN_END, null));
            }

            System.out.println("out : " + output);
        } else {
            throw new VipCompilerException("Expected . in a.b format");
        }
        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.CHAIN_STMT_END, listOf(output)));
    }

    private void compileChainExpressions(String methodName) throws VipCompilerException {
        addEvent(getSymbol(event.CHAIN_STMT_BEGIN, null));
        List<String> tokenList = new ArrayList<>(100);
        String output;
        tokenList.add(token.getLexme());
        advanceToken();

        if (token.getType() == TokenType.DOT) {
            // container.objectAB = expression
            while (token.getType() == TokenType.DOT || token.getType() == TokenType.IDENTIFIER) {
                // container.method(this.somethingFishy);
                if (exitOnSee(TokenType.LPAR)) {
                    break;
                }

                if (token.getType() == TokenType.DOT) {
                    advanceToken();
                }

                if (token.getType() == TokenType.IDENTIFIER) {
                    tokenList.add(token.getLexme());
                    advanceToken();
                }
            }
            output = convertListToString(tokenList);
            if (token.getType() == TokenType.LPAR) {
                // a.b.c.d.method call
                addEvent(getSymbol(event.METHOD_CALL_BEGIN, listOf(output)));
                this.compileExpressionList( true);
                addEvent(getSymbol(event.METHOD_CALL_END, null));
            }
            System.out.println("out : " + output);
        } else {
            throw new VipCompilerException("Expected . in a.b format");
        }
        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.CHAIN_STMT_END, listOf(output)));
    }

    private void compileChainString(String methodName) throws VipCompilerException {
        addEvent(getSymbol(event.CHAIN_STMT_BEGIN, null));
        List<String> tokenList = new ArrayList<>(100);
        String output;
        tokenList.add(token.getLexme());
        advanceToken();

        if (token.getType() == TokenType.DOT) {
            // container.objectAB = expression
            while (token.getType() == TokenType.DOT || token.getType() == TokenType.IDENTIFIER) {
                // container.method(this.somethingFishy);
                if (token.getType() == TokenType.DOT) {
                    advanceToken();
                } else if (token.getType() == TokenType.IDENTIFIER) {
                    tokenList.add(token.getLexme());
                    advanceToken();
                } else {
                    break;
                }
            }
            output = convertListToString(tokenList);
//            System.out.println("chain : " + output);
        } else {
            throw new VipCompilerException("Expected . in a.b format");
        }
        addEvent(getSymbol(event.CHAIN_STMT_END, listOf(output)));
    }

    private void compileVariableDeclaration() throws VipCompilerException {
        addEvent(getSymbol(event.VAR_DECL_BEGIN, null));
        expect_var(); // eat var
        List<String> varList = new ArrayList<>();
        //var a,b,c = 10 like format
        varList.add(token.getLexme());
        expect(TokenType.IDENTIFIER, true);
        while (token.getType() == TokenType.COMMA || token.getType() == TokenType.IDENTIFIER || token.getType() == TokenType.ASSIGN) {

            if (token.getType() == TokenType.COMMA) {
                advanceToken(); // eat ,
            }
            if (token.getType() == TokenType.IDENTIFIER) {
                varList.add(token.getLexme());
                advanceToken(); // eat a,b
            }
            if (token.getType() == TokenType.ASSIGN) {
                advanceToken();//eat :
                eventMap.add(new Symbol(event.VAR_DECL_IASSIGN, varList));
                this.compileExpressionList(false);
                break;
            }

            if (exitOnSee(TokenType.SEMICOLON)) {
                eventMap.add(new Symbol(event.VAR_DECL_IASSIGN, varList));
                break;
            }
        }
        if (token.getType() == TokenType.DOT) {
            throw new VipCompilerException("dot is not allowed in variable declaration ");
        }

        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.VAR_DECL_END, null));
    }

    private void compileVariableAssign() throws VipCompilerException {
        addEvent(getSymbol(event.VAR_ASSIGN_BEGIN, null));
        if (token.getType() == TokenType.IDENTIFIER) {
            // assignment a = 10 + 3;
            Token object = token;
            advanceToken();
            if (token.getType() == TokenType.ASSIGN) {
                advanceToken();
                // a = 10 format
                this.compileExpression();
                addEvent(event.VAR_MAPPING, listOf(object.getLexme()));
            }
        }
        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.VAR_ASSIGN_END, null));
    }


    private void compileWhileStatement() throws VipCompilerException {
        advanceToken(); // eat while
        addEvent(getSymbol(event.WHILE_BEGIN, null));
        this.compileExpressionList(false);
        this.compileBlockStatement();
        addEvent(getSymbol(event.WHILE_END, null));
    }


    private void compileIfStatement(boolean elseif) throws VipCompilerException {
        advanceToken(); // eat if
        addEvent(getSymbol(event.IF_COND_BEGIN, null));
        if (elseif) {
            if (token.getType() == TokenType.IF) {
                // else if case
                addEvent(getSymbol(event.ELIF_BEGIN, null));
                this.compileIfStatement( false);
                addEvent(getSymbol(event.ELIF_END, null));
            } else {
                // else case
                addEvent(getSymbol(event.ELSE_BEGIN, null));
                compileBlockStatement();
                addEvent(getSymbol(event.ELSE_END, null));
            }
        } else {
            this.compileExpressionList( false);
            this.compileBlockStatement();
        }
        addEvent(getSymbol(event.IF_COND_END, null));
    }

    private void compileReturnStatement() throws VipCompilerException {
        addEvent(getSymbol(event.RETURN_BEGIN, null));
        expect(TokenType.RETURN, true); //eat return
        compileExpression();
        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.RETURN_END, null));
    }

    private void compileSubroutineCall() throws VipCompilerException {
        addEvent(getSymbol(event.METHOD_CALL_BEGIN, listOf(token.getLexme())));
        expect(TokenType.IDENTIFIER);
        compileExpressionList( true);
        expect(TokenType.SEMICOLON);
        addEvent(getSymbol(event.METHOD_CALL_END, null));
    }

    private boolean exitOnSee(TokenType type) {
        return token.getType() == type;
    }

    private boolean exitOnSee(TokenType type, TokenType type1) {
        if (token.getType() == type || token.getType() == type1) {
            return true;
        }
        return false;
    }

    private boolean exitOnSee(TokenType type, TokenType type1, TokenType type2) {
        return token.getType() == type || token.getType() == type1 || token.getType() == type2;
    }

    private void compileExpression() throws VipCompilerException {
        addEvent(getSymbol(event.EXPR_BEGIN, null));
        if (exitOnSee(TokenType.SEMICOLON)) {
            addEvent(getSymbol(event.EXPR_END, null));
            return;
        }

        compileTerm();
        while (isOperator(token.getType())) {
            addEvent(getSymbol(event.OP_DECL, listOf(conversionHelper.convertOperator(token.getType()))));
            advanceToken();
            if (exitOnSee(TokenType.SEMICOLON, TokenType.RBRACE, TokenType.LBRACE)) {
                return;
            }
                // a = 10 format

                    // a : b will be valid
                    /* a[i] : b is invalid in vip if you want to
                    do it will be like var a : array()
                                           a.assign(i,b)
                     */

                compileTerm();
        }

        addEvent(getSymbol(event.EXPR_END, null));
    }

    private List<String> listOf(String data) {
        List<String> stringList = new ArrayList<>();
        stringList.add(data);
        return stringList;
    }

    private List<String> listOf(String data, String data1) {
        List<String> stringList = new ArrayList<>();
        stringList.add(data);
        stringList.add(data1);
        return stringList;
    }

    private List<String> listOf(String data, String data1, String data2) {
        List<String> stringList = new ArrayList<>();
        stringList.add(data);
        stringList.add(data1);
        stringList.add(data2);
        return stringList;
    }

//    private List<String> listOf(String data, String data1, String data2, String data3) {
//        List<String> stringList = new ArrayList<>();
//        stringList.add(data);
//        stringList.add(data1);
//        stringList.add(data2);
//        stringList.add(data3);
//        return stringList;
//    }

//    private List<String> listOf(String data, String data1, String data2, String data3, String data4) {
//        List<String> stringList = new ArrayList<>();
//        stringList.add(data);
//        stringList.add(data1);
//        stringList.add(data2);
//        stringList.add(data3);
//        stringList.add(data4);
//        return stringList;
//    }

    void addEvent(Symbol symbol) {
        this.eventMap.add(symbol);
    }

    void addEvent(event eventType, List<String> address) {
        Symbol symbol = new Symbol(eventType, address);
        this.eventMap.add(symbol);
    }

    Symbol getSymbol(event eventType, List<String> address) {
        return new Symbol(eventType, address);
    }

    private void compileTerm() throws VipCompilerException {
        addEvent(getSymbol(event.TERM_BEGIN, null));
        switch (token.getType()) {
            case IDENTIFIER:

                /* TODO: not handled  eg a = b; here b is the data handled here
                 * b should be searched on sym-tab and corresponding activities should be implemented
                 */
                if (nextToken().getType() == TokenType.LPAR) {
                    this.compileSubroutineCall();
                    break;
                } else if (nextToken().getType() == TokenType.DOT) {
                    this.compileChainMethod();
                    break;
                }
                else if(nextToken().getType()==TokenType.ASSIGN){
                    compileVariableAssign();
                    break;
                }
                else if(isOperator(nextToken().getType())){
                    addEvent(getSymbol(event.VAR_MAPPING, listOf(token.getLexme())));
                    advanceToken();
                    break;
                }
                else{
                    addEvent(getSymbol(event.VAR_MAPPING, listOf(token.getLexme())));
                    advanceToken();
                    break;
                }
            case SELF:
                this.compileSelfStatement();
                break;
            case INT, TRUE, FALSE, NULL_T:
                addEvent(getSymbol(event.STATIC_FILED, listOf(token.getLexme(), token.getType().toString())));
                advanceToken();
                break;
            case STRING:
                addEvent(getSymbol(event.STATIC_FILED, listOf("\"" + token.getLexme() + "\"", token.getType().toString())));
                advanceToken();
                break;
            case IF:
                this.compileIfStatement(false);
                break;
            case LPAR:
                this.compileExpressionList( true);
            case NOT:
                if (nextToken().getType() == TokenType.EQUALS) {
                    advanceToken();
                    this.compileTerm();
                } else {
                    advanceToken();
                    // Todo: assemble Not statements pending
                    this.compileTerm();
                    break;
                }
                break;
            default:
                if (token.getType() == TokenType.RBRACE || token.getType() == TokenType.SEMICOLON) {
                    return;
                }
                throw new VipCompilerException ("unknown token " + token.getType());
        }
        addEvent(getSymbol(event.TERM_END, null));
    }

    private void compileSelfStatement() throws VipCompilerException {
        advanceToken(); // eat self
        expect(TokenType.DOT,"expected dot in self");
        addEvent(getSymbol(event.SELF_FIELD, listOf(token.getLexme())));
        expect(TokenType.IDENTIFIER,"expected self.field name");
    }

    private void compileVarStaticExpr() throws VipCompilerException {
        switch (token.getType()) {
            case IDENTIFIER:
                if (nextToken().getType() == TokenType.LPAR) {
                    throw new VipCompilerException("A Field must be static  does not contain any method calls");
                } else if (nextToken().getType() == TokenType.DOT) {
                    throw new VipCompilerException("A Field must be static  does not contain.chain.statements");
                }
                else if(isOperator(nextToken().getType())){
                    throw new VipCompilerException("A Field must be static  does not contain any expressions");
                }
                else {
                    throw new VipCompilerException("unknown operation on "+token.getType()+" : "+token.getLexme());
                }
            case INT, TRUE, FALSE, NULL_T:
                addEvent(getSymbol(event.STATIC_FILED, listOf(token.getLexme(), token.getType().toString())));
                advanceToken();
                break;
            case STRING:
                addEvent(getSymbol(event.STATIC_FILED, listOf("\"" + token.getLexme() + "\"", token.getType().toString())));
                advanceToken();
                break;
            default:
                throw new VipCompilerException ("Invalid Operation on Field , got " + token.getType());
        }
    }

    private void compileExpressionList(boolean strict) throws VipCompilerException {
        addEvent(getSymbol(event.EXPR_LIST_BEGIN, null));
        if (nextToken().getType() == TokenType.RPAR) {
            advanceToken();// eat left parenthesis
            advanceToken();// eat right parenthesis
            addEvent(getSymbol(event.EXPR_LIST_END, null));
            return;
        }

        expect(TokenType.LPAR, strict);

        while (currentToken < tokenizerOutput.size()) {
            if (exitOnSee(TokenType.SEMICOLON, TokenType.LBRACE, TokenType.RPAR)) {
                break;
            }

            this.compileExpression();
            expect(TokenType.COMMA);
        }
        expect(TokenType.RPAR, strict);
        addEvent(getSymbol(event.EXPR_LIST_END, null));
    }

    private void advanceToken() {
        currentToken++;
        if (currentToken < tokenizerOutput.size()) {
            token = tokenizerOutput.get(currentToken);
        }
    }

    private void retreatToken() {
        currentToken--;
        token = tokenizerOutput.get(currentToken);
    }

//    private Token getToken() {
//        return tokenizerOutput.get(currentToken);
//    }

    private Token nextToken() {
        advanceToken();
        Token nextToken = token;
        retreatToken();
        return nextToken;
    }
// private Token getNextToken() {
//     advanceToken();
//     Token nextToken = getToken();
//     retreatToken();
//     return nextToken;
// }

    private boolean isOperator(TokenType type) throws VipCompilerException {
        switch (type) {
            case PLUS, MINUS, SLASH, AND, OR, EQUALS -> {
                return true;
            }
            case LESS_THAN -> {
                if (nextToken().getType() == TokenType.OR) {
                    advanceToken(); // eat less_than
                    advanceToken(); // eat or
                    if (token.getType() == TokenType.EQUALS) {
                        token.setLexme("less_than or equal to");
                        token.setType(TokenType.LESS_THAN_EQ);
                        token.setKind(TokenKind.KEYWORD);
                        return true;
                    } else {
                        throw new VipCompilerException("expected equals in less_than or equals");
                    }

                } else if (nextToken().getType() == TokenType.EQUALS) {
                    advanceToken();
                    token.setLexme("less_than or equal to");
                    token.setType(TokenType.LESS_THAN);
                    token.setKind(TokenKind.KEYWORD);
                    return true;
                } else {
                    return true;
                }

            }
            case GREATER_THAN -> {
                if (nextToken().getType() == TokenType.OR) {
                    advanceToken(); // eat greater_than
                    advanceToken(); // eat or
                    if (token.getType() == TokenType.EQUALS) {
                        token.setLexme("greater_than or equal to");
                        token.setType(TokenType.GREATER_THAN_EQ);
                        token.setKind(TokenKind.KEYWORD);
                        return true;
                    } else {
                        throw new VipCompilerException("expected equals in less_than or equals");
                    }

                } else if (nextToken().getType() == TokenType.EQUALS) {
                    advanceToken();
                    token.setLexme("greater_than or equal to");
                    token.setType(TokenType.GREATER_THAN_EQ);
                    token.setKind(TokenKind.KEYWORD);
                    return true;
                } else {
                    return true;
                }
            }
            case ASSIGN, STAR -> {
                return true;
            }
            case NOT -> {
                if (nextToken().getType() == TokenType.EQUALS) {
                    advanceToken();
                    token.setLexme("not equals");
                    token.setType(TokenType.NOT_EQUAL);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}

