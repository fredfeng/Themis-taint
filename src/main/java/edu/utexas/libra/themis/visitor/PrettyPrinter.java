package edu.utexas.libra.themis.visitor;

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

import java.io.PrintWriter;
import java.util.List;

public class PrettyPrinter {

    private PrintWriter pw;
    private int indentLevel, indentWidth;

    private TypeVisitor<Void> typePrinter = new TypeVisitor<Void>() {

        @Override
        public Void visit(BasicType type) {
            switch (type.getTag()) {
                case UNIT:
                    pw.print("unit");
                    break;
                case BOOL:
                    pw.print("bool");
                    break;
                case INT:
                    pw.print("int");
                    break;
            }

            return null;
        }

        @Override
        public Void visit(ArrayType type) {
            visit(type.getElementType());
            pw.print("[]");
            return null;
        }
    };

    private LvalueVisitor<Void> lvaluePrinter = new LvalueVisitor<Void>() {
        @Override
        public Void visit(VariableRef varRef) {
            pw.print(varRef.getName());
            return null;
        }

        @Override
        public Void visit(ArrayRef arrayRef) {
            pw.print(arrayRef.getName());
            pw.print('[');
            printExpr(arrayRef.getIndex());
            pw.print(']');
            return null;
        }
    };

    private ExprVisitor<Void> exprPrinter = new ExprVisitor<Void>() {
        @Override
        public Void visit(UnitConstantExpr expr) {
            pw.print("()");
            return null;
        }

        @Override
        public Void visit(BoolConstantExpr expr) {
            if (expr.getValue())
                pw.print("true");
            else
                pw.print("false");
            return null;
        }

        @Override
        public Void visit(IntConstantExpr expr) {
            pw.print(expr.getValue());
            return null;
        }

        @Override
        public Void visit(NondetConstantExpr expr) {
            pw.print("nondet(");
            printType(expr.getType());
            pw.print(')');
            return null;
        }

        @Override
        public Void visit(ValueFetchExpr expr) {
            printLvalue(expr.getSource());
            return null;
        }

        @Override
        public Void visit(NewArrayExpr expr) {
            pw.print("newarray(");
            printType(expr.getElementType());
            pw.print(", ");
            printExpr(expr.getSize());
            pw.print(')');
            return null;
        }

        @Override
        public Void visit(MakeArrayExpr expr) {
            pw.print('(');
            printType(expr.getElementType());
            pw.print(")[");
            printExprList(expr.getElements());
            pw.print(']');
            return null;
        }

        @Override
        public Void visit(UnaryExpr expr) {
            switch (expr.getOperator()) {
                case NOT:
                    pw.print('!');
                    printExpr(expr.getOperand());
                    break;
                case LENGTHOF:
                    pw.print("length(");
                    printExpr(expr.getOperand());
                    pw.print(")");
                    break;
            }

            return null;
        }

        private void printBinaryOperator(BinaryOperator op) {
            switch (op) {
                case PLUS:
                    pw.print('+');
                    break;
                case MINUS:
                    pw.print('-');
                    break;
                case MUL:
                    pw.print('*');
                    break;
                case DIV:
                    pw.print('/');
                    break;
                case MOD:
                    pw.print('%');
                    break;
                case GT:
                    pw.print('>');
                    break;
                case GE:
                    pw.print(">=");
                    break;
                case LT:
                    pw.print('<');
                    break;
                case LE:
                    pw.print("<=");
                    break;
                case EQ:
                    pw.print("==");
                    break;
                case NE:
                    pw.print("!=");
                    break;
                case AND:
                    pw.print("&&");
                    break;
                case OR:
                    pw.print("||");
                    break;
            }
        }

        @Override
        public Void visit(BinaryExpr expr) {
            printExpr(expr.getFirstOperand());
            pw.print(' ');
            printBinaryOperator(expr.getOperator());
            pw.print(' ');
            printExpr(expr.getSecondOperand());
            return null;
        }

        @Override
        public Void visit(CallExpr expr) {
            pw.format("%s(", expr.getCalleeName());
            printExprList(expr.getArguments());
            pw.print(')');
            return null;
        }
    };

