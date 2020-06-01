package lox.parser;

import lox.Lox;
import lox.exception.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Formal grammar rules
/*
    program     → declaration* EOF ;

    declaration → classDecl
            | funDecl
            | varDecl
            | statement ;

    classDecl → "class" IDENTIFIER ( "<" IDENTIFIER )?
            "{" function* "}" ;
    varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;

    funDecl  → "fun" function ;
    function → IDENTIFIER "(" parameters? ")" block ;
    parameters → IDENTIFIER ( "," IDENTIFIER )* ;

    statement   → exprStmt
                | printStmt
                | ifStmt
                | whileStmt
                | forStmt
                | block
                | returnStmt ;

    exprStmt  → expression ";" ;
    printStmt → "print" expression ";" ;
    ifStmt    → "if" "(" expression ")" statement ( "else" statement )? ;
    whileStmt → "while" "(" expression ")" statement ;
    forStmt   → "for" "(" ( varDecl | exprStmt | ";" )
                      expression? ";"
                      expression? ")" statement ;
    block     → "{" declaration* "}" ;
    returnStmt → "return" expression? ";" ;

    expression → assignment ;
    assignment → ( call "." )? IDENTIFIER "=" assignment
           | logic_or;
    logic_or   → logic_and ("or" logic_and)* ;
    logic_and  → equality ("and" equality)* ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    multiplication → unary ( ( "/" | "*" ) unary )* ;
    unary → ( "!" | "-" ) unary | call ;
    call → primary ( "(" arguments? ")" | "." IDENTIFIER )* ; // Function call or instance property access (get expressions)
    arguments → expression ( "," expression )* ;
    primary → "true" | "false" | "nil" | "this"
        | NUMBER | STRING | IDENTIFIER | "(" expression ")"
        | "super" "." IDENTIFIER ;
 */

