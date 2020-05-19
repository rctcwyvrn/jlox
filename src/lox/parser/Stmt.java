package lox.parser;

import java.util.List;

public abstract class Stmt implements ASTNode{
    public interface Visitor<R>{
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression=expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
        public final Expr expression;
    }

    public static class Print extends Stmt {
        Print(Expr expression) {
            this.expression=expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
        public final Expr expression;
    }

    // Statement defining a variable
    public static class Var extends Stmt {
        Var(Token name,Expr init) {
            this.name=name;
            this.init=init;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
        public final Token name;
        public final Expr init;
    }

    public static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements=statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
        public final List<Stmt> statements;
    }

    public static class If extends Stmt {
        If(Expr condition,Stmt thenCase,Stmt elseCase) {
            this.condition=condition;
            this.thenCase=thenCase;
            this.elseCase=elseCase;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
        public final Expr condition;
        public final Stmt thenCase;
        public final Stmt elseCase;
    }

    public static class While extends Stmt {
        While(Expr cond,Stmt body) {
            this.cond=cond;
            this.body=body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
        public final Expr cond;
        public final Stmt body;
    }
}
