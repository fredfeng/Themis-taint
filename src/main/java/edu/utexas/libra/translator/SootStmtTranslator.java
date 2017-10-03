package edu.utexas.libra.translator;

import edu.utexas.libra.themis.ast.expr.BinaryOperator;
import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.expr.UnaryOperator;
import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.stmt.ConsumeStmt;
import edu.utexas.libra.themis.ast.stmt.DeclareStmt;
import edu.utexas.libra.themis.ast.stmt.SkipStmt;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.builder.AstBuilder;
import edu.utexas.libra.translator.block.*;
import edu.utexas.libra.translator.util.SootAllocSiteMap;
import edu.utexas.libra.translator.util.SootFieldMap;
import edu.utexas.libra.translator.util.SootLocalMap;
import edu.utexas.libra.translator.util.SootParamMap;
import soot.*;
import soot.jimple.*;

import java.util.*;
import java.util.stream.Collectors;

public class SootStmtTranslator {

    private static String DUMMY_RETURN_PREFIX = "__dummy_ret_";
    private static String VAR_PTSTO_NAME = "__varptsto";
    private static String INSTANCE_FIELD_LOAD = "__ifield_load_int";
    private static String INSTANCE_FIELD_STORE = "__ifield_store_int";
    private static String STATIC_FIELD_LOAD = "__sfield_load_int";
    private static String STATIC_FIELD_STORE = "__sfield_store_int";

    private AstBuilder astBuilder;
    private SootTypeTranslator typeTranslator;
    private SootMethod method;
    private SootParamMap paramMap;
    private SootLocalMap localMap;
    private SootAllocSiteMap allocSiteMap;
    private SootFieldMap fieldMap;
    private List<Stmt> resultStmts;

    public SootStmtTranslator(AstBuilder astBuilder, SootTypeTranslator typeTranslator, SootFieldMap fieldMap, SootAllocSiteMap allocSiteMap, SootParamMap paramMap, SootLocalMap localMap, SootMethod method, List<Stmt> resultStmts) {
        this.astBuilder = astBuilder;
        this.typeTranslator = typeTranslator;
        this.method = method;
        this.paramMap = paramMap;
        this.localMap = localMap;
        this.allocSiteMap = allocSiteMap;
        this.fieldMap = fieldMap;
        this.resultStmts = resultStmts;
    }

    public static String translateLocalName(Local local) {
        return local.getName().replaceAll("[^\\w]", "_");
    }

    private class StmtVisitor implements StmtSwitch {

        @Override
        public void caseBreakpointStmt(BreakpointStmt breakpointStmt) {
            throw new RuntimeException("Breakpoint stmt not supported");
        }

        @Override
        public void caseInvokeStmt(InvokeStmt invokeStmt) {
            // Hack: assume all calls are direct calls
            InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            Expr expr = translateInvokeExpr(invokeExpr);
            Type retType = typeTranslator.translateType(invokeExpr.getType());
            resultStmts.add(astBuilder.makeAssignStmt(getDummyLvalue(retType), expr));
        }

        private void caseAssignToLocal(Local lhs, Value rhs) {
            Expr expr = translateValue(rhs);
            if (expr != null) {
                String lhsName = translateLocalName(lhs);
                Lvalue lval = astBuilder.makeVariableRef(lhsName);
                resultStmts.add(astBuilder.makeAssignStmt(lval, expr));
            }
        }

