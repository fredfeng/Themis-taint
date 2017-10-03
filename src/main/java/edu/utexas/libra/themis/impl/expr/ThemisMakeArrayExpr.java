package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.expr.MakeArrayExpr;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.ExprVisitor;

import java.util.Collections;
import java.util.List;

class ThemisMakeArrayExpr extends ThemisExpr implements MakeArrayExpr {

    private BasicType elemType;
    private List<Expr> elems;

    ThemisMakeArrayExpr(BasicType elemType, List<Expr> elems) {
        if (elemType == null || elems == null)
            throw new IllegalArgumentException();

        this.elemType = elemType;
        this.elems = Collections.unmodifiableList(elems);
    }

    @Override
    public BasicType getElementType() {
        return elemType;
    }

    @Override
    public List<Expr> getElements() {
        return elems;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
