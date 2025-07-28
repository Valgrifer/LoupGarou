package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGWinType;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

public class RoleWinType {
    @Getter
    private static final ArrayList<RoleWinType> values = new ArrayList<>();
    public static final RoleWinType NONE = register("NONE", "None", MAGIC, LGWinType.NONE);
    public static final RoleWinType VILLAGE = register("VILLAGE", "Village", GREEN, LGWinType.VILLAGEOIS);
    public static final RoleWinType LOUP_GAROU = register("LOUP_GAROU", "Loups", RED, LGWinType.LOUPGAROU);
    public static final RoleWinType VAMPIRE = register("VAMPIRE", "Vampires", DARK_PURPLE, LGWinType.VAMPIRE);
    public static final RoleWinType SOLO = register("SOLO", "SEUL", GRAY, LGWinType.SOLO);
    public static final RoleWinType COUPLE = register("COUPLE", "Couple", LIGHT_PURPLE, LGWinType.COUPLE);
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final ChatColor color;
    @Getter
    private final LGWinType winType;
    private RoleWinType(String id, String name, ChatColor color, LGWinType winType) {
        this.id = id.replaceAll("[^\\w]", "");
        this.name = name;
        this.color = color;
        this.winType = winType;

        values.add(this);
    }

    public static RoleWinType register(String id, String name, ChatColor color, LGWinType winType) {
        RoleWinType cause;
        if ((cause = getRoleWinType(name)) != null) return cause;
        return new RoleWinType(id, name, color, winType);
    }

    public static RoleWinType getRoleWinType(String name) {
        for (RoleWinType cause : getValues())
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
        RoleWinType that = (RoleWinType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
