package edu.utexas.libra.themis.ast;

import edu.utexas.libra.themis.ast.func.Function;

import java.util.List;

public interface Program {
    List<Function> getFunctionList();
    Function getEntryFunction();

    Function getFunction(String name);

    int size();
}
