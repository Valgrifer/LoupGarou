package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Objects;
import java.util.stream.Stream;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RReaper extends Role {
    public RReaper(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Faucheur";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Si les " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE +
            " te tuent pendant la nuit, tu emporteras l’un d’entre eux dans ta mort, mais si tu meurs lors du vote du " +
            RoleWinType.VILLAGE.getColoredName(BOLD) + WHITE + ", ce sont tes deux voisins qui en paieront le prix.";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKill(LGPlayerKilledEvent e) {
        if (e.getGame() != this.getGame() || e.getKilled().getRole() != this || !e.getKilled().isRoleActive())
            return;

        LGPlayer killed = e.getKilled();
        if (killed.getCache().getBoolean("faucheur_did"))//A déjà fait son coup de faucheur !
            return;
        killed.getCache().set("faucheur_did", true);

        if (e.getReason() == Reason.LOUP_GAROU || e.getReason() == Reason.GM_LOUP_GAROU) {
            // Mort par les LG
            // Tue un lg au hasard
            LGPlayer selected = null;
            RWereWolf role;
            if ((role = getGame().getRole(RWereWolf.class)) != null)
                selected = role.getPlayers().get(this.getGame().getRandom().nextInt(role.getPlayers().size()));

            if (selected != null) {
                LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), selected, Reason.FAUCHEUR);
                Bukkit.getPluginManager().callEvent(killEvent);
                if (killEvent.isCancelled()) return;
                getGame().kill(killEvent.getKilled(), killEvent.getReason(), false);
            }
        }
        else if (e.getReason() == Reason.VOTE) {
            int index = killed.getSpot();
            Stream.of(this.getGame().getSpots().previousOf(index), this.getGame().getSpots().nextOf(index))
                    .distinct()
                    .filter(Objects::nonNull)
                    .forEach(lgp -> {
                        LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), lgp, Reason.FAUCHEUR);
                        Bukkit.getPluginManager().callEvent(killEvent);
                        if (!killEvent.isCancelled()) getGame().kill(killEvent.getKilled(), killEvent.getReason(), false);
                    });
        }
    }
}
