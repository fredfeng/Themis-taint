package edu.utexas.libra.translator.block;

public interface Condition {
    <T> T accept(ConditionVisitor<T> visitor);

    Condition negate();
}
