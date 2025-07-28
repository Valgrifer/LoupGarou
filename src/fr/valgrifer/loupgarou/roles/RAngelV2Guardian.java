package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RAngelV2Guardian extends Role {
    public static final String LifeKey = "guardian_left";
    public static final String GuardKey = "guardian_protected";


    private static final ItemBuilder itemNoAction = ResourcePack.getItem("ui_cancel")
        .setCustomId("ac_no")
        .setDisplayName(GRAY + BOLD + "Ne rien faire")
        .setLore(DARK_GRAY + "Passez votre tour");

    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(3, new MenuPreset.Slot(itemNoAction), ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RAngelV2Guardian)) return;

                RAngelV2Guardian role = (RAngelV2Guardian) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(RED + "Vous ne protégez pas votre cible.");
                lgp.hideView();
                role.callback.run();
            }));
            setSlot(5, new Slot(ItemBuilder.make(Material.TOTEM_OF_UNDYING)
                .setCustomId("ac_yes")
                .setLore(DARK_GRAY + "Votre Cible ne pourras pas être tué par",
                    DARK_GRAY + "les " + RED + BOLD + "Loups" + DARK_GRAY + " cette nuit.")) {
                @Override
                public ItemBuilder getItem(LGInventoryHolder holder) {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return MenuPreset.lockSlot.getDefaultItem();

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    return getDefaultItem().setDisplayName(
                        DARK_GREEN + BOLD + "Protéger (" + GOLD + BOLD + lgp.getCache().<Integer> get(LifeKey) + DARK_GREEN + BOLD + " restant)");
                }
            }, ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RAngelV2Guardian)) return;

                if (lgp.getCache().get(LifeKey, 0) <= 0) return;

                RAngelV2Guardian role = (RAngelV2Guardian) lgp.getRole();

                role.closeInventory(lgp);
                lgp.hideView();

                LGRoleActionEvent e = new LGRoleActionEvent(role.getGame(), new GuardAction(), lgp);
                Bukkit.getPluginManager().callEvent(e);
                GuardAction action = (GuardAction) e.getAction();
                if (!action.isCancelled() || action.isForceMessage()) {
                    lgp.sendActionBarMessage(BLUE + BOLD + "Tu as décidé de protéger ta cible.");
                    lgp.sendMessage(GOLD + "Tu as décidé de protéger ta cible.");
                }
                else lgp.sendMessage(RED + "Tu ne peux pas protégé ta cible.");

                if (!action.isCancelled() || action.isForceConsume()) lgp.getCache().set(LifeKey, lgp.getCache().get(LifeKey, 0) - 1);

                if (action.isCancelled()) {
                    role.callback.run();
                    return;
                }

                ((LGPlayer) lgp.getCache().get(RAngelV2.TargetKey)).getCache().set(GuardKey, true);
                role.callback.run();
            }));
        }
    };
    private Runnable callback;
    private LGPrivateInventoryHolder invHolder;
    private boolean inMenu = false;

    public RAngelV2Guardian(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return YELLOW + BOLD + "Ange Gardien";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getScoreBoardName();
    }

    public static String _getScoreBoardName() {
        return RAngelV2._getScoreBoardName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes si tu remplis ton objectif";
    }

    public static String _getDescription() {
        return WHITE + "Tu es " + RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD) + WHITE + " et tu gagnes si tu remplis ton objectif. " +
            "Tu as une cible à protéger en " + YELLOW + BOLD + "Ange Gardien" + WHITE + ". " + "Vous aurez une vie supplémentaire contre les " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + " pour réussir votre mission. " + "En " + YELLOW + BOLD + "Ange Gardien" + WHITE +
            ", ton objectif est de garder en vie ton protégé, pour cela tu pourras le protéger jusqu'à 2x durant la partie.";
    }

    public static String _getTask() {
        return "Choisis ton Role";
    }

    public static String _getBroadcastedTask() {
        return "L'" + _getScoreBoardName() + BLUE + " réfléchit.";
    }

    @Override
    public int getTimeout() {
        return 20;
    }

    @Override
    public void join(LGPlayer player) {
        super.join(player);
        player.getCache().set(LifeKey, 2);
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Choisis ce que tu veux être!", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        player.getPlayer().closeInventory();
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
    }

    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof LGPrivateInventoryHolder) || !e.getInventory().getHolder().equals(invHolder) ||
            LGPlayer.get((Player) e.getPlayer()).getRole() != this || !inMenu) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }


    @EventHandler
    public void onPlayerKill(LGNightPlayerPreKilledEvent e) {
        if (e.getGame() != getGame()) return;

        if ((e.getReason() == LGPlayerKilledEvent.Reason.LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.LOUP_BLANC ||
            e.getReason() == LGPlayerKilledEvent.Reason.GM_LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.ASSASSIN) &&
            e.getKilled().getCache().getBoolean(GuardKey)) e.setCancelled(true);
    }

    @EventHandler
    public void onDayStart(LGPreDayStartEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getGame().getInGame())
            lgp.getCache().remove(GuardKey);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() == getGame()) if (e.getTo().getRole() instanceof RAngelV2Guardian && e.getTo().getCache().has(RAngelV2.TargetKey) &&
            e.getTo().getCache().<LGPlayer> get(RAngelV2.TargetKey).equals(e.getPlayer())) e.setPrefix(GREEN + "⌖ " + e.getPrefix() + RESET);
    }


    @EventHandler
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() != getGame()) return;

        List<LGPlayer> winners = getPlayers().stream()
            .filter(lgPlayer -> !lgPlayer.isDead())
            .filter(lgPlayer -> !getGame().getAlive(pl -> lgPlayer.getCache().get(RAngelV2.TargetKey) == pl).isEmpty())
            .collect(Collectors.toCollection(ArrayList::new));

        if (!winners.isEmpty()) new BukkitRunnable() {
            @Override
            public void run() {
                getGame().broadcastMessage(GOLD + ITALIC + "L'" + getName() + GOLD + ITALIC + " a rempli son objectif.", true);
            }
        }.runTaskAsynchronously(MainLg.getInstance());

        e.getWinners().addAll(winners);
    }

    @Getter
    @Setter
    public static class GuardAction implements LGRoleActionEvent.RoleAction, Cancellable, MessageForced, AbilityConsume {
        private boolean cancelled;
        private boolean forceMessage;
        private boolean forceConsume;
        public GuardAction() { }
    }
}
