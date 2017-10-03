package edu.utexas.libra.themis.builder;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.ast.expr.*;
import edu.utexas.libra.themis.ast.func.Function;
import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.lvalue.ArrayRef;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.lvalue.VariableRef;
import edu.utexas.libra.themis.ast.stmt.*;
import edu.utexas.libra.themis.ast.type.ArrayType;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.impl.ThemisProgramBuilder;
import edu.utexas.libra.themis.impl.expr.ThemisExprBuilder;
import edu.utexas.libra.themis.impl.func.ThemisFunctionBuilder;
import edu.utexas.libra.themis.impl.lvalue.ThemisLvalueBuilder;
import edu.utexas.libra.themis.impl.stmt.ThemisStmtBuilder;
import edu.utexas.libra.themis.impl.type.ThemisTypeBuilder;

import java.util.Collection;
import java.util.List;

public class ThemisAstBuilder implements AstBuilder {

    @Override
    public Program makeProgram(Collection<Function> functions) {
        return ThemisProgramBuilder.makeProgram(functions);
    }

    @Override
    public Function makeFunction(String name, List<Param> params, Type
            retType, Stmt body) {
        return ThemisFunctionBuilder.makeFunction(name, params, retType, body);
    }

    @Override
    public VarDecl makeVariableDecl(Type type, String name) {
        return ThemisFunctionBuilder.makeVarDecl(type, name);
    }

    @Override
    public Param makeParam(VarDecl decl, ParamAnnotation annotation) {
        return ThemisFunctionBuilder.makeParam(decl, annotation);
    }

    @Override
    public SkipStmt makeSkipStmt() {
        return ThemisStmtBuilder.makeSkip();
    }

    @Override
    public BreakStmt makeBreakStmt() {
        return ThemisStmtBuilder.makeBreak();
    }

    @Override
    public ReturnStmt makeReturnStmt(Expr expr) {
        return ThemisStmtBuilder.makeReturn(expr);
    }

    @Override
    public AssertStmt makeAssertStmt(Expr expr) {
        return ThemisStmtBuilder.makeAssert(expr);
    }

    @Override
    public AssumeStmt makeAssumeStmt(Expr expr) {
        return ThemisStmtBuilder.makeAssume(expr);
    }

    @Override
    public ConsumeStmt makeConsumeStmt(Expr expr) {
        return ThemisStmtBuilder.makeConsume(expr);
    }

    @Override
    public DeclareStmt makeDeclareStmt(VarDecl decl, Expr initExpr) {
        return ThemisStmtBuilder.makeDeclare(decl, initExpr);
    }

    @Override
    public DeclareStmt makeDefaultDeclareStmt(VarDecl decl) {
        return ThemisStmtBuilder.makeDeclare(decl, null);
    }

    @Override
    public AssignStmt makeAssignStmt(Lvalue target, Expr source) {
        return ThemisStmtBuilder.makeAssign(target, source);
    }

    @Override
    public BlockStmt makeBlockStmt(List<Stmt> stmts) {
        return ThemisStmtBuilder.makeBlock(stmts);
    }

    @Override
    public IfStmt makeIfStmt(Expr cond, Stmt trueBranch, Stmt falseBranch) {
        return ThemisStmtBuilder.makeIf(cond, trueBranch, falseBranch);
    }

    @Override
    public WhileStmt makeWhileStmt(Expr cond, Stmt body) {
        return ThemisStmtBuilder.makeWhile(cond, body);
    }

    @Override
    public UnitConstantExpr makeUnitConstantExpr() {
        return ThemisExprBuilder.makeUnitConst();
    }

    @Override
    public BoolConstantExpr makeBoolConstantExpr(boolean value) {
        return ThemisExprBuilder.makeBoolConst(value);
    }

    @Override
    public IntConstantExpr makeIntConstantExpr(int value) {
        return ThemisExprBuilder.makeIntConst(value);
    }

    @Override
    public NondetConstantExpr makeNondetConstantExpr(Type type) {
        return ThemisExprBuilder.makeNondetConst(type);
    }

    @Override
    public NewArrayExpr makeNewArrayExpr(BasicType elemType, Expr size) {
        return ThemisExprBuilder.makeNewArray(elemType, size);
    }

    @Override
    public MakeArrayExpr makeMakeArrayExpr(BasicType elemType, List<Expr> elems) {
        return ThemisExprBuilder.makeMakeArray(elemType, elems);
    }

    @Override
    public ValueFetchExpr makeValueFetchExpr(Lvalue source) {
        return ThemisExprBuilder.makeValueFetch(source);
    }

    @Override
    public UnaryExpr makeUnaryExpr(UnaryOperator operator, Expr operand) {
        return ThemisExprBuilder.makeUnary(operator, operand);
    }

    @Override
    public BinaryExpr makeBinaryExpr(BinaryOperator operator, Expr first, Expr second) {
        return ThemisExprBuilder.makeBinary(operator, first, second);
    }

    @Override
    public CallExpr makeCallExpr(String calleeName, List<Expr> args) {
        return ThemisExprBuilder.makeCall(calleeName, args);
    }

    @Override
    public VariableRef makeVariableRef(String name) {
        return ThemisLvalueBuilder.makeVariableRef(name);
    }

    @Override
    public ArrayRef makeArrayRef(String name, Expr index) {
        return ThemisLvalueBuilder.makeArrayRef(name, index);
    }

    @Override
    public BasicType makeUnitType() {
        return ThemisTypeBuilder.makeUnitType();
    }

    @Override
    public BasicType makeBoolType() {
        return ThemisTypeBuilder.makeBoolType();
    }

    @Override
    public BasicType makeIntType() {
        return ThemisTypeBuilder.makeIntType();
    }

    @Override
    public ArrayType makeArrayType(BasicType elemType) {
        return ThemisTypeBuilder.makeArrayType(elemType);
    }
}
