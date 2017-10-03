package edu.utexas.libra.translator.block;

public interface ConditionVisitor<T> {
    T visit(CompareCondition cmpCond);
    T visit(NegateCondition negCond);
    T visit(ValueCondition valueCond);
}
