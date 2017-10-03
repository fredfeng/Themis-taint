package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.expr.UnaryExpr;
import edu.utexas.libra.themis.ast.expr.UnaryOperator;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisUnaryExpr extends ThemisExpr implements UnaryExpr {
    private UnaryOperator operator;
    private Expr operand;

    ThemisUnaryExpr(UnaryOperator operator, Expr operand) {
        if (operator == null || operand == null)
            throw new IllegalArgumentException();

        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public Expr getOperand() {
        return operand;
    }

    @Override
    public UnaryOperator getOperator() {
        return operator;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
