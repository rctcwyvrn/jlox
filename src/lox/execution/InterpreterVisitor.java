package lox.execution;

import lox.Lox;
import lox.exception.LoxRuntimeException;
import lox.parser.Expr;
import lox.parser.Token;

public class InterpreterVisitor implements Expr.Visitor<Object>{

    public void interpret(Expr expression){
        try{
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (LoxRuntimeException e){
            Lox.runtimeError(e);
        }
    }

    private String stringify(Object x) {
        if(x == null){
            return "nil";
        } else if (x instanceof Double){
            Double num = (Double) x;
            if(num == Math.floor(num)){
                return num.intValue() + "";
            } else {
                return num.toString();
            }
        } else {
            return x.toString();
        }
    }

    private Object evaluate(Expr expression){
        return expression.accept(this);
    }

    /**
     * False and null are falsey, everything else is truthy
     * @param val
     * @return
     */
    private boolean isTruthy(Object val){
        if(val == null) return false;
        if(val instanceof Boolean && !(Boolean) val) return false;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null){
            return right == null;
        }

        return left.equals(right);
    }

    private void checkNumberType(Token operator, Object operand){
        if(!(operand instanceof Double)){
            throw new LoxRuntimeException(operator, "Operand must be a number. Found: " + operand);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.getType()) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case PLUS:
                if(left instanceof Double && right instanceof Double){
                    return (double) left + (double) right;
                } else if(left instanceof String || right instanceof String){
                    return stringify(left) + stringify(right);
                } else {
                    throw new LoxRuntimeException(expr.operator, "Operands must be both numbers or one must be a string. Got: " + left + " and " + right);
                }
        }

        // Past this point, all binary operators involve two numbers, so we must check their types here
        // Throws a LoxRuntimeException if either is not an int
        checkNumberType(expr.operator, left);
        checkNumberType(expr.operator, right);

        switch(expr.operator.getType()) {
            case MINUS:
                return (double) left - (double) right;
            case SLASH:
                if((double) right == 0){
                    throw new LoxRuntimeException(expr.operator, "Division by zero");
                }
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;
        }

        throw new LoxRuntimeException(expr.operator, "Found unknown binary operator " + expr.operator);
    }


    @Override
    /**
     * Evaluate the internal expression and return it
     */
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    /**
     * Just pull out the literal value
     */
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right); // Evaluate the value inside the unary

        // Apply the unary
        switch(expr.operator.getType()){
            case MINUS:
                return -1 * (double) right;
            case BANG:
                return !isTruthy(right);
        }

        return null; // If we get here we somehow parsed something that definitely should not be a unary, into a unary
    }
}
