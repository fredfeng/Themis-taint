package edu.utexas.libra.themis.visitor;

import edu.utexas.libra.themis.ast.lvalue.ArrayRef;
import edu.utexas.libra.themis.ast.lvalue.VariableRef;

public interface LvalueVisitor<T> {
    T visit(VariableRef varRef);
    T visit(ArrayRef arrayRef);
}
