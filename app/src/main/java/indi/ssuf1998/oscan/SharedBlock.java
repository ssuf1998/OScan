package indi.ssuf1998.oscan;

import java.util.HashMap;

public class SharedBlock {
    private static SharedBlock instance;

    private final HashMap<String, Object> dataMap;

    private SharedBlock() {
        dataMap = new HashMap<>();
    }

    public static SharedBlock getInstance() {
        if (instance == null) {
            synchronized (SharedBlock.class) {
                if (instance == null) {
                    instance = new SharedBlock();
                }
            }
        }
        return instance;
    }

    public Object getDataThenSweep(String key) {
        final Object v = dataMap.get(key);
        removeData(key);
        return v;
    }

    public Object getData(String key, Object defaultValue) {
        if (!dataMap.containsKey(key))
            return defaultValue;
        return dataMap.get(key);
    }

    public void putData(String key, Object value) {
        dataMap.put(key, value);
    }


    public void removeData(String key) {
        dataMap.remove(key);
    }

}
