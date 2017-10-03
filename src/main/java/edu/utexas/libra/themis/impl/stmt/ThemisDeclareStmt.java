package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.stmt.DeclareStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisDeclareStmt extends ThemisStmt implements DeclareStmt {
    private VarDecl varDecl;
    private Expr init;

    ThemisDeclareStmt(VarDecl varDecl, Expr init) {
        if (varDecl == null)
            throw new IllegalArgumentException();

        this.varDecl = varDecl;
        this.init = init;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public VarDecl getDelcaration() {
        return varDecl;
    }

    @Override
    public Expr getInitExpr() {
        return init;
    }
}
