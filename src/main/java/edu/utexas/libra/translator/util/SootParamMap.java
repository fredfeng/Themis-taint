package edu.utexas.libra.translator.util;

import edu.utexas.libra.themis.ast.func.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SootParamMap {
    private Map<Integer, Param> paramMap = new TreeMap<>();

    public void addParam(int index, Param param) {
        if (paramMap.containsKey(index))
            throw new IllegalArgumentException("Duplicate insertion into SootParamMap");
        paramMap.put(index, param);
    }

    public List<Param> getParamList() {
        int idx = 0;
        List<Param> paramList = new ArrayList<>();
        for (Map.Entry<Integer, Param> entry: paramMap.entrySet()) {
            if (entry.getKey() != idx)
                throw new RuntimeException("Expect parameter index " + idx + "while found " + entry.getKey());
            paramList.add(entry.getValue());
            ++idx;
        }
        return paramList;
    }
}
