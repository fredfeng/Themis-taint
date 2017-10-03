package edu.utexas.libra.themis.builder;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.ast.expr.*;
import edu.utexas.libra.themis.ast.func.*;
import edu.utexas.libra.themis.ast.lvalue.*;
import edu.utexas.libra.themis.ast.stmt.*;
import edu.utexas.libra.themis.ast.type.*;

import java.util.Collection;
import java.util.List;

/**
 * Builder interface for Themis IR
 */
public interface AstBuilder {
    /**
     * Construct a program from a list of functions.
     * The function with name "main" is treated as entry function
     * @param functions The list of functions
     * @return A Themis program AST
     * @throws IllegalArgumentException if the list is empty, or if the list contains more than one functions that share the same name
     */
    Program makeProgram(Collection<Function> functions);

    Function makeFunction(String name, List<Param> params, Type retType, Stmt body);
    VarDecl makeVariableDecl(Type type, String name);
    Param makeParam(VarDecl decl, ParamAnnotation annotation);

    SkipStmt makeSkipStmt();
    BreakStmt makeBreakStmt();
    ReturnStmt makeReturnStmt(Expr expr);
    AssertStmt makeAssertStmt(Expr expr);
    AssumeStmt makeAssumeStmt(Expr expr);
    ConsumeStmt makeConsumeStmt(Expr expr);
    DeclareStmt makeDeclareStmt(VarDecl decl, Expr initExpr);
    DeclareStmt makeDefaultDeclareStmt(VarDecl decl);
    AssignStmt makeAssignStmt(Lvalue target, Expr source);
    BlockStmt makeBlockStmt(List<Stmt> stmts);
    IfStmt makeIfStmt(Expr cond, Stmt trueBranch, Stmt falseBranch);
    WhileStmt makeWhileStmt(Expr cond, Stmt body);

    UnitConstantExpr makeUnitConstantExpr();
    BoolConstantExpr makeBoolConstantExpr(boolean value);
    IntConstantExpr makeIntConstantExpr(int value);
    NondetConstantExpr makeNondetConstantExpr(Type type);
    NewArrayExpr makeNewArrayExpr(BasicType elemType, Expr size);
    MakeArrayExpr makeMakeArrayExpr(BasicType elemType, List<Expr> elems);
    ValueFetchExpr makeValueFetchExpr(Lvalue source);
    UnaryExpr makeUnaryExpr(UnaryOperator operator, Expr operand);
    BinaryExpr makeBinaryExpr(BinaryOperator operator, Expr first, Expr second);
    CallExpr makeCallExpr(String calleeName, List<Expr> args);

    VariableRef makeVariableRef(String name);
    ArrayRef makeArrayRef(String name, Expr index);

    BasicType makeUnitType();
    BasicType makeBoolType();
    BasicType makeIntType();
    ArrayType makeArrayType(BasicType elemType);
}