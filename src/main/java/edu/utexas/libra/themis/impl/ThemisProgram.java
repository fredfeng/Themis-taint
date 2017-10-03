package edu.utexas.libra.themis.impl;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.ast.func.Function;

import java.util.*;

public class ThemisProgram implements Program {

    private static String ENTRY_NAME = "main";

    private Map<String, Function> functionMap;

    ThemisProgram(Collection<Function> functions) {
        this(buildFunctionMap(functions));
    }

    private ThemisProgram(Map<String, Function> map) {
        if (map == null)
            throw new IllegalArgumentException();
        this.functionMap = Collections.unmodifiableMap(map);
    }

    @Override
    public List<Function> getFunctionList() {
        List<Function> funcList = new ArrayList<>(functionMap.size());
        funcList.addAll(functionMap.values());
        return Collections.unmodifiableList(funcList);
    }

    @Override
    public Function getEntryFunction() {
        return getFunction(ENTRY_NAME);
    }

    @Override
    public Function getFunction(String name) {
        return functionMap.get(name);
    }

    @Override
    public int size() {
        return functionMap.size();
    }

    private static Map<String, Function> buildFunctionMap(Collection<Function> functions) {
        if (functions.isEmpty())
            throw new IllegalArgumentException("Creating an empty program is not allowed");

        Map<String, Function> fmap = new HashMap<>();
        for (Function f: functions) {
            String funcName = f.getName();
            if (fmap.containsKey(funcName))
                throw new IllegalArgumentException("Functions cannot have duplicated names");
            fmap.put(funcName, f);
        }

        return fmap;
    }

}
