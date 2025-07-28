package fr.valgrifer.loupgarou.roles;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

public class RoleType {
    @Getter
    private static final ArrayList<RoleType> values = new ArrayList<>();
    public static final RoleType VILLAGER = register("VILLAGER", "Villageois", GREEN);
    public static final RoleType LOUP_GAROU = register("LOUP_GAROU", "Loup-Garou", RED);
    public static final RoleType VAMPIRE = register("VAMPIRE", "Vampire", DARK_PURPLE);
    public static final RoleType NEUTRAL = register("NEUTRAL", "Neutre", GRAY);
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final ChatColor color;
    private RoleType(String id, String name, ChatColor color) {
        this.id = id.replaceAll("[^\\w]", "");
        this.name = name;
        this.color = color;

        values.add(this);
    }

    public static RoleType register(String id, String name, ChatColor color) {
        RoleType cause;
        if ((cause = getRoleType(id)) != null) return cause;
        return new RoleType(id, name, color);
    }

    public static RoleType getRoleType(String name) {
        for (RoleType cause : getValues())
            if (cause.getId().equalsIgnoreCase(name)) return cause;
        return null;
    }

    public String getColoredName(ChatColor... moreColors) {
        return color + Arrays.stream(moreColors).map(ChatColor::toString).collect(Collectors.joining()) + name;
    }

    public String getColoredName(String... moreColors) {
        return color + String.join("", moreColors) + name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleType that = (RoleType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
