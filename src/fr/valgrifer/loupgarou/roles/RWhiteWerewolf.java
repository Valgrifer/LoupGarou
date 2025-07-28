package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWhiteWerewolf extends Role {
    private static final ItemBuilder itemNoAction = ResourcePack.getItem("ui_cancel")
        .setCustomId("ac_skip")
        .setDisplayName(GRAY + BOLD + "Ne rien faire")
        .setLore(DARK_GRAY + "Passez votre tour");
    Runnable callback;

    public RWhiteWerewolf(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Loup Blanc";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes " + RoleWinType.SOLO.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Les autres " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE +
            " croient que tu es un loup normal, mais une nuit sur deux, tu peux assassiner l'un d'eux au choix.";
    }

    public static String _getTask() {
        return "Tu peux choisir un " + RoleType.LOUP_GAROU.getColoredName(BOLD) + GOLD + " (ou du " + RoleWinType.VILLAGE.getColoredName(BOLD) +
            GOLD + " si il n'y a plus de " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) + GOLD + ") à éliminer, ou te rendormir.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " pourrait faire un ravage cette nuit...";
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    public boolean hasPlayersLeft() {
        return super.hasPlayersLeft() && getGame().getNight() % 2 == 0;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        this.callback = callback;
        List<LGPlayer> targetable;
        RWereWolf lg;
        if ((lg = getGame().getRole(RWereWolf.class)) != null &&
            (lg.getPlayers().stream().anyMatch(lgp -> !(lgp.getRole() instanceof RWhiteWerewolf)) ||
                lg.getPlayers().stream().filter(lgp -> (lgp.getRole() instanceof RWhiteWerewolf)).count() > 1)) targetable = lg.getPlayers();
        else targetable = getGame().getAlive();

        player.showView();
        player.getPlayer().getInventory().setItem(8, itemNoAction.build());
        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            if (!targetable.contains(choosen)) {
                player.sendMessage(GRAY + BOLD + choosen.getName() + DARK_RED + " n'est pas ciblable.");
                return;
            }

            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().updateInventory();
            player.stopChoosing();
            player.hideView();

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new KillAction(choosen), player);
            Bukkit.getPluginManager().callEvent(event);
            KillAction action = (KillAction) event.getAction();
            if (!action.isCancelled() || action.isForceMessage()) {
                player.sendActionBarMessage(YELLOW + BOLD + action.getTarget().getName() + GOLD + " va mourir cette nuit");
                player.sendMessage(GOLD + "Tu as choisi de dévorer " + GRAY + BOLD + action.getTarget().getName() + GOLD + ".");
            }
            else player.sendMessage(RED + "Votre cible est immunisée.");

            if (action.isCancelled()) {
                callback.run();
                return;
            }

            LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), action.getTarget(), Reason.LOUP_BLANC);
            Bukkit.getPluginManager().callEvent(killEvent);
            if (killEvent.isCancelled()) {
                callback.run();
                return;
            }

            getGame().kill(killEvent.getKilled(), Reason.LOUP_BLANC);
            callback.run();
        });

        return true;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        LGPlayer player = LGPlayer.get(p);

        if (player.getRole() != this || !ItemBuilder.checkId(e.getItem(), itemNoAction.getCustomId())) return;

        player.stopChoosing();
        p.getInventory().setItem(8, null);
        p.updateInventory();
        player.hideView();
        player.sendMessage(GOLD + "Tu n'as tué personne.");
        callback.run();
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.getPlayer().getInventory().setItem(8, null);
        player.getPlayer().updateInventory();
        player.hideView();
        player.sendMessage(GOLD + "Tu n'as tué personne.");
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        RWereWolf.forceJoin(player);
    }

    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && !getPlayers().isEmpty()) e.setWinType(LGWinType.LOUPGAROUBLANC);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (e.getWinType() == LGWinType.LOUPGAROUBLANC) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }
    }

    @Setter
    @Getter
    public static class KillAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced
    {
        private boolean cancelled;
        private LGPlayer target;
        private boolean forceMessage;
        public KillAction(LGPlayer target) {
            this.target = target;
        }
    }
}
