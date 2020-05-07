package lox.exception;

import lox.parser.Token;

public class LoxRuntimeException extends RuntimeException{
    private final Token token;

    public LoxRuntimeException(Token token, String message){
        super(message);
        this.token = token;
    }

    public Token getToken(){
        return token;
    }
}
