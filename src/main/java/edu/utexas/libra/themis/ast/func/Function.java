package edu.utexas.libra.themis.ast.func;

import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.Type;

import java.util.List;

public interface Function {
    String getName();
    List<Param> getParameters();
    Type getReturnType();
    Stmt getFunctionBody();
}
