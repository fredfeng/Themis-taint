package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.ConsumeStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisConsumeStmt extends ThemisStmt implements ConsumeStmt {
    private Expr expr;

    ThemisConsumeStmt(Expr expr) {
        if (expr == null)
            throw new IllegalArgumentException();

        this.expr = expr;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getConsumption() {
        return expr;
    }
}
