package fr.valgrifer.loupgarou.classes;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LGWinType {
    @Getter
    private static final ArrayList<LGWinType> values = new ArrayList<>();
    public static final LGWinType NONE = register("NONE", DARK_RED + "Erreur: " + RED + "personne n'a gagné la partie.");
    public static final LGWinType EQUAL = register("EQUAL",
        GRAY + BOLD + ITALIC + "Égalité" + GOLD + BOLD + ITALIC + ", personne n'a gagné la partie !");
    public static final LGWinType VILLAGEOIS = register("VILLAGEOIS",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par le " + DARK_GREEN + BOLD + "Village" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType LOUPGAROU = register("LOUPGAROU",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par les " + RED + BOLD + "Loups-Garous" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType VAMPIRE = register("VAMPIRE",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par les " + DARK_PURPLE + BOLD + "Vampires" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType SOLO = register("SOLO", GOLD + BOLD + ITALIC + "Un joueur solitaire a gagné la partie!");
    public static final LGWinType COUPLE = register("COUPLE",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par le " + LIGHT_PURPLE + BOLD + "couple" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType LOUPGAROUBLANC = register("LOUPGAROUBLANC",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par le " + RED + BOLD + "Loup-Garou Blanc" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType ANGE = register("ANGE",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par l'" + LIGHT_PURPLE + BOLD + "Ange" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType ASSASSIN = register("ASSASSIN",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par l'" + DARK_BLUE + BOLD + "Assassin" + GOLD + BOLD + ITALIC + " !");
    public static final LGWinType PYROMANE = register("PYROMANE",
        GOLD + BOLD + ITALIC + "La partie a été gagnée par le " + GOLD + BOLD + "Pyromane" + GOLD + BOLD + ITALIC + " !");
    @Getter
    private final String name;
    @Getter
    private final String message;

    private LGWinType(String name, String message) {
        this.name = name.replaceAll("[^\\w]", "");
        this.message = message;

        values.add(this);
    }

    public static LGWinType register(String name, String message) {
        LGWinType cause;
        if ((cause = getWinType(name)) != null) return cause;
        return new LGWinType(name, message);
    }

    public static LGWinType getWinType(String name) {
        for (LGWinType cause : getValues())
            if (cause.getName().equalsIgnoreCase(name))
                return cause;
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
        LGWinType that = (LGWinType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
