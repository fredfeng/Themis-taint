package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.AssertStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisAssertStmt extends ThemisStmt implements AssertStmt {
    private Expr expr;

    ThemisAssertStmt(Expr expr) {
        if (expr == null)
            throw new IllegalArgumentException();

        this.expr = expr;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getAssertion() {
        return expr;
    }
}
