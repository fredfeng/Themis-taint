package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.stmt.BreakStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisBreakStmt extends ThemisStmt implements BreakStmt {

    ThemisBreakStmt() {}

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
