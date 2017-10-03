package edu.utexas.libra.themis.visitor;

import edu.utexas.libra.themis.ast.expr.*;

public interface ExprVisitor<T> {
    T visit(UnitConstantExpr expr);
    T visit(BoolConstantExpr expr);
    T visit(IntConstantExpr expr);
    T visit(NondetConstantExpr expr);
    T visit(ValueFetchExpr expr);
    T visit(NewArrayExpr expr);
    T visit(MakeArrayExpr expr);
    T visit(UnaryExpr expr);
    T visit(BinaryExpr expr);
    T visit(CallExpr expr);
}
