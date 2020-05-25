package lox.execution;

import java.util.List;

public interface LoxCallable {
    Object call(InterpreterVisitor interpreter, List<Object> args);
    int getArity();
}
