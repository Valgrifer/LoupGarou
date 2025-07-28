package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

import java.util.ArrayList;
import java.util.Objects;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LGPlayerKilledEvent extends LGEvent implements Cancellable {
    @Getter
    @Setter
    boolean cancelled;
    @Getter
    @Setter
    private LGPlayer killed;
    @Getter
    @Setter
    private Reason reason;
    public LGPlayerKilledEvent(LGGame game, LGPlayer killed, Reason reason) {
        super(game);
        this.killed = killed;
        this.reason = reason;
    }

    public static class Reason {
        @Getter
        private static final ArrayList<Reason> values = new ArrayList<>();
        public static final Reason LOUP_GAROU = register("LOUP_GAROU", GRAY + BOLD + "%s" + DARK_RED + " est mort pendant la nuit");
        public static final Reason LOUP_BLANC = register("LOUP_BLANC", LOUP_GAROU.getMessage());
        public static final Reason SORCIERE = register("SORCIERE", LOUP_GAROU.getMessage());
        public static final Reason GM_LOUP_GAROU = register("GM_LOUP_GAROU", GRAY + BOLD + "%s" + DARK_RED + " est mort pendant la nuit");
        public static final Reason CHASSEUR_DE_VAMPIRE = register("CHASSEUR_DE_VAMPIRE", GRAY + BOLD + "%s" + DARK_RED + " s'est fait purifier");
        public static final Reason VOTE = register("VOTE", GRAY + BOLD + "%s" + DARK_RED + " a été victime du vote");
        public static final Reason CHASSEUR = register("CHASSEUR", GRAY + BOLD + "%s" + DARK_RED + " est mort sur le coup");
        public static final Reason DICTATOR = register("DICTATOR", GRAY + BOLD + "%s" + DARK_RED + " a été désigné");
        public static final Reason DICTATOR_SUICIDE = register("DICTATOR_SUICIDE", GRAY + BOLD + "%s" + DARK_RED + " s'est suicidé par culpabilité");
        public static final Reason DISCONNECTED = register("DISCONNECTED", GRAY + BOLD + "%s" + DARK_RED + " est mort d'une déconnexion");
        public static final Reason LOVE = register("LOVE", GRAY + BOLD + "%s" + DARK_RED + " s'est suicidé par amour");
        public static final Reason BOUFFON = register("BOUFFON", GRAY + BOLD + "%s" + DARK_RED + " est mort de peur");
        public static final Reason ASSASSIN = register("ASSASSIN", GRAY + BOLD + "%s" + DARK_RED + " s'est fait poignarder");
        public static final Reason PYROMANE = register("PYROMANE", GRAY + BOLD + "%s" + DARK_RED + " est parti en fumée");
        public static final Reason PIRATE = register("PIRATE", GRAY + BOLD + "%s" + DARK_RED + " était l'otage");
        public static final Reason FAUCHEUR = register("FAUCHEUR", GRAY + BOLD + "%s" + DARK_RED + " a égaré son âme");
        public static final Reason DONT_DIE = register("DONT_DIE", GRAY + BOLD + "%s" + DARK_RED + " est mort pour rien");
        @Getter
        private final String name;
        @Getter
        private final String message;

        private Reason(String name, String message) {
            this.name = name.replaceAll("[^\\w]", "");
            this.message = message;

            values.add(this);
        }

        public static Reason register(String name, String message) {
            Reason cause;
            if ((cause = getReason(name)) != null) return cause;
            return new Reason(name, message);
        }

        public static Reason getReason(String name) {
            for (Reason cause : getValues())
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
            Reason that = (Reason) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

    }

}
