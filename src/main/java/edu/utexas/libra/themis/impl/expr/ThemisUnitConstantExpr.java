package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.UnitConstantExpr;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisUnitConstantExpr extends ThemisExpr implements UnitConstantExpr {

    ThemisUnitConstantExpr() {}

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
