package edu.utexas.libra.translator.util;

import soot.SootClass;
import soot.jimple.AnyNewExpr;

import java.util.HashMap;
import java.util.Map;

public class SootAllocSiteMap {
    private Map<AnyNewExpr, Integer> instanceMap = new HashMap<>();
    private Map<SootClass, Integer> staticMap = new HashMap<>();
    private int nextId = 1;

    private <K> int getOrAdd(Map<K, Integer> theMap, K key) {
        Integer ret = theMap.get(key);
        if (ret != null)
            return ret;

        int retId = nextId; ++nextId;
        theMap.put(key, retId);
        return retId;
    }

    public int getOrAddAllocSite(AnyNewExpr allocSite) {
        return getOrAdd(instanceMap, allocSite);
    }

    public int getOrAddClass(SootClass cl) {
        return getOrAdd(staticMap, cl);
    }
}
