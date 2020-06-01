package lox.execution;

import lox.parser.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Fun declaration;
    private final Env closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Fun fun, Env closure, boolean isInitializer){
        this.declaration = fun;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(InterpreterVisitor interpreter, List<Object> args) {
        Env functionEnv = new Env(closure); // Create a new env to define the parameter names to the argument values, parent is the env present during function def
        for(int i=0; i<declaration.params.size(); i++){
            functionEnv.define(declaration.params.get(i), args.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, functionEnv); // Discard the env from the callee and to go the new env (which only has the parameters + globals)
            if(isInitializer){
                return closure.getAt(0, "this");
            } else {
                return null;
            }
        } catch (FunctionReturn ret) {
            if(isInitializer) { // The resolver handles making sure the user doesn't try to return a value from the init
                return closure.getAt(0, "this");
            } else {
                return ret.returnValue;
            }
        }
    }

    @Override
    public int getArity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "LoxFunction{" +
                "name=" + declaration.name +
                '}';
    }

    public LoxFunction bind(LoxInstance instance) {
        Env bindedEnv = new Env(closure);
        bindedEnv.define("this", instance);
        return new LoxFunction(declaration, bindedEnv, isInitializer);
    }
}
