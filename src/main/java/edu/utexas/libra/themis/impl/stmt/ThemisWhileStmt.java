package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.stmt.WhileStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisWhileStmt extends ThemisStmt implements WhileStmt {

    private Expr cond;
    private Stmt body;

    ThemisWhileStmt(Expr cond, Stmt body) {
        if (cond == null || body == null)
            throw new IllegalArgumentException();

        this.cond = cond;
        this.body = body;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getLoopCondition() {
        return cond;
    }

    @Override
    public Stmt getLoopBody() {
        return body;
    }
}
