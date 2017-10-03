package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.BoolConstantExpr;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisBoolConstantExpr extends ThemisExpr implements BoolConstantExpr {

    private boolean value;

    ThemisBoolConstantExpr(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
