package lox.execution;

import java.util.List;
import java.util.Map;

/**
 * Call to generate a new instance of this class
 */
public class LoxClass implements LoxCallable{
    private final String name;
    private final Map<String, LoxFunction> methods;
    private final LoxClass superclass;

    public LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods){
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    @Override
    public Object call(InterpreterVisitor interpreter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this);
        // Run constructor (init())
        if(containsMethod("init")){
            LoxFunction initializer = getMethod("init");
            initializer.bind(instance).call(interpreter, args); // Bind to be able to update the instance object
            // The return value is the LoxInstance so we just discard it
        }
        return instance;
    }

    @Override
    public int getArity() {
        if(containsMethod("init")){
            return getMethod("init").getArity();
        } else {
            return 0;
        }
    }

    public boolean containsMethod(String name){
        if(superclass != null){
            return methods.containsKey(name) || superclass.containsMethod(name);
        } else {
            return methods.containsKey(name);
        }
    }

    public LoxFunction getMethod(String name){
        if(!methods.containsKey(name) && superclass != null){ // Check first to see if the method is overridden
            return superclass.getMethod(name);
        } else {
            return methods.get(name); // could we have just binded super to this.superclass right here and be done with it?
        }
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
