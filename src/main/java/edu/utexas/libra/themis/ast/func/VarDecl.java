package edu.utexas.libra.themis.ast.func;

import edu.utexas.libra.themis.ast.type.Type;

public interface VarDecl {
    Type getType();
    String getName();
}
