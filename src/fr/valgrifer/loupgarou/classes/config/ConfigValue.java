package fr.valgrifer.loupgarou.classes.config;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ConfigValue<S, D> {
    private static final List<ConfigValue<?, ?>> values = new ArrayList<>();
    public static List<ConfigValue<?, ?>> getValues() {
        return Collections.unmodifiableList(values);
    }
    public static List<ConfigValue<?, ?>> getGameOptions() {
        return values.stream()
                       .filter(cv -> Objects.nonNull(cv.optionType()))
                       .collect(Collectors.toUnmodifiableList());
    }

    public abstract String displayName(D data);
    public abstract List<String> lore(D data);
    public abstract @Nullable Class<D> optionType();

    public static void register(@NotNull ConfigValue<?, ?> key) {
        Validate.isTrue(values.stream().noneMatch(o -> Objects.equals(o.key(), key.key())), String.format("ConfigValue: the key '%s' is already register", key.key()));
        values.add(key);
    }

    public abstract @NotNull String key();
    public abstract D defaultValue();
    public abstract @NotNull D serialize(@NotNull S value);
    public abstract @NotNull S deserialize(@NotNull D value);

    public static abstract class Single<T> extends ConfigValue<T, T> {
        public @NotNull T serialize(@NotNull T value) {
            return value;
        }
        public @NotNull T deserialize(@NotNull T value) {
            return value;
        }
    }
}
