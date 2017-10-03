package edu.utexas.libra.translator.util;

import edu.utexas.libra.themis.ast.type.Type;

import java.util.*;

public class SootLocalMap {

    public static class LocalInfo {
        private int id;
        private String name;
        private Type type;

        private LocalInfo(int id, String name, Type type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public Type getType() { return type; }
    }

    private Map<String, LocalInfo> infoMap = new HashMap<>();
    private int nextId = 1;

    public boolean hasDeclare(String name) {
        return infoMap.containsKey(name);
    }

    public Integer getLocalId(String name) {
        LocalInfo info = infoMap.get(name);
        if (info == null)
            return null;
        return info.getId();
    }

    public void addDeclare(Type type, String name) {
        LocalInfo info = new LocalInfo(nextId, name, type);
        ++nextId;
        infoMap.put(name, info);
    }

    public Collection<LocalInfo> declareEntries() {
        return infoMap.values();
    }
}
