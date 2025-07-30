package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.LGUpdatePrefixEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RVillagerVillager extends Role {
    public RVillagerVillager(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Villageois-Villageois";
    }

    public static String _getFriendlyName() {
        return "des " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes avec le " + RoleWinType.VILLAGE.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Tu ne disposes d'aucun pouvoir particulier, uniquement tous le monde sais que tu es gentil.";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() == getGame()) if (e.getPlayer().getRole() instanceof RVillagerVillager) e.setColorName(ChatColor.GREEN);
    }

    @EventHandler
    public void onTarget(LGRoleActionEvent e) {
        if (e.getGame() != getGame()) return;

        if (!e.isAction(RPsychopath.PsychopathGuessAction.class)) return;

        RPsychopath.PsychopathGuessAction action = (RPsychopath.PsychopathGuessAction) e.getAction();

        if (action.getTarget().getRole() != this || action.getRole() != this) return;

        action.setCancelled(true);

        e.getPlayers().forEach(player -> player.sendMessage(GRAY + "Vous ne pouvez pas tuer ce pauvre " + this.getName()));
    }
}
