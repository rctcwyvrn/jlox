package lox.execution;

public class FunctionReturn extends RuntimeException{
    final Object returnValue;
    public FunctionReturn(Object value){
        super(null, null, false, false);
        this.returnValue = value;
    }
}
