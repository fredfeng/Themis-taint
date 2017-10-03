package edu.utexas.libra.themis.visitor;

import edu.utexas.libra.themis.ast.stmt.*;

public interface StmtVisitor<T> {
    T visit(SkipStmt stmt);
    T visit(BreakStmt stmt);
    T visit(ReturnStmt stmt);
    T visit(AssertStmt stmt);
    T visit(AssumeStmt stmt);
    T visit(ConsumeStmt stmt);
    T visit(DeclareStmt stmt);
    T visit(AssignStmt stmt);
    T visit(BlockStmt stmt);
    T visit(IfStmt stmt);
    T visit(WhileStmt stmt);
}
