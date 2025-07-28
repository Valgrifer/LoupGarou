package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RAssassin extends Role {
    public RAssassin(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return DARK_BLUE + BOLD + "Assassin";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes " + RoleWinType.SOLO.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu peux choisir un joueur à éliminer. Tu es immunisé contre l'attaque des " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Choisis un joueur à éliminer.";
    }

    public static String _getBroadcastedTask() {
        return "L'" + _getName() + BLUE + " ne controle plus ses pulsions...";
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

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new KillAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            KillAction action = (KillAction) event.getAction();
            if (!action.isCancelled() || action.isForceMessage()) {
                player.sendActionBarMessage(YELLOW + BOLD + action.getTarget().getName() + GOLD + " va mourir");
                player.sendMessage(GOLD + "Tu as choisi de tuer " + GRAY + BOLD + action.getTarget().getName() + GOLD + ".");
            }
            else player.sendMessage(RED + "Votre cible est immunisée.");

            if (action.isCancelled()) {
                callback.run();
                return;
            }

            LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), action.getTarget(), Reason.ASSASSIN);
            Bukkit.getPluginManager().callEvent(killEvent);
            if (killEvent.isCancelled()) {
                callback.run();
                return;
            }

            getGame().kill(killEvent.getKilled(), Reason.ASSASSIN);
            callback.run();
        });

        return true;
    }

    @EventHandler
    public void onKill(LGNightPlayerPreKilledEvent e) {
        if (e.getKilled().getRole() != this || (e.getReason() != Reason.LOUP_GAROU && e.getReason() != Reason.GM_LOUP_GAROU) ||
            !e.getKilled().isRoleActive()) return;

        e.setCancelled(true);

        if (e.getReason() == Reason.LOUP_GAROU) {
            RWereWolf lgs;
            if ((lgs = getGame().getRole(RWereWolf.class)) != null) for (LGPlayer lg : lgs.getPlayers())
                lg.sendMessage(RED + "Votre cible est immunisée.");
        }
        else if (e.getReason() == Reason.GM_LOUP_GAROU) {
            RBigBadWolf lgs;
            if ((lgs = getGame().getRole(RBigBadWolf.class)) != null) for (LGPlayer lg : lgs.getPlayers())
                lg.sendMessage(RED + "Votre cible est immunisée.");
        }
    }

    @EventHandler
    public void onTarget(LGRoleActionEvent e) {
        if (e.getGame() != getGame()) return;
        if (e.isAction(RPyromaniac.GasoilAction.class)) {
            RPyromaniac.GasoilAction action = (RPyromaniac.GasoilAction) e.getAction();
            if (action.getTarget().getRole() == this && action.getTarget().isRoleActive()) action.setCancelled(true);
        }
        else if (e.isAction(RVampire.VampiredAction.class)) {
            RVampire.VampiredAction action = (RVampire.VampiredAction) e.getAction();
            if (action.getTarget().getRole() == this && action.getTarget().isRoleActive()) action.setImmuned(true);
        }
    }

    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && !getPlayers().isEmpty()) e.setWinType(LGWinType.ASSASSIN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if (e.getWinType() == LGWinType.ASSASSIN) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }
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
