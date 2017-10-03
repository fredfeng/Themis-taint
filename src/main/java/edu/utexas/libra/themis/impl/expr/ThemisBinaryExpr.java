package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.BinaryExpr;
import edu.utexas.libra.themis.ast.expr.BinaryOperator;
import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisBinaryExpr extends ThemisExpr implements BinaryExpr {
    private BinaryOperator operator;
    private Expr first;
    private Expr second;

    ThemisBinaryExpr(BinaryOperator operator, Expr first, Expr second) {
        if (operator == null || first == null || second == null)
            throw new IllegalArgumentException();

        this.operator = operator;
        this.first = first;
        this.second = second;
    }


    @Override
    public BinaryOperator getOperator() {
        return operator;
    }

    @Override
    public Expr getFirstOperand() {
        return first;
    }

    @Override
    public Expr getSecondOperand() {
        return second;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
