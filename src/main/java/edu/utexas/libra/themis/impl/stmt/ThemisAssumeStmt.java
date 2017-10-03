package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.AssumeStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisAssumeStmt extends ThemisStmt implements AssumeStmt {
    private Expr expr;

    ThemisAssumeStmt(Expr expr) {
        if (expr == null)
            throw new IllegalArgumentException();

        this.expr = expr;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getAssumption() {
        return expr;
    }
}
