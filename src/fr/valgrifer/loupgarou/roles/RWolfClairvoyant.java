package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import org.bukkit.Bukkit;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWolfClairvoyant extends Role {
    public RWolfClairvoyant(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.LOUP_GAROU;
    }

    public static String _getName() {
        return RED + BOLD + "Loup Voyante";
    }

    public static String _getFriendlyName() {
        return RClairvoyant._getFriendlyName();
    }

    public static String _getShortDescription() {
        return RWereWolf._getShortDescription();
    }

    public static String _getDescription() {
        return RWereWolf._getDescription() + WHITE + ". Chaque nuit, tu peux espionner un joueur et découvrir sa véritable identité...";
    }

    public static String _getTask() {
        return RClairvoyant._getTask();
    }

    public static String _getBroadcastedTask() {
        return RClairvoyant._getBroadcastedTask();
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        RWereWolf.forceJoin(player);
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();

        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            player.stopChoosing();
            player.hideView();

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new RClairvoyant.LookAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            RClairvoyant.LookAction action = (RClairvoyant.LookAction) event.getAction();
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
}
