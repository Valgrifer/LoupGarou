package fr.valgrifer.loupgarou.classes.config;

import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GamePreset implements ConfigurationSerializable, Cloneable {
    private Map<String, KeyValue<?, ?>> data;

    public GamePreset() {
        data = new HashMap<>();
    }

    public <S> GamePreset(@NotNull Map<String, Object> map) {
        this.data = map.entrySet()
                            .stream()
                            .map(entry -> {
                                ConfigValue<S, ?> configValue = (ConfigValue<S, ?>) ConfigValue.getValues()
                                                                        .stream()
                                                                        .filter(cv -> Objects.equals(cv.key(), entry.getKey()))
                                                                        .findFirst()
                                                                        .orElse(null);
                                if (configValue == null) {
                                    System.err.printf("[ERROR] No config value found for key '%s'", entry.getKey());
                                    return null;
                                }

                                KeyValue<S, ?> kv = new KeyValue<>(configValue);
                                kv.deserialize((S) entry.getValue());
                                return new AbstractMap.SimpleEntry<String, KeyValue<?, ?>>(entry.getKey(), kv);
                            })
                            .filter(Objects::nonNull)
                            .collect(VariousUtils.toMap(HashMap::new));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return this.data.entrySet()
                       .stream()
                       .map(entry -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey(), entry.getValue().serialize()))
                       .collect(VariousUtils.toMap(HashMap::new));
    }

    public boolean has(@NotNull ConfigValue<?, ?> key) {
        return this.data.containsKey(key.key());
    }

    public <D> void set(@NotNull ConfigValue<?, D> key, @NotNull D value) {
        if (this.data.containsKey(key.key()))
            ((KeyValue<?, D>) this.data.get(key.key())).setValue(value);
        else
            this.data.put(key.key(), new KeyValue<>(key, value));
    }

    public <D> @NotNull D get(@NotNull ConfigValue<?, D> key) {
        if (data.containsKey(key.key()))
            return (D) data.get(key.key()).getValue();

        D value = key.defaultValue();

        if (!VariousUtils.isPrimitive(value.getClass()))
            this.data.put(key.key(), new KeyValue<>(key, value));

        return value;
    }

    public <D> @Nullable D remove(@NotNull ConfigValue<?, D> key) {
        KeyValue<?, ?> kv = this.data.remove(key.key());
        return kv == null ? null : (D) kv.getValue();
    }

    @Override
    public GamePreset clone() {
        try {
            GamePreset clone = (GamePreset) super.clone();

            clone.data = (Map<String, KeyValue<?, ?>>) (Object) this.data.entrySet()
                                 .stream()
                                 .map((entry) -> new AbstractMap.SimpleEntry<>(
                                         entry.getKey(),
                                         new KeyValue<>(
                                                 (ConfigValue<?, Object>) entry.getValue().getKey(),
                                                 safeClone(entry.getValue().getValue()))))
                                 .collect(VariousUtils.toMap(HashMap::new));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static <T> T safeClone(T clone) {
        try {
            Class<?> clazz = clone.getClass();
            if (VariousUtils.isPrimitive(clazz))
                return clone;

            Method method = clazz.getDeclaredMethod("clone");
            Validate.isTrue(Modifier.isPublic(method.getModifiers()), "Clone method is not public");
            return (T) method.invoke(clone);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class KeyValue<S, D> {
        @Getter
        private final ConfigValue<S, D> key;

        @Getter
        @Setter
        private @NotNull D value;

        KeyValue(ConfigValue<S, D> key) {
            this.key = key;
        }
        KeyValue(ConfigValue<S, D> key, @NotNull D value) {
            this.key = key;
            this.value = value;
        }

        void deserialize(S value) {
            this.value = this.key.serialize(value);
        }
        S serialize() {
            return this.key.deserialize(this.value);
        }
    }
}
