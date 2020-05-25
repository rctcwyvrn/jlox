package lox.execution;

import lox.exception.LoxRuntimeException;
import lox.parser.Token;
import lox.semantic.Resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    protected final Object[] values;
    private final Env enclosing;

    // Only for GlobalEnv
    protected Env(){
        enclosing = null;
        values = null;
    }

    public Env(Env enclosing){
        this.enclosing = enclosing;
        this.values = new Object[256];
    }

    public void define(int index, Object value){
        values[index] =  value;
    }

    private Env ancestor(int distance){
        Env env = this;
        for(int i = 0; i < distance; i++){
            env = env.enclosing;
        }
        return env;
    }

    public Object getAt(Resolver.Destination dest){
        return ancestor(dest.depth).values[dest.index];
    }

    public void updateAt(Resolver.Destination dest, Object val){
        ancestor(dest.depth).values[dest.index] = val;
    }
}
