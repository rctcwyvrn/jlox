package lox.parser;

import java.util.List;

public abstract class Stmt implements ASTNode{
    public interface Visitor<R>{
        R visitVarStmt(Var stmt);
        R visitClassStmt(Class stmt);
        R visitFunStmt(Fun stmt);
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitReturnStmt(Return stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

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

    public static class Class extends Stmt {
        Class(Token name,List<Stmt.Fun> methods) {
            this.name=name;
            this.methods=methods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
        public final Token name;
        public final List<Stmt.Fun> methods;
    }

    public static class Fun extends Stmt {
        Fun(Token name,List<Token> params,List<Stmt> body) {
            this.name=name;
            this.params=params;
            this.body=body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunStmt(this);
        }
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;
    }

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

    public static class Return extends Stmt {
        Return(Token ret,Expr value) {
            this.ret=ret;
            this.value=value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
        public final Token ret;
        public final Expr value;
    }
}
