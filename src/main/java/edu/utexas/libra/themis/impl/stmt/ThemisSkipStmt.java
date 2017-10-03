package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.stmt.SkipStmt;
import edu.utexas.libra.themis.visitor.StmtVisitor;

class ThemisSkipStmt extends ThemisStmt implements SkipStmt {

    ThemisSkipStmt() {}

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
