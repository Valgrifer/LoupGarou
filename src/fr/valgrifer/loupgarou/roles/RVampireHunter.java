package fr.valgrifer.loupgarou.roles;


import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.MessageForced;
import fr.valgrifer.loupgarou.events.TakeTarget;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RVampireHunter extends Role {
    public RVampireHunter(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Chasseur de Vampires";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu peux traquer un joueur pour découvrir s'il s'agit d'un Vampire. Si c'est le cas, tu le tueras dans son sommeil. Si les " +
            RoleWinType.VAMPIRE.getColoredName(BOLD) + WHITE +
            " te prennent pour cible, tu seras immunisé contre leur attaque, et tu tueras le plus jeune d'entre eux.";
    }

    public static String _getTask() {
        return "Choisis un joueur à pister.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " traque ses proies...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    public boolean hasPlayersLeft() {
        for (LGPlayer lgp : getGame().getAlive())
            if (lgp.getRoleType() == RoleType.VAMPIRE) return super.hasPlayersLeft();
        return false;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();

        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            player.stopChoosing();
            player.hideView();
            if (choosen.getCache().getBoolean("vampire") || choosen.getRole() instanceof RVampire) {
                LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new KillAction(choosen), player);
                Bukkit.getPluginManager().callEvent(event);
                KillAction action = (KillAction) event.getAction();

                if (!action.isCancelled() || action.isForceMessage()) {
                    player.sendMessage(
                        GRAY + BOLD + action.getTarget().getName() + GOLD + " est un " + DARK_PURPLE + BOLD + "Vampire" + GOLD + ", à l'attaque.");
                    player.sendActionBarMessage(YELLOW + BOLD + action.getTarget().getName() + GOLD + " va mourir");
                }
                else player.sendMessage(RED + "Votre cible est immunisée.");

                if (action.isCancelled()) {
                    callback.run();
                    return;
                }

                LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), action.getTarget(), Reason.CHASSEUR_DE_VAMPIRE);
                Bukkit.getPluginManager().callEvent(killEvent);

                if (killEvent.isCancelled()) {
                    callback.run();
                    return;
                }

                getGame().kill(killEvent.getKilled(), Reason.CHASSEUR_DE_VAMPIRE);
            }
            else {
                player.sendMessage(GRAY + BOLD + choosen.getName() + GOLD + " n'est pas un " + DARK_PURPLE + BOLD + "Vampire" + GOLD + "...");
                player.sendActionBarMessage(YELLOW + BOLD + choosen.getName() + GOLD + " n'est pas un " + DARK_PURPLE + BOLD + "Vampire");
            }

            callback.run();
        });

        return true;
    }


    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }

    public static class KillAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced
    {
        @Getter
        @Setter
        private boolean cancelled;
        @Getter
        @Setter
        private LGPlayer target;
        @Getter
        @Setter
        private boolean forceMessage;
        public KillAction(LGPlayer target) {
            this.target = target;
        }
    }
}
