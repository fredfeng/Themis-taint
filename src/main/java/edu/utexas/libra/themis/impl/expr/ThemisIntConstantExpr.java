package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.IntConstantExpr;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisIntConstantExpr extends ThemisExpr implements IntConstantExpr {

    private int value;

    ThemisIntConstantExpr(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
