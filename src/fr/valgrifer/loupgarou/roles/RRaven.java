package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import fr.valgrifer.loupgarou.events.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RRaven extends Role {
    public RRaven(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Corbeau";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + ". Chaque nuit, tu peux désigner un joueur qui se retrouvera le lendemain avec deux voix contre lui au vote.";
    }

    public static String _getTask() {
        return "Tu peux choisir un joueur qui aura deux votes contre lui.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " s'apprête à diffamer quelqu'un...";
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
            //player.sendTitle(GOLD+"Vous avez regardé un rôle", YELLOW+BOLD+choosen.getName()+GOLD+BOLD+" est "+YELLOW+BOLD+choosen.getRole().getName(), 5*20);

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new VoteAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            VoteAction action = (VoteAction) event.getAction();
            if (!action.isCancelled() || action.isForceMessage()) {
                player.sendActionBarMessage(YELLOW + BOLD + action.getTarget().getName() + GOLD + " aura deux votes contre lui");
                player.sendMessage(GOLD + "Tu nuis à la réputation de " + GRAY + BOLD + action.getTarget().getName() + GOLD + ".");
            }
            else player.sendMessage(RED + "Votre cible est immunisée.");

            if (action.isCancelled()) {
                callback.run();
                return;
            }

            action.getTarget().getCache().set("corbeau_selected", true);

            callback.run();
        });

        return true;
    }

    @EventHandler
    public void onNightStart(LGDayEndEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getGame().getAlive())
            lgp.getCache().remove("corbeau_selected");
    }

    @EventHandler
    public void onVoteStart(LGVoteRequestedEvent e) {
        if (e.getGame() == getGame() && e.getCause() == LGVoteCause.VILLAGE) for (LGPlayer lgp : getGame().getAlive())
            if (lgp.getCache().getBoolean("corbeau_selected")) {
                lgp.getCache().remove("corbeau_selected");
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        getGame().getVote().vote(new LGPlayer(GREEN + BOLD + "Le corbeau"), lgp);
                        getGame().getVote().vote(new LGPlayer(GREEN + BOLD + "Le corbeau"), lgp);//fix
                        getGame().broadcastMessage(GRAY + BOLD + lgp.getName() + GOLD + " a reçu la visite du " + getName() + GOLD + ".", true);
                    }
                }.runTask(MainLg.getInstance());

            }
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }

    public static class VoteAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced
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
        public VoteAction(LGPlayer target) {
            this.target = target;
        }
    }
}
