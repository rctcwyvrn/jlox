package lox.execution;

import java.util.List;
import java.util.Map;

/**
 * Call to generate a new instance of this class
 */
public class LoxClass implements LoxCallable{
    private final String name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name, Map<String, LoxFunction> methods){
        this.name = name;
        this.methods = methods;
    }

    @Override
    public Object call(InterpreterVisitor interpreter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this);
        return instance;
    }

    @Override
    public int getArity() {
        return 0; // TODO: Constructors
    }

    public boolean containsMethod(String name){
        return methods.keySet().contains(name);
    }

    public LoxFunction getMethod(String name){
        return methods.get(name);
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "LoxClass{" +
                "name='" + name + '\'' +
                '}';
    }
}
