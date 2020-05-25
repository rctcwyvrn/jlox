package lox.execution;

import lox.Lox;
import lox.exception.LoxRuntimeException;
import lox.execution.external.Clock;
import lox.parser.Expr;
import lox.parser.Stmt;
import lox.parser.Token;
import lox.parser.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tree walk interpreter using the Visitor pattern
 */
public class InterpreterVisitor implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    private final GlobalEnv globals = new GlobalEnv();
    private Env env = globals;
    private Map<Expr, Integer> locals = new HashMap<>();
    private boolean isREPL = true;

    public InterpreterVisitor(){
        env.define(new Token(null, "clock", null, -1), new Clock());
    }

    public InterpreterVisitor(Map<Expr, Integer> resolutions){
        this();
        this.locals = resolutions;
        this.isREPL = false;
    }

    public Env getGlobals(){
        return globals;
    }

    public void interpret(List<Stmt> program){
        try{
            for(Stmt statement: program){
                execute(statement);
            }
        } catch (LoxRuntimeException e){
            Lox.runtimeError(e);
        }
    }

    public void executeBlock(List<Stmt> body, Env env){
        Env enclosing = this.env;
        try {
            this.env = env;
            for(Stmt statement: body){
                execute(statement);
            }
        } finally {
            this.env = enclosing;
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

    public Void execute(Stmt statement) {return statement.accept(this);}
    public Object evaluate(Expr expression){
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

    private Object lookUpVariable(Token name, Expr expr){
        Integer dist = locals.get(expr);
        if(dist != null){
            return env.getAt(dist, name.getLexeme());
//        } else if(isREPL){
//            return env.get(name);
        } else {
            return globals.get(name);
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

    @Override
    public Object visitVarExpr(Expr.Var expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object val = evaluate(expr.value);
        Integer dist = locals.get(expr);
        if(dist != null){
            env.updateAt(dist, expr.name, val);
//        } else if(isREPL) {
//            env.update(expr.name, val);
        } else {
            globals.update(expr.name, val);
        }
        return val;
    }

    @Override
    public Object visitLogicalBinaryExpr(Expr.LogicalBinary expr) {
        Object left = evaluate(expr.left);
        if(expr.operator.getType() == TokenType.OR){
            if(isTruthy(left)){
                return true;
            } else {
                return isTruthy(evaluate(expr.right));
            }
        } else {
            if(!isTruthy(left)){
                return false;
            } else {
                return isTruthy(evaluate(expr.right));
            }
        }
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.calle);
        List<Object> arguments = expr.args.stream().map(this::evaluate).collect(Collectors.toList());
        if(!(callee instanceof LoxCallable))
            throw new LoxRuntimeException(expr.paren, "Object {" + callee + "} is not callable. Only functions and classes are callable");
        LoxCallable fun = (LoxCallable) callee;
        if(arguments.size() != fun.getArity())
            throw new LoxRuntimeException(expr.paren, "Expected " + fun.getArity() + " arguments, got " + arguments.size() + ": " + arguments + " instead");
        return fun.call(this, arguments);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object toPrint = evaluate(stmt.expression);
        System.out.println(stringify(toPrint));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if(stmt.init != null){
            env.define(stmt.name, evaluate(stmt.init));
        } else {
            env.define(stmt.name, null);
        }

        return null;
    }

    @Override
    public Void visitFunStmt(Stmt.Fun stmt) {
        LoxFunction fun = new LoxFunction(stmt, this.env); // Uses the env (all defined names) that are present when the function is defined
        env.define(stmt.name, fun); // Add to the env
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Env(this.env));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object cond = evaluate(stmt.condition);
        if(isTruthy(cond)){
            execute(stmt.thenCase);
        } else if (stmt.elseCase != null){
            execute(stmt.elseCase);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while(isTruthy(evaluate(stmt.cond))){
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object returnValue = stmt.value == null ? null: evaluate(stmt.value);
        throw new FunctionReturn(returnValue);
    }
}
