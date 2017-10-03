package edu.utexas.libra.themis.impl.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.stmt.Stmt;

import java.util.List;

public class ThemisStmtBuilder {
    public static ThemisAssertStmt makeAssert(Expr expr) {
        return new ThemisAssertStmt(expr);
    }

    public static ThemisAssignStmt makeAssign(Lvalue lhs, Expr rhs) {
        return new ThemisAssignStmt(lhs, rhs);
    }

    public static ThemisAssumeStmt makeAssume(Expr expr) {
        return new ThemisAssumeStmt(expr);
    }

    public static ThemisBlockStmt makeBlock(List<Stmt> stmtList) {
        return new ThemisBlockStmt(stmtList);
    }

    public static ThemisBreakStmt makeBreak() {
        return new ThemisBreakStmt();
    }

    public static ThemisConsumeStmt makeConsume(Expr expr) {
        return new ThemisConsumeStmt(expr);
    }

    public static ThemisDeclareStmt makeDeclare(VarDecl decl, Expr init) {
        return new ThemisDeclareStmt(decl, init);
    }

    public static ThemisIfStmt makeIf(Expr cond, Stmt trueBranch, Stmt falseBranch) {
        return new ThemisIfStmt(cond, trueBranch, falseBranch);
    }

    public static ThemisReturnStmt makeReturn(Expr expr) {
        return new ThemisReturnStmt(expr);
    }

    public static ThemisSkipStmt makeSkip() {
        return new ThemisSkipStmt();
    }

    public static ThemisWhileStmt makeWhile(Expr cond, Stmt body) {
        return new ThemisWhileStmt(cond, body);
    }
}

