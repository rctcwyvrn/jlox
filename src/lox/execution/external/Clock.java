package lox.execution.external;

import lox.execution.InterpreterVisitor;
import lox.execution.LoxCallable;

import java.util.List;

public class Clock implements LoxCallable {
    @Override
    public Object call(InterpreterVisitor interpreter, List<Object> args) {
        return (double) System.nanoTime() / (Math.pow(10,9));
    }

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public String toString() {
        return "LoxFunction{" +
                "name=" + "LoxClock" +
                '}';
    }
}
