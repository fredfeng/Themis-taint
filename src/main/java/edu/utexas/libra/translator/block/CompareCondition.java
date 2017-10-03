package edu.utexas.libra.translator.block;

import soot.Value;

public class CompareCondition implements Condition {
    public enum Operator {
        LT,
        LE,
        GT,
        GE,
        EQ,
        NE;

        public static Operator negate(Operator op) {
           switch (op) {
               case LT:
                   return GE;
               case LE:
                   return GT;
               case GT:
                   return LE;
               case GE:
                   return LT;
               case EQ:
                   return NE;
               case NE:
                   return EQ;
           }
           throw new RuntimeException("Unrecognized operator: " + op);
        }
    }

    private Operator operator;
    private Value lhs;
    private Value rhs;

    public CompareCondition(Operator operator, Value lhs, Value rhs) {
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Operator getOperator() { return operator; }
    public Value getLhs() { return lhs; }
    public Value getRhs() { return rhs; }

    @Override
    public <T> T accept(ConditionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Condition negate() {
        return new CompareCondition(Operator.negate(operator), lhs, rhs);
    }
}
