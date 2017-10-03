package edu.utexas.libra.translator.util;

import soot.SootField;

import java.util.HashMap;
import java.util.Map;

public class SootFieldMap {
    private Map<SootField, Integer> idMap = new HashMap<>();
    private int nextId = 1;

    public int getOrAddField(SootField field) {
        Integer ret = idMap.get(field);
        if (ret != null)
            return ret;

        int retId = nextId; ++nextId;
        idMap.put(field, retId);
        return retId;
    }
}
