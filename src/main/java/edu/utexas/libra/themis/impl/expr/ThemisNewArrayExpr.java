package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.expr.NewArrayExpr;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.ExprVisitor;

public class ThemisNewArrayExpr extends ThemisExpr implements NewArrayExpr {
    private BasicType basicType;
    private Expr size;

    ThemisNewArrayExpr(BasicType basicType, Expr size) {
        this.basicType = basicType;
        this.size = size;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public BasicType getElementType() {
        return basicType;
    }

    @Override
    public Expr getSize() {
        return size;
    }
}