    private StmtVisitor<Void> stmtPrinter = new StmtVisitor<Void>() {
        @Override
        public Void visit(SkipStmt stmt) {
            pw.print("{}");
            return null;
        }

        @Override
        public Void visit(BreakStmt stmt) {
            pw.print("break;");
            return null;
        }

        @Override
        public Void visit(ReturnStmt stmt) {
            pw.print("return ");
            printExpr(stmt.getReturnValue());
            pw.print(';');
            return null;
        }

        @Override
        public Void visit(AssertStmt stmt) {
            pw.print("assert(");
            printExpr(stmt.getAssertion());
            pw.print(");");
            return null;
        }

        @Override
        public Void visit(AssumeStmt stmt) {
            pw.print("assume(");
            printExpr(stmt.getAssumption());
            pw.print(");");
            return null;
        }

        @Override
        public Void visit(ConsumeStmt stmt) {
            pw.print("consume(");
            printExpr(stmt.getConsumption());
            pw.print(");");
            return null;
        }

        @Override
        public Void visit(DeclareStmt stmt) {
            printVarDecl(stmt.getDelcaration());
            Expr initExpr = stmt.getInitExpr();
            if (initExpr != null) {
                pw.print(" = ");
                printExpr(initExpr);
            }
            pw.print(';');
            return null;
        }

        @Override
        public Void visit(AssignStmt stmt) {
            printLvalue(stmt.getTarget());
            pw.print(" = ");
            printExpr(stmt.getSource());
            pw.print(';');
            return null;
        }

        @Override
        public Void visit(BlockStmt stmt) {
            pw.print('{');
            pw.println();
            incIndent();
            for (Stmt s: stmt.getBody()) {
                printStmt(s);
            }
            decIndent();
            printIndent();
            pw.print('}');
            return null;
        }

        @Override
        public Void visit(IfStmt stmt) {
            pw.print("if (");
            printExpr(stmt.getBranchCondition());
            pw.print(')');
            pw.println();

            incIndent();
            printStmt(stmt.getTrueBranch());
            decIndent();

            printIndent();
            pw.print("else");
            pw.println();

            incIndent();
            printStmt(stmt.getFalseBranch());
            decIndent();

            return null;
        }

        @Override
        public Void visit(WhileStmt stmt) {
            pw.print("while (");
            printExpr(stmt.getLoopCondition());
            pw.print(')');
            pw.println();

            incIndent();
            printStmt(stmt.getLoopBody());
            decIndent();

            return null;
        }
    };

    private PrettyPrinter(PrintWriter pw, int indentLevel, int indentWidth) {
        this.pw = pw;
        this.indentLevel = indentLevel;
        this.indentWidth = indentWidth;
    }

    private void incIndent() {
        ++indentLevel;
    }
    private void decIndent() {
        --indentLevel;
    }

    private void printIndent() {
        int indent = indentLevel * indentWidth;
        if (indent > 0)
            pw.format("%" + indent + "s", "");
    }

    private void printType(Type type) {
        type.accept(typePrinter);
    }

    private void printAnnotation(ParamAnnotation annotation) {
        if (annotation.isLow()) {
            if (annotation.isHigh())
                pw.print("low high ");
            else
                pw.print("low ");
        }
        else if (annotation.isHigh())
            pw.print("high ");
    }

    private void printVarDecl(VarDecl decl) {
        printType(decl.getType());
        pw.format(" %s", decl.getName());
    }

    private void printParamList(List<Param> parameters) {
        pw.print("( ");
        String delim = "";
        for (Param param: parameters) {
            ParamAnnotation annotation = param.getAnnotation();
            VarDecl decl = param.getDecl();
            pw.print(delim);
            printAnnotation(annotation);
            printVarDecl(decl);

            delim = ", ";
        }
        pw.print(" )");
    }

    private void printExpr(Expr expr) {
        expr.accept(exprPrinter);
    }

    private void printExprList(List<Expr> exprs) {
        String delim = "";
        for (Expr expr: exprs) {
            pw.print(delim);
            printExpr(expr);

            delim = ", ";
        }
    }

    private void printLvalue(Lvalue lvalue) {
        lvalue.accept(lvaluePrinter);
    }

    private void printStmt(Stmt stmt) {
        printIndent();
        stmt.accept(stmtPrinter);
        pw.println();
    }

    private void printFunctionHeader(Function f) {
        printIndent();
        printType(f.getReturnType());
        pw.format(" %s ", f.getName());
        printParamList(f.getParameters());
        pw.println();
    }

    private void printFunctionBody(Function f) {
        Stmt bodyStmt = f.getFunctionBody();
        pw.println("{"); incIndent();
        if (bodyStmt instanceof BlockStmt) {
            BlockStmt blockStmt = (BlockStmt) bodyStmt;
            for (Stmt s: blockStmt.getBody())
                printStmt(s);
        } else
            printStmt(bodyStmt);
        pw.println("}"); decIndent();


    }

    private void doPrint(Program program) {
        for (Function f: program.getFunctionList()) {
            printFunctionHeader(f);
            printFunctionBody(f);
            pw.println();
        }
        pw.flush();
    }

    public static void print(PrintWriter pw, Program program) {
        print(pw, program, 0, 2);
    }

    public static void print(PrintWriter pw, Program program, int indentLevel) {
        print(pw, program, indentLevel, 2);
    }

    public static void print(PrintWriter pw, Program program, int indentLevel, int indentWidth) {
        new PrettyPrinter(pw, indentLevel, indentWidth).doPrint(program);
    }
}
