package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;

import java.util.HashMap;
import java.util.Map;

public class Env {
    protected final Map<String, Object> values = new HashMap<>();
    private final Env enclosing;

    public Env(){
        enclosing = null;
    }

    public Env(Env enclosing){
        this.enclosing = enclosing;
    }

    public void define(Token name, Object value){
        values.put(name.getLexeme(), value);
    }

    public void define(String name, Object value){
        values.put(name, value);
    }

    private Env ancestor(int distance){
        Env env = this;
        for(int i = 0; i < distance; i++){
            env = env.enclosing;
        }
        return env;
    }

    public Object getAt(int dist, String name){
        return ancestor(dist).values.get(name);
    }

    public void updateAt(int dist, Token name, Object val){
        ancestor(dist).values.put(name.getLexeme(), val);
    }
}
