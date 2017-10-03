package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.NondetConstantExpr;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.visitor.ExprVisitor;

class ThemisNondetConstantExpr extends ThemisExpr implements NondetConstantExpr {

    private Type type;

    protected ThemisNondetConstantExpr(Type type) {
        if (type == null)
            throw new IllegalArgumentException();

        this.type = type;
    }

    public Type getType() { return type; }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
