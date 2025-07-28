package fr.valgrifer.loupgarou.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class EnumExtendable {
    @Getter
    private static final ArrayList<EnumExtendable> values = new ArrayList<>();
    @Getter
    private final String name;

    private EnumExtendable(String name) {
        this.name = name.replaceAll("[^\\w]", "");

        values.add(this);
    }

    public static EnumExtendable register(String name) {
        EnumExtendable cause;
        if ((cause = getEnumExtendable(name)) != null) return cause;
        return new EnumExtendable(name);
    }

    public static EnumExtendable getEnumExtendable(String name) {
        for (EnumExtendable cause : getValues())
            if (cause.getName().equalsIgnoreCase(name)) return cause;
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumExtendable that = (EnumExtendable) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
