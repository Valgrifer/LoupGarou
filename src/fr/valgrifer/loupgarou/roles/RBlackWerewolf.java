package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.LGCardItems.Constraint;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RBlackWerewolf extends Role {
    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(3, new Slot(ResourcePack.getItem("ui_cancel")
                .setCustomId("ac_skip")
                .setDisplayName(GRAY + BOLD + "Ne rien faire")
                .setLore(DARK_GRAY + "Passez votre tour")), (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RBlackWerewolf)) return;

                RBlackWerewolf role = (RBlackWerewolf) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
                lgp.hideView();
                role.callback.run();
            });

            setSlot(5, new Slot(ItemBuilder.make(Material.ROTTEN_FLESH)
                .setCustomId("ac_infect")
                .setDisplayName(RED + BOLD + "Infecter")
                .setLore(DARK_GRAY + "Tu peux infecter la cible du vote.", DARK_GRAY + "Le joueur tiendra avec les Loups.")), (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RBlackWerewolf)) return;

                RBlackWerewolf role = (RBlackWerewolf) lgp.getRole();

                role.closeInventory(lgp);
                lgp.stopChoosing();
                lgp.hideView();

                LGRoleActionEvent e = new LGRoleActionEvent(role.getGame(), new InfectAction(role.toInfect), lgp);
                Bukkit.getPluginManager().callEvent(e);
                InfectAction action = (InfectAction) e.getAction();
                if (!action.isCancelled() || action.isForceMessage()) {
                    lgp.sendActionBarMessage(BLUE + BOLD + "Vous infectez " + BLUE + action.getTarget().getName());
                    lgp.sendMessage(GOLD + "Tu as infecté " + GRAY + BOLD + action.getTarget().getName() + GOLD + ".");
                }
                else lgp.sendMessage(RED + "Votre cible est immunisée.");

                if (!action.isCancelled() || action.isForceConsume()) {
                    lgp.getCache().set("has_infected", true);
                    role.getPlayers().remove(lgp);
                }

                if (action.isCancelled()) {
                    role.callback.run();
                    return;
                }

                action.getTarget().getCache().set("infected", true);
                action.getTarget().addEndGameReaveal(RED + "Infecté");
                action.getTarget().setRoleType(RoleType.LOUP_GAROU);
                action.getTarget().setRoleWinType(RoleWinType.LOUP_GAROU);
                action.getTarget().getCache().set("just_infected", true);
                role.getGame().getDeaths().remove(Reason.LOUP_GAROU, action.getTarget());
                role.callback.run();
            });
        }
    };
    Runnable callback;
    LGPlayer toInfect;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

    public RBlackWerewolf(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Loup Noir";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RWereWolf._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer... Une fois dans la partie, tu peux transformer la personne désignée en " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ". L'infecté conserve ses pouvoirs mais gagne désormais avec les " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Veux-tu infecter la cible du vote ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " décide s'il veut infecter sa cible.";
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
        return super.hasPlayersLeft() && getGame().getDeaths().containsKey(Reason.LOUP_GAROU);
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Infecter " + toInfect.getName() + " ?", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        toInfect = getGame().getDeaths().get(Reason.LOUP_GAROU);
        if (toInfect == null) {
            callback.run();
            return true;
        }
        player.showView();
        this.callback = callback;
        openInventory(player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.getPlayer().getInventory().setItem(8, null);
        player.stopChoosing();
        closeInventory(player);
        player.getPlayer().updateInventory();
        player.hideView();
        //player.sendTitle(RED+"Vous n'infectez personne", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
        player.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
        player.getPlayer().closeInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDayStart(LGNightEndEvent e) {
        if (e.getGame() != getGame()) return;

        getGame().getAlive().stream().filter(player -> player.getCache().getBoolean("just_infected")).forEach(player -> {
            player.getCache().remove("just_infected");
            player.sendMessage(GOLD + "Tu as été infecté pendant la nuit.");
            player.sendMessage(GOLD + ITALIC + "Tu gagnes désormais avec les " + RED + BOLD + ITALIC + "Loups-Garous" + GOLD + ITALIC + ".");
            if (!player.isDead()) {//Si il n'a pas été tué je ne sais comment
                RWereWolf.forceJoin(player);
                LGCardItems.updateItem(player);
            }

            for (LGPlayer lgp : getGame().getInGame()) {
                if (lgp.getRoleType() == RoleType.LOUP_GAROU)
                    lgp.sendMessage(GRAY + BOLD + player.getName() + GOLD + " s'est fait infecter pendant la nuit.");
                else lgp.sendMessage(GOLD + "Un joueur a été " + RED + BOLD + "infecté" + GOLD + " pendant la nuit.");
            }

            if (getGame().checkEndGame()) e.setCancelled(true);
        });
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        RWereWolf.forceJoin(player);
    }

    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof LGInventoryHolder) || !e.getInventory().getHolder().equals(invHolder) ||
            LGPlayer.get((Player) e.getPlayer()).getRole() != this || !inMenu) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }

    @EventHandler
    public void onCustomItemChange(LGCustomItemChangeEvent e) {
        if (e.getGame() == getGame()) if (e.getPlayer().getCache().getBoolean("infected")) e.getConstraints().add(Constraint.INFECTED);
    }

    @Setter
    @Getter
    public static class InfectAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced, AbilityConsume {
        private boolean cancelled;
        private LGPlayer target;
        private boolean forceMessage;
        private boolean forceConsume;
        public InfectAction(LGPlayer target) {
            this.target = target;
        }
    }
}
