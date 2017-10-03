package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.ReturnStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisReturnStmt extends ThemisStmt implements ReturnStmt {
    private Expr expr;

    ThemisReturnStmt(Expr expr) {
        if (expr == null)
            throw new IllegalArgumentException();

        this.expr = expr;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getReturnValue() {
        return expr;
    }
}
