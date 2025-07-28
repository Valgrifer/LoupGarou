package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDayStartEvent;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.TakeTarget;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RBearShowman extends Role {
    private int lastNight = -1;

    public RBearShowman(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Montreur d'Ours";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque matin, ton Ours va renifler tes voisins et grognera si l'un d'eux est hostile aux Villageois.";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDay(LGDayStartEvent e) {
        if (e.getGame() != getGame() || getPlayers().isEmpty())
            return;

        if (lastNight == getGame().getNight())
            return;
        lastNight = getGame().getNight();

        for (LGPlayer by : getPlayers()) {
            if (!by.isRoleActive()) continue;

            int index = by.getSpot();

            if (Stream.of(
                    this.getGame().getSpots().previousOf(index),
                    by,
                    this.getGame().getSpots().nextOf(index)
            ).anyMatch(lgp -> isLG(by, lgp)))
                getGame().broadcastMessage(GOLD + "La bête du " + getName() + GOLD + " grogne...", true);
        }
    }

    private boolean isLG(@Nonnull LGPlayer by, @Nullable LGPlayer target) {
        if (target == null)
            return false;

        LGRoleActionEvent event = new LGRoleActionEvent(this.getGame(), new SniffAction(target), by);
        Bukkit.getPluginManager().callEvent(event);

        SniffAction action = (SniffAction) event.getAction();

        if (action.isCancelled())
            return false;

        return action.getRoleType() == RoleType.LOUP_GAROU;
    }

    @Getter
    @Setter
    public static class SniffAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable
    {
        private boolean cancelled;
        private LGPlayer target;
        private RoleType roleType;
        public SniffAction(LGPlayer target) {
            this.target = target;
            this.roleType = target.getRoleType();
        }
    }
}
