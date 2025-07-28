package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.TakeTarget;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RClairvoyant extends Role {
    public RClairvoyant(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Voyante";
    }

    public static String _getFriendlyName() {
        return "de la " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu peux espionner un joueur et découvrir sa véritable identité...";
    }

    public static String _getTask() {
        return "Choisis un joueur dont tu veux connaître l'identité.";
    }

    public static String _getBroadcastedTask() {
        return "La " + _getName() + BLUE + " s'apprête à sonder un joueur...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();

        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            player.stopChoosing();
            player.hideView();

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new LookAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            LookAction action = (LookAction) event.getAction();
            if (!action.isCancelled()) {
                LGPlayer target = action.getTarget();
                player.sendActionBarMessage(
                    YELLOW + BOLD + target.getName() + GOLD + " est " + YELLOW + BOLD + action.getRoleView().getPublicName(target));
                player.sendMessage(
                    GOLD + "Tu découvres que " + GRAY + BOLD + target.getName() + GOLD + " est " + action.getRoleView().getPublicName(target) + GOLD +
                        ".");
            }
            else player.sendMessage(RED + "Votre cible est immunisée.");

            callback.run();
        });

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }

    public static class LookAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable {
        @Getter
        @Setter
        private boolean cancelled;
        @Getter
        @Setter
        private LGPlayer target;
        @Getter
        @Setter
        private Role roleView;
        public LookAction(LGPlayer target) {
            this.target = target;
            this.roleView = target.getRole();
        }
    }
}
