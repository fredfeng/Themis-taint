package edu.utexas.libra.themis.ast.stmt;

import java.util.List;

public interface BlockStmt extends Stmt {
    List<Stmt> getBody();
}
