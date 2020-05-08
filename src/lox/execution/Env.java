package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;

import java.util.HashMap;
import java.util.Map;

public class Env {
    private final Map<String, Object> values = new HashMap<>();
    private final Env enclosing;

    public Env(){
        enclosing = null;
    }

    public Env(Env enclosing){
        this.enclosing = enclosing;
    }

    public void define(Token name, Object value){
        if(values.containsKey(name.getLexeme())){
            throw new LoxRuntimeException(name, "Variable '" + name.getLexeme() + "' already defined.");
        }
        values.put(name.getLexeme(), value);
    }

    public void update(Token name, Object value){

        if(!values.containsKey(name.getLexeme())){
            if(enclosing == null){
                throw new LoxRuntimeException(name, "Variable '" + name.getLexeme() + "' does not exist.");
            } else {
                enclosing.update(name, value);
            }
        }else {
            values.put(name.getLexeme(), value);
        }
    }

    public Object get(Token name){
        if(!values.containsKey(name.getLexeme())){
            if(enclosing == null) {
                throw new LoxRuntimeException(name, "Variable '" + name.getLexeme() + "' undefined in scope.");
            } else {
                return enclosing.get(name);
            }
        } else {
            return values.get(name.getLexeme());
        }
    }
}
