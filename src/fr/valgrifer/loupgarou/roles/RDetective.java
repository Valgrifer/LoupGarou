package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RDetective extends Role {
    LGPlayer first;

    public RDetective(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Détective";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu mènes l'enquête sur deux joueurs pour découvrir s'ils font partie du même camp.";
    }

    public static String _getTask() {
        return "Choisis deux joueurs à étudier.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " est sur une enquête...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        first = null;
        player.showView();

        player.choose(choosen -> {
            if (choosen == null) return;

            if (choosen == player) {
                player.sendMessage(RED + "Vous ne pouvez pas vous sélectionner !");
                return;
            }
            if (first != null) {
                if (first == choosen) {
                    player.sendMessage(RED + "Vous ne pouvez pas comparer " + GRAY + BOLD + first.getName() + RED + " avec lui même !");
                }
                else {
                    player.stopChoosing();
                    player.hideView();

                    LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new CompareAction(first, choosen), player);
                    Bukkit.getPluginManager().callEvent(event);
                    CompareAction action = (CompareAction) event.getAction();
                    if (!action.isCancelled()) if (action.isDifferentCamp()) player.sendMessage(
                        GRAY + BOLD + action.getTarget1().getName() + GOLD + " et " + GRAY + BOLD + action.getTarget2().getName() + GOLD +
                            " ne sont " + RED + "pas du même camp.");
                    else player.sendMessage(
                            GRAY + BOLD + action.getTarget1().getName() + GOLD + " et " + GRAY + BOLD + action.getTarget2().getName() + GOLD +
                                " sont " + GREEN + "du même camp.");
                    else player.sendMessage(RED + "Votre information a été brouillé.");
                    callback.run();
                }
            }
            else {
                first = choosen;
                player.sendMessage(BLUE + "Choisis un joueur avec qui tu souhaites comparer le rôle de " + GRAY + BOLD + choosen.getName());
            }
        });

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }


    @Setter
    @Getter
    public static class CompareAction implements LGRoleActionEvent.RoleAction, Cancellable {
        private boolean cancelled;
        private LGPlayer target1;
        private LGPlayer target2;
        private boolean differentCamp;
        public CompareAction(LGPlayer target1, LGPlayer target2) {
            this.target1 = target1;
            this.target2 = target2;
            this.differentCamp = isDifferentCamp(target1.getRoleType(), target2.getRoleType());
        }

        public static boolean isDifferentCamp(RoleType role1, RoleType role2) {
            return role1 == RoleType.NEUTRAL || role1 != role2;
        }
    }
}
