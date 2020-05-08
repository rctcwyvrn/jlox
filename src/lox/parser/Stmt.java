package lox.parser;

import java.util.List;

public abstract class Stmt implements ASTNode{
    public interface Visitor<R>{
        R visitExpressionStmt(Stmt.Expression expr);
        R visitPrintStmt(Stmt.Print expr);
        R visitVarStmt(Var expr);
        R visitBlockStmt(Block expr);
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
}
