package fr.valgrifer.loupgarou.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VariableCache {
    private final Map<String, Object> cache = new HashMap<>();

    public boolean getBoolean(String key) {
        Object object = get(key);
        return object != null && (boolean) object;
    }

    public void set(String key, Object value) {
        if (cache.containsKey(key)) cache.replace(key, value);
        else cache.put(key, value);
    }

    public boolean has(String key) {
        return cache.containsKey(key);
    }

    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    public <T> T get(String key, T def) {
        return has(key) ? (T) cache.get(key) : def;
    }

    public <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        if (has(key)) return (T) cache.get(key);

        T value = supplier.get();
        cache.put(key, value);

        return value;
    }

    public <T> T remove(String key) {
        return (T) cache.remove(key);
    }

    public void reset() {
        cache.clear();
    }
}