/**
 * Recursive descent parser
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private boolean isAtEnd(){
        return peek().getType() == TokenType.EOF;
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token advance(){
        if(!isAtEnd()) current ++;
        return previous();
    }

    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean match(TokenType... types){
        for(TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private ParseException error(Token token, String message){
        Lox.error(token, message);
        return new ParseException();
    }

    /**
     * Consumes the next token if it is the given type, throws an error otherwise
     * @param next
     * @param errorMsg
     * @return
     */
    private Token consume(TokenType next, String errorMsg){
        if(check(next)) return advance();
        throw error(peek(), errorMsg);
    }

    /**
     *  Used to handle ParseExceptions
     *  Advance and drop tokens until we get to what is probably a good boundary
     */
    private void synchronize(){
        advance(); // Drop the bad token
        while(!isAtEnd()) {
            if (previous().getType() == TokenType.SEMICOLON) return;
            switch (peek().getType()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
        }
    }

    public List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    public ASTNode parseREPL(){
        Stmt statement = declaration();
        if(statement == null){
            current = 0;
            return expression();
        } else {
            return statement;
        }
    }

    // VARIABLE DECLARATIONS
    private Stmt declaration(){
        try{
            if(match(TokenType.VAR)) {
                return varDeclaration();
            } else if(match(TokenType.CLASS)){
                return classDeclaration();
            } else if(match(TokenType.FUN)){
                return function("function");
            } else {
                return statement();
            }
        } catch (ParseException e){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name after keyword 'var'");

        Expr init = null;
        if(match(TokenType.EQUAL)){
            init = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new Stmt.Var(name, init);
    }

    private Stmt classDeclaration(){
        Token name = consume(TokenType.IDENTIFIER, "Expected class name after keyword 'class'");

        Expr.Var superclass = null;
        if(match(TokenType.LESS)){
            consume(TokenType.IDENTIFIER, "Expected superclass name after '>'.");
            superclass = new Expr.Var(previous());
        }

        consume(TokenType.LEFT_BRACE, "Expected '{' before class body");
        List<Stmt.Fun> methods = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            methods.add(function("method"));
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after class body");
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Fun function(String kind){
        Token name = consume(TokenType.IDENTIFIER, "Expected " + kind + " name.");
        consume(TokenType.LEFT_PAREN, "Expected ( after " + kind + " name");
        List<Token> params = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do {
                if (params.size() >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.");
                }

                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters");
        consume(TokenType.LEFT_BRACE, "Expected '{' before " + kind + " body");
        List<Stmt> body = blockStatement().statements;
        return new Stmt.Fun(name, params, body);
    }

    // STATEMENTS
    private Stmt statement(){
        if(match(TokenType.PRINT)){
            return printStatement();
        } else if(match(TokenType.IF)) {
            return ifStatement();
        } else if(match(TokenType.WHILE)) {
            return whileStatement();
        } else if(match(TokenType.FOR)){
            return forStatement();
        } else if (match(TokenType.RETURN)){
            return returnStatement();
        } else if (match(TokenType.LEFT_BRACE)) {
            return blockStatement();
        } else {
            return expressionStatement();
        }
    }

    private Stmt printStatement(){
        Expr expression = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Print(expression);
    }

    private Stmt ifStatement(){
        consume(TokenType.LEFT_PAREN, "Expected '(' after keyword 'if'.");
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenCase = statement();
        if(match(TokenType.ELSE)){
            Stmt elseCase = statement();
            return new Stmt.If(cond, thenCase,elseCase);
        } else {
            return new Stmt.If(cond, thenCase, null);
        }
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after keyword 'while'.");
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(cond,body);
    }

    // First purely sugar statement
    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after keyword 'for'.");
        Stmt initializer = null;
        if(!match(TokenType.SEMICOLON)){
            if(match(TokenType.VAR)){
                initializer = varDeclaration();
            } else {
                initializer = expressionStatement();
            }
        }

        Expr condition = null;
        if(!match(TokenType.SEMICOLON)){
            condition = expression();
            consume(TokenType.SEMICOLON, "Expected ';' after for loop condition");
        }

        Expr increment = null;
        if(!match(TokenType.SEMICOLON)){
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after for loop clauses");

        Stmt body = statement();
        Stmt full = body;
        if(increment != null) {
            full = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if(condition != null) {
            full = new Stmt.While(condition, full);
        } else {
            full = new Stmt.While(new Expr.Literal(true), full); // If there is no loop condition, treat it as a while(true)
        }

        if(initializer != null) {
            full = new Stmt.Block(Arrays.asList(initializer, full));
        }

        return full;
    }

    private Stmt returnStatement(){
        Token keyword = previous();
        if(match(TokenType.SEMICOLON)){
            return new Stmt.Return(keyword, null);
        } else {
            Expr value = expression();
            consume(TokenType.SEMICOLON, "Expected ';' after return value");
            return new Stmt.Return(keyword, value);
        }
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expression);
    }

    private Stmt.Block blockStatement(){
        List<Stmt> statements = new ArrayList<>();
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return new Stmt.Block(statements);
    }

    // EXPRESSIONS

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr left = or();
        if(match(TokenType.EQUAL)){
            return assignmentRight(left);
        } else {
            return left;
        }
    }

    private Expr assignmentRight(Expr left){
        if(left instanceof Expr.Var){
            Token name = ((Expr.Var) left).name;
            Expr right = assignment();
            return new Expr.Assign(name, right);
        } else if(left instanceof Expr.Get){
            Expr.Get get = (Expr.Get) left; // All of the dots get matched into the get, we want to take the last get's name and turn it into the set
            Expr right = assignment();
            return new Expr.Set(get.target, get.name, right); // Passing in the target strips off the last get
        } else {
            error(previous(), "Invalid assignment target."); // We don't throw this exception because we don't need to resynchronize
            return left; // just return the left-hand expression so we can keep going
        }
    }

    private Expr or(){
        Expr res = and();
        while(match(TokenType.OR)){
            Token op = previous();
            Expr right = and();
            res = new Expr.LogicalBinary(res,op,right);
        }
        return res;
    }

    private Expr and(){
        Expr res = equality();
        while(match(TokenType.AND)){
            Token op = previous();
            Expr right = equality();
            res = new Expr.LogicalBinary(res,op,right);
        }
        return res;
    }

    private Expr equality(){
        Expr expr = comparison();

        // The while loop matches groups of ( ( "!=" | "==" ) comparison )*
        // which are iteratively added into expr as left expressions until we dont find anymore != or ==
        // and we return the expr
        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison(){
        Expr expr = addition();

        while(match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)){
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition(){
        Expr expr = multiplication();

        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication(){
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)) {
            return new Expr.Unary(previous(), unary());
        } else {
            return call();
        }
    }

    // Function calls and property gets chained together
    // eg: thing()('stuff')().foo.bar()();
    private Expr call(){
        Expr expr = primary();

        while(true){
            if(match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if(match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected field name after '.'");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            arguments.add(expression());
            while(match(TokenType.COMMA)){
                arguments.add(expression());
            }
        }
        if(arguments.size() >= 255){
            error(peek(), "Function calls cannot have more than 255 arguments.");
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary(){
        if(match(TokenType.FALSE)){
            return new Expr.Literal(false);
        } else if(match(TokenType.TRUE)){
            return new Expr.Literal(true);
        } else if(match(TokenType.NIL)){
            return new Expr.Literal(null);
        } else if(match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().getLiteral());
        } else if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Missing ')' after expression");
            return new Expr.Grouping(expr);
        } else if(match(TokenType.THIS)) {
            return new Expr.This(previous());
        } else if(match(TokenType.SUPER)) {
            Token keyword = previous();
            consume(TokenType.DOT, "Expected '.' before superclass method name");
            Token method = consume(TokenType.IDENTIFIER, "Expected method name after 'super'");
            return new Expr.Super(keyword, method);
        } else if(match(TokenType.IDENTIFIER)){
            return new Expr.Var(previous());
        } else {
            throw error(peek(), "Unrecognized value, variable not defined");
        }
    }
}