        private void caseAssignToNonLocal(Value lhs, Value rhs) {
            if (lhs instanceof StaticFieldRef) {
                StaticFieldRef sFieldRef = (StaticFieldRef) lhs;

                soot.Type lhsType = lhs.getType();
                // Non-primitive store gets handled by pointer analysis
                if (!(lhsType instanceof PrimType))
                    return;

                int fieldId = fieldMap.getOrAddField(sFieldRef.getField());
                Expr fieldIdConst = astBuilder.makeIntConstantExpr(fieldId);
                Expr src = translateValue(rhs);
                Objects.requireNonNull(src);

                Expr callExpr = astBuilder.makeCallExpr(STATIC_FIELD_STORE, Arrays.asList(fieldIdConst, src));
                resultStmts.add(astBuilder.makeAssignStmt(getDummyLvalue(astBuilder.makeUnitType()), callExpr));
            }
            else if (lhs instanceof InstanceFieldRef) {
                InstanceFieldRef iFieldRef = (InstanceFieldRef) lhs;
                soot.Type lhsType = lhs.getType();
                // Non-primitive store gets handled by pointer analysis
                if (!(lhsType instanceof PrimType))
                    return;

                Value baseValue = iFieldRef.getBase();
                if (baseValue instanceof Local) {
                    Expr baseExpr = translateValue(baseValue);
                    int fieldId = fieldMap.getOrAddField(iFieldRef.getField());
                    Expr fieldIdConst = astBuilder.makeIntConstantExpr(fieldId);
                    Expr src = translateValue(rhs);
                    Objects.requireNonNull(src);
                    Expr callExpr = astBuilder.makeCallExpr(INSTANCE_FIELD_STORE, Arrays.asList(
                        baseExpr, fieldIdConst, src
                    ));
                    resultStmts.add(astBuilder.makeAssignStmt(getDummyLvalue(astBuilder.makeUnitType()), callExpr));
                } else
                    throw new RuntimeException("non-local field base not supported");
            }

            else if (lhs instanceof ArrayRef) {
                Expr expr = translateValue(rhs);
                if (expr != null) {
                    ArrayRef arrayRef = (ArrayRef) lhs;
                    Lvalue lval = translateArrayRef(arrayRef);
                    resultStmts.add(astBuilder.makeAssignStmt(lval, expr));
                }
            }

            else
                throw new RuntimeException("Assign lhs not supported: " + lhs);
        }

        @Override
        public void caseAssignStmt(AssignStmt assignStmt) {
            Value lhs = assignStmt.getLeftOp();
            Value rhs = assignStmt.getRightOp();
            if (lhs instanceof Local)
            {
                Local local = (Local) lhs;
                addLocal(local);
                caseAssignToLocal(local, rhs);
            } else
                caseAssignToNonLocal(lhs, rhs);
        }

        @Override
        public void caseIdentityStmt(IdentityStmt identityStmt) {
            Value lhs = identityStmt.getLeftOp();
            Value rhs = identityStmt.getRightOp();

            if(lhs instanceof Local && rhs instanceof ThisRef)
            {
                Local local = (Local) lhs;
                soot.Type localType = local.getType();

                Type themisType = typeTranslator.translateType(localType);
                String name = translateLocalName(local);
                Param param = astBuilder.makeParam(astBuilder.makeVariableDecl(themisType, name), ParamAnnotation.NONE);
                paramMap.addParam(0, param);
            }
            else if(lhs instanceof Local && rhs instanceof ParameterRef)
            {
                Local local = (Local) lhs;
                ParameterRef parameterRef = (ParameterRef) rhs;
                soot.Type localType = local.getType();

                Type themisType = typeTranslator.translateType(localType);
                String name = translateLocalName(local);
                int index = parameterRef.getIndex();
                if (!method.isStatic())
                    index += 1;
                Param param = astBuilder.makeParam(astBuilder.makeVariableDecl(themisType, name), ParamAnnotation.NONE);
                paramMap.addParam(index, param);
            }
            else
            {
                throw new RuntimeException("Unsupported identity statement: " + identityStmt);
            }
        }

        @Override
        public void caseEnterMonitorStmt(EnterMonitorStmt enterMonitorStmt) {
            // Ignore entermonitor
        }

        @Override
        public void caseExitMonitorStmt(ExitMonitorStmt exitMonitorStmt) {
            // Ignore exitmonitor
        }

        @Override
        public void caseGotoStmt(GotoStmt gotoStmt) {
            // Ignore goto
        }

        @Override
        public void caseIfStmt(IfStmt ifStmt) {
            throw new RuntimeException("Ifstmt not supported");
        }

        @Override
        public void caseLookupSwitchStmt(LookupSwitchStmt lookupSwitchStmt) {
            throw new RuntimeException("Lookupswitch not supported yet");
        }

