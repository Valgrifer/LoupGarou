package fr.valgrifer.loupgarou.classes.config;

import fr.valgrifer.loupgarou.roles.Role;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class LgComposition implements Cloneable {
    private @NotNull Map<Class<? extends Role>, Integer> composition;

    public LgComposition() {
        this(new HashMap<>());
    }

    public Map<Class<? extends Role>, Integer> getComposition() {
        return Collections.unmodifiableMap(composition);
    }

    public int size() {
        return composition.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void set(Class<? extends Role> role, @Nullable Integer value)
    {
        this.composition.compute(role, (k, v) -> value == null || value <= 0 ? null : value);
    }

    public void increase(Class<? extends Role> role) {
        this.increase(role, 1);
    }
    public void increase(Class<? extends Role> role, int value) {
        this.composition.compute(role, (k, v) -> v == null ? value : v + value);
    }

    public void decrease(Class<? extends Role> role) {
        this.decrease(role, 1);
    }
    public void decrease(Class<? extends Role> role, int value) {
        this.composition.compute(role, (k, v) -> v == null || v <= value ? null : v - value);
    }

    public int get(Class<? extends Role> role) {
        return this.composition.getOrDefault(role, 0);
    }

    public boolean contains(Class<? extends Role> role) {
        return this.composition.containsKey(role);
    }

    @Override
    public LgComposition clone() {
        try {
            LgComposition clone = (LgComposition) super.clone();

            clone.composition = new HashMap<>(this.composition);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
