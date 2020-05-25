package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;

import java.util.HashMap;

/**
 * The only Env that can be accessed without requiring any distance specified
 */
public class GlobalEnv extends Env {
    private HashMap<String, Object> values = new HashMap<>();

    public void define(Token name, Object value){
        values.put(name.getLexeme(), value);
    }
    public void update(Token name, Object value){
        if(!values.containsKey(name.getLexeme())){
            throw new LoxRuntimeException(name, "Variable '" + name.getLexeme() + "' does not exist.");
        } else {
            values.put(name.getLexeme(), value);
        }
    }

    public Object get(Token name){
        if(!values.containsKey(name.getLexeme())){
            throw new LoxRuntimeException(name, "Variable '" + name.getLexeme() + "' undefined in scope.");
        } else {
            return values.get(name.getLexeme());
        }
    }
}