        @Override
        public void caseNopStmt(NopStmt nopStmt) {
            // Ignore no-op
        }

        @Override
        public void caseRetStmt(RetStmt retStmt) {
            throw new RuntimeException("RetStmt not supported");
        }

        @Override
        public void caseReturnStmt(ReturnStmt returnStmt) {
            Value retVal = returnStmt.getOp();
            Expr retExpr = translateValue(retVal);
            Objects.requireNonNull(retExpr);
            Stmt retStmt = astBuilder.makeReturnStmt(retExpr);
            resultStmts.add(retStmt);
        }

        @Override
        public void caseReturnVoidStmt(ReturnVoidStmt returnVoidStmt) {
            Stmt retStmt = astBuilder.makeReturnStmt(astBuilder.makeUnitConstantExpr());
            resultStmts.add(retStmt);
        }

        @Override
        public void caseTableSwitchStmt(TableSwitchStmt tableSwitchStmt) {
            throw new RuntimeException("Tableswitch not supported yet");
        }

        @Override
        public void caseThrowStmt(ThrowStmt throwStmt) {
            // Hack: ignore throw stmts for now
        }

        @Override
        public void defaultCase(Object o) {
            throw new RuntimeException("Unit not translatable: " + o.getClass().getName());
        }
    }

    private void doInstrument(Unit unit) {
        // TODO: make the instrumentation policy user-controllable
        if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) unit;
            Value rhs = assignStmt.getRightOp();
            if (!(rhs instanceof InvokeExpr)) {
                ConsumeStmt stmt = astBuilder.makeConsumeStmt(astBuilder.makeIntConstantExpr(1));
                resultStmts.add(stmt);
            }
        }
    }
    private StmtVisitor stmtVisitor = new StmtVisitor();

    public void translateUnit(Unit unit) {
        unit.apply(stmtVisitor);
        doInstrument(unit);
    }

    private void addLocal(Local local) {
        String localName = translateLocalName(local);
        if (!localMap.hasDeclare(localName)) {
            Type localType = typeTranslator.translateType(local.getType());
            localMap.addDeclare(localType, localName);
        }
    }

    private Lvalue getDummyLvalue(Type type) {
        String dummyName = DUMMY_RETURN_PREFIX + type.toString();
        Lvalue dummyRef = astBuilder.makeVariableRef(dummyName);
        if (!localMap.hasDeclare(dummyName)) {
            localMap.addDeclare(type, dummyName);
        }
        return dummyRef;
    }

    private Lvalue translateArrayRef(ArrayRef arrayRef) {
        Value baseVal = arrayRef.getBase();
        if (baseVal instanceof Local) {
            Local base = (Local) baseVal;
            String baseName = translateLocalName(base);
            Expr index = translateValue(arrayRef.getIndex());
            Objects.requireNonNull(index);
            return astBuilder.makeArrayRef(baseName, index);
        } else
            throw new RuntimeException("Array base cannot be non-local: " + arrayRef);
    }

    private Expr translateConstant(Constant constant) {
        if (constant instanceof NullConstant)
            return astBuilder.makeIntConstantExpr(0);
        else if (constant instanceof IntConstant) {
            IntConstant intConst = (IntConstant) constant;
            return astBuilder.makeIntConstantExpr(intConst.value);
        } else if (constant instanceof LongConstant) {
            LongConstant longConst = (LongConstant) constant;
            long longValue = longConst.value;
            if (longValue > (long) Integer.MAX_VALUE)
                throw new RuntimeException("Int value longer than 32bit");
            return astBuilder.makeIntConstantExpr((int)longValue);
        } else if (constant instanceof FloatConstant) {
            FloatConstant floatConst = (FloatConstant) constant;
            float floatValue = floatConst.value;
            int roundedFloatValue = (int)floatValue;
            System.err.println("Truncating float value " + floatValue + " to int value " + roundedFloatValue);
            return astBuilder.makeIntConstantExpr(roundedFloatValue);
        } else if (constant instanceof DoubleConstant) {
            DoubleConstant doubleConst = (DoubleConstant) constant;
            double doubleValue = doubleConst.value;
            int roundedDoubleValue = (int)doubleValue;
            System.err.println("Truncating double value " + doubleValue + " to int value " + roundedDoubleValue);
            return astBuilder.makeIntConstantExpr(roundedDoubleValue);
        } else if (constant instanceof StringConstant) {
            StringConstant strConst = (StringConstant) constant;
            // FIXME: Handle string constant properly
            return astBuilder.makeIntConstantExpr(1);
        } else
            throw new RuntimeException("Constant not supported: " + constant);
    }

    private BinaryOperator getBinaryOperator(BinopExpr expr) {
        if (expr instanceof AddExpr)
            return BinaryOperator.PLUS;
        else if (expr instanceof SubExpr)
            return BinaryOperator.MINUS;
        else if (expr instanceof MulExpr)
            return BinaryOperator.MUL;
        else if (expr instanceof DivExpr)
            return BinaryOperator.DIV;
        else if (expr instanceof RemExpr)
            return BinaryOperator.MOD;
        else if (expr instanceof AndExpr)
            return BinaryOperator.AND;
        else if (expr instanceof OrExpr)
            return BinaryOperator.OR;
        else if (expr instanceof EqExpr)
            return BinaryOperator.EQ;
        else if (expr instanceof GeExpr)
            return BinaryOperator.GE;
        else if (expr instanceof GtExpr)
            return BinaryOperator.GT;
        else if (expr instanceof LeExpr)
            return BinaryOperator.LE;
        else if (expr instanceof LtExpr)
            return BinaryOperator.LT;
        else if (expr instanceof NeExpr)
            return BinaryOperator.NE;
        else
            throw new RuntimeException("Unknown binary expr: " + expr);
    }

    private Expr translateBuiltin(InvokeExpr invokeExpr) {
        SootMethod callee = invokeExpr.getMethod();
        String calleeSignature = callee.getSignature();
        if (calleeSignature.equals("<java.lang.String: int length()>")) {
            if (!(invokeExpr instanceof VirtualInvokeExpr))
                throw new RuntimeException("String.length() not a virtual call?");
            VirtualInvokeExpr vInvokeExpr = (VirtualInvokeExpr) invokeExpr;
            Expr baseExpr = translateValue(vInvokeExpr.getBase());
            return astBuilder.makeUnaryExpr(UnaryOperator.LENGTHOF, baseExpr);
        } else if (calleeSignature.equals("<java.lang.String: char charAt(int)>")) {
            if (!(invokeExpr instanceof VirtualInvokeExpr))
                throw new RuntimeException("String.length() not a virtual call?");
            if (invokeExpr.getArgCount() != 1)
                throw new RuntimeException("String.charAt() does not have 1 arg?");
            VirtualInvokeExpr vInvokeExpr = (VirtualInvokeExpr) invokeExpr;
            Value baseValue = vInvokeExpr.getBase();
            if (!(baseValue instanceof Local))
                throw new RuntimeException("Virtual invoke base is not a local?");
            Local baseLocal = (Local) baseValue;
            String localName = translateLocalName(baseLocal);
            Expr idxExpr = translateValue(vInvokeExpr.getArg(0));
            return astBuilder.makeValueFetchExpr(astBuilder.makeArrayRef(localName, idxExpr));
        }

        return null;
    }

    private Expr translateInvokeExpr(InvokeExpr invokeExpr) {
        Expr builtinExpr = translateBuiltin(invokeExpr);
        if (builtinExpr != null)
            return builtinExpr;

        // Hack: for now assume all invokes are direct calls
        SootMethod callee = invokeExpr.getMethod();
        String calleeName = SootMethodTranslator.translateMethodName(callee);
        List<Expr> argExprs = new ArrayList<>();
        if (invokeExpr instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr) invokeExpr;
            Value baseVal = instanceInvokeExpr.getBase();
            if (baseVal instanceof Local) {
                Local baseLocal = (Local) baseVal;
                String baseName = translateLocalName(baseLocal);
                Lvalue baseRef = astBuilder.makeVariableRef(baseName);
                argExprs.add(astBuilder.makeValueFetchExpr(baseRef));
            } else
                throw new RuntimeException("Non-local instance invoke base not supported");
        }
        for (Value argValue: invokeExpr.getArgs()) {
            Expr argExpr = translateValue(argValue);
            Objects.requireNonNull(argExpr);
            argExprs.add(argExpr);
        }
        return astBuilder.makeCallExpr(calleeName, argExprs);
    }

    private Expr translateValue(Value value) {
        if (value instanceof Local) {
            Local localValue = (Local) value;
            String localName = translateLocalName(localValue);
            Lvalue varRef = astBuilder.makeVariableRef(localName);
            return astBuilder.makeValueFetchExpr(varRef);
        } else if (value instanceof Constant) {
            Constant c = (Constant) value;
            return translateConstant(c);
        } else if (value instanceof NegExpr) {
            NegExpr negExpr = (NegExpr) value;
            Expr expr = translateValue(negExpr.getOp());
            Objects.requireNonNull(expr);
            return astBuilder.makeBinaryExpr(BinaryOperator.MINUS, astBuilder.makeIntConstantExpr(0), expr);
        } else if (value instanceof LengthExpr) {
            LengthExpr lenExpr = (LengthExpr) value;
            Expr expr = translateValue(lenExpr.getOp());
            Objects.requireNonNull(expr);
            return astBuilder.makeUnaryExpr(UnaryOperator.LENGTHOF, expr);
        } else if (value instanceof BinopExpr) {
            BinopExpr binExpr = (BinopExpr) value;
            BinaryOperator op = getBinaryOperator(binExpr);

            Value op0 = binExpr.getOp1();
            Expr expr0 = translateValue(op0);
            Objects.requireNonNull(expr0);

            Value op1 = binExpr.getOp2();
            Expr expr1 = translateValue(op1);
            Objects.requireNonNull(expr1);

            return astBuilder.makeBinaryExpr(op, expr0, expr1);
        } else if (value instanceof ArrayRef) {
            ArrayRef arrayRef = (ArrayRef) value;
            Lvalue lval = translateArrayRef(arrayRef);
            return astBuilder.makeValueFetchExpr(lval);
        } else if (value instanceof CastExpr) {
            // TODO: Handle casts properly
            CastExpr castExpr = (CastExpr) value;
            Expr translated = translateValue(castExpr.getOp());
            Objects.requireNonNull(translated);
            return translated;
        } else if (value instanceof AnyNewExpr) {
            AnyNewExpr newExpr = (AnyNewExpr) value;
            allocSiteMap.getOrAddAllocSite(newExpr);
            // For array allocations, need to specify the length
            if (value instanceof NewArrayExpr) {
                NewArrayExpr newArrayExpr = (NewArrayExpr) newExpr;
                Value allocSize = newArrayExpr.getSize();
                Expr sizeExpr = translateValue(allocSize);
                Objects.requireNonNull(sizeExpr);
                Type elemType = typeTranslator.translateType(newArrayExpr.getBaseType());
                if (!(elemType instanceof BasicType))
                    throw new RuntimeException("NewArrayExpr cannot have an array elem type");
                return astBuilder.makeNewArrayExpr((BasicType)elemType, sizeExpr);
            }
            // TODO: Handle newMultiArray

            // Other allocations do not need to be translated for now
            return null;
        } else if (value instanceof InvokeExpr) {
            InvokeExpr invokeExpr = (InvokeExpr) value;
            return translateInvokeExpr(invokeExpr);
        } else if (value instanceof StaticFieldRef) {
            StaticFieldRef sFieldRef = (StaticFieldRef) value;
            SootField field = sFieldRef.getField();

            int fieldId = fieldMap.getOrAddField(field);
            List<Expr> args = Arrays.asList(astBuilder.makeIntConstantExpr(fieldId));
            return astBuilder.makeCallExpr(STATIC_FIELD_LOAD, args);
        } else if (value instanceof InstanceFieldRef) {
            InstanceFieldRef iFieldRef = (InstanceFieldRef) value;
            Value baseValue = iFieldRef.getBase();
            if (baseValue instanceof Local) {
                Expr baseExpr = translateValue(baseValue);
                Objects.requireNonNull(baseExpr);
                SootField field = iFieldRef.getField();
                int fieldId = fieldMap.getOrAddField(field);
                List<Expr> args = Arrays.asList(baseExpr, astBuilder.makeIntConstantExpr(fieldId));
                return astBuilder.makeCallExpr(INSTANCE_FIELD_LOAD, args);
            } else
                throw new RuntimeException("non-local field base not supported");
        }
        else
            throw new RuntimeException("Unsupported value: " + value);
    }

    private class ConditionTranslator implements ConditionVisitor<Expr> {

        @Override
        public Expr visit(CompareCondition cmpCond) {
            Value lhs = cmpCond.getLhs();
            Expr lhsExpr = translateValue(lhs);
            Objects.requireNonNull(lhsExpr);

            Value rhs = cmpCond.getRhs();
            Expr rhsExpr = translateValue(rhs);
            Objects.requireNonNull(rhsExpr);

            BinaryOperator op;
            switch (cmpCond.getOperator()) {
                case LT:
                    op = BinaryOperator.LT;
                    break;
                case LE:
                    op = BinaryOperator.LE;
                    break;
                case GT:
                    op = BinaryOperator.GT;
                    break;
                case GE:
                    op = BinaryOperator.GE;
                    break;
                case EQ:
                    op = BinaryOperator.EQ;
                    break;
                case NE:
                    op = BinaryOperator.NE;
                    break;
                default:
                    throw new RuntimeException("Unrecognized operator: " + cmpCond.getOperator());
            }

            // Hack: if this is an array-to-null comparison, change it to an isempty check
            if (lhs instanceof NullConstant && rhs.getType() instanceof ArrayType) {
                rhsExpr = astBuilder.makeUnaryExpr(UnaryOperator.LENGTHOF, rhsExpr);
            } else if (rhs instanceof NullConstant && lhs.getType() instanceof ArrayType) {
                lhsExpr = astBuilder.makeUnaryExpr(UnaryOperator.LENGTHOF, lhsExpr);
            }


            return astBuilder.makeBinaryExpr(op, lhsExpr, rhsExpr);
        }

        @Override
        public Expr visit(NegateCondition negCond) {
            Expr condExpr = translateValue(negCond.getValue());
            Objects.requireNonNull(condExpr);
            Expr negExpr = astBuilder.makeUnaryExpr(UnaryOperator.NOT, condExpr);
            return negExpr;
        }

        @Override
        public Expr visit(ValueCondition valueCond) {
            return translateValue(valueCond.getValue());
        }
    }
    private ConditionTranslator condTranslator = new ConditionTranslator();

    public void translateBranch(Condition cond, Stmt trueBranch, Stmt falseBranch) {
        Expr condExpr = cond.accept(condTranslator);
        Objects.requireNonNull(condExpr);
        Stmt ifStmt = astBuilder.makeIfStmt(condExpr, trueBranch, falseBranch);
        resultStmts.add(ifStmt);
    }

    public void translateLoop(Condition cond, Stmt body) {
        Expr condExpr = cond.accept(condTranslator);
        Objects.requireNonNull(condExpr);
        Stmt loopStmt = astBuilder.makeWhileStmt(condExpr, body);
        resultStmts.add(loopStmt);
    }

    private class TerminatorTranslator implements TerminatorVisitor<Stmt> {

        @Override
        public Stmt visit(BreakTerminator breakTerm) {
            return astBuilder.makeBreakStmt();
        }

        @Override
        public Stmt visit(ReturnTerminator returnTerm) {
            Value retValue = returnTerm.getValue();
            Expr retExpr = (retValue == null) ? astBuilder.makeUnitConstantExpr() : translateValue(retValue);
            return astBuilder.makeReturnStmt(retExpr);
        }
    }
    private TerminatorTranslator terminatorTranslator = new TerminatorTranslator();

    public void translateTerminator(Terminator terminator) {
        resultStmts.add(terminator.accept(terminatorTranslator));
    }

}
