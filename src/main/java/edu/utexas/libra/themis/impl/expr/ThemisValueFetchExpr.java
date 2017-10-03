package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.ValueFetchExpr;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisValueFetchExpr extends ThemisExpr implements ValueFetchExpr{

    private Lvalue src;

    ThemisValueFetchExpr(Lvalue src) {
        if (src == null)
            throw new IllegalArgumentException();

        this.src = src;
    }

    @Override
    public Lvalue getSource() {
        return src;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
