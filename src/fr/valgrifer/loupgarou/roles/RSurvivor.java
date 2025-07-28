package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
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
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RSurvivor extends Role {
    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(3, new Slot(ResourcePack.getItem("ui_cancel")
                .setCustomId("ac_skip")
                .setDisplayName(GRAY + BOLD + "Ne rien faire")
                .setLore(DARK_GRAY + "Passez votre tour")), (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RSurvivor)) return;

                RSurvivor role = (RSurvivor) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(DARK_RED + ITALIC + "Tu es sans défense...");
                lgp.hideView();
                role.callback.run();
            });

            setSlot(5, new Slot(ResourcePack.getItem("ui_validation")
                .setCustomId("ac_protect")
                .setLore(DARK_GRAY + "Tu ne pourras pas être tué par", DARK_GRAY + "les " + RED + BOLD + "Loups" + DARK_GRAY + " cette nuit.")) {
                @Override
                public ItemBuilder getItem(LGInventoryHolder holder) {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return MenuPreset.lockSlot.getDefaultItem();

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    return getDefaultItem().setDisplayName(
                        DARK_GREEN + BOLD + "Se protéger (" + GOLD + BOLD + lgp.getCache().<Integer> get("survivant_left") + DARK_GREEN + BOLD +
                            " restant)");
                }
            }, (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (lgp.getCache().get("survivant_left", 0) <= 0) return;

                if (!(lgp.getRole() instanceof RSurvivor)) return;

                RSurvivor role = (RSurvivor) lgp.getRole();

                role.closeInventory(lgp);
                lgp.hideView();

                LGRoleActionEvent e = new LGRoleActionEvent(role.getGame(), new ProtectAction(), lgp);
                Bukkit.getPluginManager().callEvent(e);
                ProtectAction action = (ProtectAction) e.getAction();
                if (!action.isCancelled() || action.isForceMessage()) {
                    lgp.sendActionBarMessage(BLUE + BOLD + "Tu as décidé de te protéger.");
                    lgp.sendMessage(GOLD + "Tu as décidé de te protéger.");
                }
                else lgp.sendMessage(RED + "Tu ne peux pas te protégé.");

                if (!action.isCancelled() || action.isForceConsume())
                    lgp.getCache().set("survivant_left", lgp.getCache().get("survivant_left", 0) - 1);

                if (action.isCancelled()) {
                    role.callback.run();
                    return;
                }

                lgp.getCache().set("survivant_protected", true);
                role.callback.run();
            });
        }
    };
    Runnable callback;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

    public RSurvivor(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.NONE;
    }

    public static String _getName() {
        return LIGHT_PURPLE + BOLD + "Survivant";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes si tu remplis ton objectif";
    }

    public static String _getDescription() {
        return WHITE + "Tu es " + RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD) + WHITE +
            " et tu gagnes si tu remplis ton objectif. Ton objectif est de survivre. Tu disposes de " + BOLD + "2" + WHITE +
            " protections. Chaque nuit, tu peux utiliser une protection pour ne pas être tué par les " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) +
            WHITE + ". Tu peux gagner aussi bien avec les " + RoleType.VILLAGER.getColoredName(BOLD) + WHITE + " qu'avec les " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ", tu dois juste rester en vie jusqu'à la fin de la partie.";
    }

    public static String _getTask() {
        return "Veux-tu utiliser une protection cette nuit ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " décide s'il veut se protéger.";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Veux-tu te protéger ?", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    @Override
    public void join(LGPlayer player) {
        super.join(player);
        player.getCache().set("survivant_left", 2);
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();
        this.callback = callback;
        openInventory(player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.hideView();
        closeInventory(player);
        player.sendMessage(DARK_RED + ITALIC + "Tu es sans défense...");
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
        player.getPlayer().closeInventory();
    }

    @EventHandler
    public void onPlayerKill(LGNightPlayerPreKilledEvent e) {
        if (e.getGame() == getGame() &&
            (e.getReason() == Reason.LOUP_GAROU || e.getReason() == Reason.LOUP_BLANC || e.getReason() == Reason.GM_LOUP_GAROU ||
                e.getReason() == Reason.ASSASSIN) && e.getKilled().getCache().getBoolean("survivant_protected") && e.getKilled().isRoleActive())
            e.setCancelled(true);
    }

    @EventHandler
    public void onVampired(LGRoleActionEvent e) {
        if (e.getGame() != getGame()) return;
        if (e.isAction(RVampire.VampiredAction.class)) {
            RVampire.VampiredAction action = (RVampire.VampiredAction) e.getAction();
            if (action.getTarget().getRole() == this && action.getTarget().isRoleActive()) action.setProtect(true);
        }
    }

    @EventHandler
    public void onDayStart(LGPreDayStartEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getGame().getInGame())
            if (lgp.isRoleActive()) lgp.getCache().remove("survivant_protected");
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
    public void onWin(LGGameEndEvent e) {
        if (e.getGame() == getGame() && getPlayers().size() > 0 && e.getWinType() != LGWinType.ANGE) {
            for (LGPlayer lgp : getPlayers())
                e.getWinners().add(lgp);
            new BukkitRunnable() {
                @Override
                public void run() {
                    getGame().broadcastMessage(GOLD + ITALIC + "Le " + getName() + GOLD + ITALIC + " a rempli son objectif.", true);
                }
            }.runTaskAsynchronously(MainLg.getInstance());
        }
    }

    public static class ProtectAction implements LGRoleActionEvent.RoleAction, Cancellable, MessageForced, AbilityConsume {
        @Getter
        @Setter
        private boolean cancelled;
        @Getter
        @Setter
        private boolean forceMessage;
        @Getter
        @Setter
        private boolean forceConsume;
        public ProtectAction() { }
    }
}
