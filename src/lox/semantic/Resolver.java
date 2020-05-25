package lox.semantic;

import lox.Lox;
import lox.execution.InterpreterVisitor;
import lox.parser.Expr;
import lox.parser.Stmt;
import lox.parser.Token;

import java.util.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    // Each scope is a map of variable names to a boolean representing initialized/not initialized
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    // Mirror of the scope stack, it contains a list of local variables used instead
    private final Stack<List<String>> variablesUsed = new Stack<>();
    private final Map<Expr,Integer> resolutions = new HashMap<>();
    private FunctionType currentFunction =  FunctionType.NONE;

    public Map<Expr,Integer> performResolve(List<Stmt> program){
        resolve(program);
        return resolutions;
    }

    private void beginScope(){
        scopes.push(new HashMap<>());
        variablesUsed.push(new ArrayList<>());
    }

    private void endScope(){
        Map<String, Boolean> allDefined = scopes.pop();
        List<String> used = variablesUsed.pop();
        for(String defined: allDefined.keySet()){
            if(!used.contains(defined)){
                Lox.error(-1, "Variable " + defined + " defined but not used"); // I could change a whole bunch of code to make this have a correct line number but honestly I can't be bothered
            }
        }
    }

    private void declare(Token name){
        if(scopes.isEmpty()) return; //global name
        Map<String,Boolean> scope = scopes.peek();

        if(scope.containsKey(name.getLexeme())){
            Lox.error(name, "Variable with this name has already been declared in this scope.");
        }
        scope.put(name.getLexeme(), false);
    }

    private void define(Token name){
        if(scopes.isEmpty()) return; //global name
        Map<String,Boolean> scope = scopes.peek();
        scope.put(name.getLexeme(), true);
    }

    private void resolve(List<Stmt> statements){
        for(Stmt stmt: statements){
            resolve(stmt);
        }
    }

    private void resolve(Stmt statement){
        statement.accept(this);
    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void resolveLocal(Expr expr, Token name){
        for(int i = scopes.size() - 1; i >=0; i--){
            if(scopes.get(i).containsKey(name.getLexeme())){
                resolutions.put(expr, scopes.size() - i - 1);
                variablesUsed.get(i).add(name.getLexeme());
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Fun function, FunctionType newType){
        beginScope();
        FunctionType enclosingType = currentFunction;
        currentFunction = newType;
        for(Token param: function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        currentFunction = enclosingType;
        endScope();
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVarExpr(Expr.Var expr) {
        if(!scopes.isEmpty() && scopes.peek().get(expr.name.getLexeme()) == Boolean.FALSE){
            Lox.error(expr.name, "Cannot read local cariable in its own initializer");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitLogicalBinaryExpr(Expr.LogicalBinary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.calle);
        for(Expr arg: expr.args){
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if(stmt.init != null){
            resolve(stmt.init);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitFunStmt(Stmt.Fun stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenCase);
        if(stmt.elseCase != null) {
            resolve(stmt.elseCase);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.cond);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(currentFunction == FunctionType.NONE){
            Lox.error(stmt.ret, "Cannot return from top-level scope.");
        }
        if(stmt.value != null){
            resolve(stmt.value);
        }
        return null;
    }
}
