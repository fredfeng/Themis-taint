package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.stmt.AssignStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisAssignStmt extends ThemisStmt implements AssignStmt {
    private Lvalue target;
    private Expr source;

    ThemisAssignStmt(Lvalue target, Expr source) {
        if (target == null || source == null)
            throw new IllegalArgumentException();

        this.target = target;
        this.source = source;
    }


    @Override
    public Lvalue getTarget() {
        return target;
    }

    @Override
    public Expr getSource() {
        return source;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
