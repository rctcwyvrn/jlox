package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;

/**
 * The only Env that can be accessed without requiring any distance specified
 */
public class GlobalEnv extends Env {
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
