package lox.parser;

public class ASTPrinter implements Expr.Visitor<String>{
    int depth = 0;
    public String print(Expr expr){
        return expr.accept(this);
    }

    private String paren(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for(Expr e: exprs){
            builder.append(" ");
            builder.append(e.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return paren(expr.operator.getLexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return paren("group",expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil": expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return paren(expr.operator.getLexeme(), expr.right);
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new ASTPrinter().print(expression));
    }
}
