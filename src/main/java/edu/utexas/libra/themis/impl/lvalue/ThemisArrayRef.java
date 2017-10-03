package edu.utexas.libra.themis.impl.lvalue;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.lvalue.ArrayRef;
import edu.utexas.libra.themis.visitor.LvalueVisitor;

class ThemisArrayRef extends ThemisLvalue implements ArrayRef {

    private String baseName;
    private Expr indexExpr;

    ThemisArrayRef(String baseName, Expr indexExpr) {
        if (baseName == null || indexExpr == null)
            throw new IllegalArgumentException();

        this.baseName = baseName;
        this.indexExpr = indexExpr;
    }

    public String getName() {
        return baseName;
    }

    public Expr getIndex() {
        return indexExpr;
    }

    @Override
    public <T> T accept(LvalueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
