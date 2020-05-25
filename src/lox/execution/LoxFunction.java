package lox.execution;

import lox.parser.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Fun declaration;

    public LoxFunction(Stmt.Fun fun){
        this.declaration = fun;
    }
    @Override
    public Object call(InterpreterVisitor interpreter, List<Object> args) {
        Env functionEnv = new Env(interpreter.getGlobals()); // Create a new env to define the parameter names to the argument values
        for(int i=0; i<declaration.params.size(); i++){
            functionEnv.define(declaration.params.get(i), args.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, functionEnv); // Discard the env from the callee and to go the new env (which only has the parameters + globals)
            return null;
        } catch (FunctionReturn ret) {
            return ret.returnValue;
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
}
