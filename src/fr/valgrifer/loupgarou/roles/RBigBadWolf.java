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
import org.bukkit.event.EventHandler;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RBigBadWolf extends Role {

    boolean lgDied;
    Runnable callback;

    public RBigBadWolf(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Grand Méchant Loup";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RWereWolf._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer... Tant qu'aucun autre " +
            RoleWinType.VILLAGE.getColoredName(BOLD) + WHITE + " n'est mort, tu peux, chaque nuit, dévorer une victime supplémentaire.";
    }

    public static String _getTask() {
        return "Choisis un joueur à dévorer.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + RED + BOLD + "Grand Méchant Loup" + BLUE + " n'en a pas terminé...";
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.LOUP_GAROU;
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    public boolean hasPlayersLeft() {
        return super.hasPlayersLeft() && !lgDied;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        this.callback = callback;

        player.showView();
        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            player.stopChoosing();
            player.hideView();

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new KillAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            KillAction action = (KillAction) event.getAction();
            if (!action.isCancelled() || action.isForceMessage()) {
                player.sendActionBarMessage(YELLOW + BOLD + action.getTarget().getName() + GOLD + " va mourir cette nuit");
                player.sendMessage(GOLD + "Tu as choisi de manger " + GRAY + BOLD + action.getTarget().getName() + GOLD + ".");
            }
            else player.sendMessage(RED + "Votre cible est immunisée.");

            if (action.isCancelled()) {
                callback.run();
                return;
            }

            LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), action.getTarget(), Reason.GM_LOUP_GAROU);
            Bukkit.getPluginManager().callEvent(killEvent);
            if (killEvent.isCancelled()) {
                callback.run();
                return;
            }

            getGame().kill(killEvent.getKilled(), Reason.GM_LOUP_GAROU);

            callback.run();
        });

        return true;
    }

    @EventHandler
    public void onPlayerDie(LGPlayerKilledEvent e) {//Quand un Loup-Garou meurt, les grands méchants loups ne peuvent plus jouer.
        if (e.getGame() == getGame()) if (e.getKilled().getRoleType() == RoleType.LOUP_GAROU) lgDied = true;
    }


    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
        player.sendMessage(GOLD + "Tu n'as tué personne.");
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        RWereWolf.forceJoin(player);
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
