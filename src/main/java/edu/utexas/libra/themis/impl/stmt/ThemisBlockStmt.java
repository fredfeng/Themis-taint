package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.stmt.BlockStmt;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

import java.util.Collections;
import java.util.List;

class ThemisBlockStmt extends ThemisStmt implements BlockStmt {
    private List<Stmt> stmts;

    ThemisBlockStmt(List<Stmt> stmts) {
        if (stmts == null)
            throw new IllegalArgumentException();

        this.stmts = Collections.unmodifiableList(stmts);
    }

    @Override
    public List<Stmt> getBody() {
        return stmts;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
