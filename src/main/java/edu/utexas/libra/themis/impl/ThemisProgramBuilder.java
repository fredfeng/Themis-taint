package edu.utexas.libra.themis.impl;

import edu.utexas.libra.themis.ast.func.Function;

import java.util.Collection;
import java.util.List;

public class ThemisProgramBuilder {
    public static ThemisProgram makeProgram(Collection<Function> functions) {
        return new ThemisProgram(functions);
    }
}
