package lox.parser;

import java.util.List;

public abstract class Expr implements ASTNode{
    public interface Visitor<R>{
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitVarExpr(Var expr);
        R visitAssignExpr(Assign expr);
        R visitLogicalBinaryExpr(LogicalBinary expr);
        R visitCallExpr(Call expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Assign extends Expr {
        Assign(Token name,Expr value) {
            this.name=name;
            this.value=value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
        public final Token name;
        public final Expr value;
    }

    public static class Binary extends Expr {
        Binary(Expr left,Token operator,Expr right) {
            this.left=left;
            this.operator=operator;
            this.right=right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression=expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
        public final Expr expression;
    }

    public static class Literal extends Expr {
        Literal(Object value) {
            this.value=value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
        public final Object value;
    }

    public static class Unary extends Expr {
        Unary(Token operator,Expr right) {
            this.operator=operator;
            this.right=right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
        public final Token operator;
        public final Expr right;
    }

    public static class Var extends Expr {
        Var(Token name) {
            this.name=name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarExpr(this);
        }
        public final Token name;
    }

    public static class LogicalBinary extends Expr {
        LogicalBinary(Expr left,Token operator,Expr right) {
            this.left=left;
            this.operator=operator;
            this.right=right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalBinaryExpr(this);
        }
        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static class Call extends Expr {
        Call(Expr calle, Token paren, List<Expr> args) {
            this.calle=calle;
            this.paren=paren;
            this.args=args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
        public final Expr calle;
        public final Token paren;
        public final List<Expr> args;
    }
}
