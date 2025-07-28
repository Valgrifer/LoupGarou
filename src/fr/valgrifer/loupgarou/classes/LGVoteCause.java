package fr.valgrifer.loupgarou.classes;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("ALL")
public class LGVoteCause {
    @Getter
    private static final ArrayList<LGVoteCause> values = new ArrayList<>();
    public static final LGVoteCause VILLAGE = register("VILLAGE");
    public static final LGVoteCause MAYOR = register("MAYOR");
    public static final LGVoteCause LOUPGAROU = register("LOUPGAROU");
    public static final LGVoteCause VAMPIRE = register("VAMPIRE");
    @Getter
    private final String name;
    private LGVoteCause(String name) {
        this.name = name.replaceAll("[^\\w]", "");

        values.add(this);
    }

    public static LGVoteCause register(String name) {
        LGVoteCause cause;
        if ((cause = getCause(name)) != null) return cause;
        return new LGVoteCause(name);
    }

    public static LGVoteCause getCause(String name) {
        for (LGVoteCause cause : getValues())
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
        LGVoteCause that = (LGVoteCause) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
