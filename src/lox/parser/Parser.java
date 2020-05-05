package lox.parser;

import lox.Lox;
import lox.exception.ParseException;

import java.util.List;

// Formal grammar rules
/*
    expression     → equality ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    multiplication → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary
                   | primary ;
    primary        → NUMBER | STRING | "false" | "true" | "nil"
                   | "(" expression ")" ;
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

    public Expr parse(){
        try{
            return expression();
        } catch(ParseException e){
            return null;
        }
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

    private Expr expression(){
        return equality();
    }

    // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
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

    //     comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private Expr comparison(){
        Expr expr = addition();

        while(match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)){
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    //     addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    private Expr addition(){
        Expr expr = multiplication();

        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    //multiplication → unary ( ( "/" | "*" ) unary )* ;
    private Expr multiplication(){
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    //     unary          → ( "!" | "-" ) unary
    //                   | primary ;
    //    primary        → NUMBER | STRING | "false" | "true" | "nil"
    //                   | "(" expression ")" ;
    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)) {
            return new Expr.Unary(previous(), unary());
        } else {
            return primary();
        }
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
        } else if(match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Missing ')' after expression");
            return new Expr.Grouping(expr);
        } else {
            throw error(peek(), "Something has gone wrong with parsing!");
        }
    }
}
