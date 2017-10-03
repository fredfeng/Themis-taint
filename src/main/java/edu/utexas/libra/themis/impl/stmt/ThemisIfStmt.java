package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.stmt.IfStmt;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisIfStmt extends ThemisStmt implements IfStmt {
    private Expr cond;
    private Stmt trueBranch;
    private Stmt falseBranch;

    ThemisIfStmt(Expr cond, Stmt trueBranch, Stmt falseBranch) {
        if (cond == null || trueBranch == null || falseBranch == null)
            throw new IllegalArgumentException();

        this.cond = cond;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expr getBranchCondition() {
        return cond;
    }

    @Override
    public Stmt getTrueBranch() {
        return trueBranch;
    }

    @Override
    public Stmt getFalseBranch() {
        return falseBranch;
    }
}
