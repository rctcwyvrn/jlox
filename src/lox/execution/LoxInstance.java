package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();
    public LoxInstance(LoxClass klass){
        this.klass = klass;
    }

    public Object get(Token name){
        if (fields.containsKey(name.getLexeme())){
            return fields.get(name.getLexeme());
        } else if(klass.containsMethod(name.getLexeme())){
            return klass.getMethod(name.getLexeme()).bind(this);  // Return a new LoxFunction object with 'this' binded
        }
        throw new LoxRuntimeException(name, "Instance does not contain property '" + name.getLexeme() + "'");
    }

    public void set(Token name, Object value){
        fields.put(name.getLexeme(), value);
    }

    @Override
    public String toString() {
        return "LoxInstance{" +
                "instanceof='" + klass.getName() + "', " +
                "fields=" + fields.entrySet() +
                '}';
    }
}
