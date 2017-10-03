package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.visitor.StmtVisitor;

public interface Stmt {
    <T> T accept(StmtVisitor<T> visitor);
}
