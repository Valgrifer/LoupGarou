package fr.valgrifer.loupgarou.roles;

import com.comphenix.packetwrapper.WrapperPlayServerEntityEffect;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.events.LGPreDayStartEvent;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.MessageForced;
import fr.valgrifer.loupgarou.events.TakeTarget;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPriest extends Role {
    private static final ItemBuilder itemNoAction = ResourcePack.getItem("ui_cancel")
        .setCustomId("ac_no")
        .setDisplayName(GRAY + BOLD + "Ne rien faire")
        .setLore(DARK_GRAY + "Passez votre tour");
    private static final Map<LGPlayer, RPriest> playerToRole = new HashMap<>();
    private static final LGInventoryHolder invHolder;

    static {
        invHolder = new LGInventoryHolder(1, BLACK + "Veux-tu ressusciter quelqu'un ?");
        invHolder.setDefaultPreset(new MenuPreset(invHolder) {
            @Override
            protected void preset() {
                setSlot(3, new Slot(itemNoAction), ((holder, event) -> {
                    LGPlayer lgp = LGPlayer.get((Player) event.getWhoClicked());
                    RPriest role = playerToRole.get(lgp);

                    if (role == null) return;

                    role.closeInventory(lgp);
                    lgp.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
                    role.hidePlayers(lgp);
                    lgp.hideView();
                    role.callback.run();
                }));
                setSlot(5, new Slot(ItemBuilder.make(Material.ROTTEN_FLESH)
                    .setCustomId("ac_yes")
                    .setDisplayName(DARK_GREEN + BOLD + "Ressusciter")
                    .setLore(DARK_GRAY + "Tu peux ressusciter un " + RoleWinType.VILLAGE.getColoredName(BOLD),
                        DARK_GRAY + "mort précédemment pendant la partie.")), ((holder, event) -> {
                    Player player = (Player) event.getWhoClicked();
                    LGPlayer lgp = LGPlayer.get(player);
                    RPriest role = playerToRole.get(lgp);

                    if (role == null) return;

                    role.closeInventory(lgp);
                    player.getInventory().setItem(8, itemNoAction.build());
                    player.updateInventory();
                    //Pour éviter les missclick
                    WrapperPlayServerHeldItemSlot held = new WrapperPlayServerHeldItemSlot();
                    held.setSlot(0);
                    held.sendPacket(player);
                    lgp.sendMessage(GOLD + "Choisissez qui ressusciter.");
                    lgp.canSelectDead = true;
                    lgp.choose(choosen -> {
                        if (choosen == null) return;

                        if (!choosen.isDead()) {
                            lgp.sendMessage(GRAY + BOLD + choosen.getName() + RED + " n'est pas mort.");
                            return;
                        }
                        else if (lgp.getRoleType() == RoleType.LOUP_GAROU && choosen.getRoleType() == RoleType.NEUTRAL) {
                            lgp.sendMessage(
                                GRAY + BOLD + choosen.getName() + RED + " ne faisait ni partie du " + GREEN + BOLD + "Village" + GOLD + " ni des " +
                                    RED + BOLD + "Loups" + GOLD + ".");
                            return;
                        }
                        else if (lgp.getRoleType() != RoleType.LOUP_GAROU && choosen.getRoleType() != RoleType.VILLAGER) {
                            lgp.sendMessage(
                                GRAY + BOLD + choosen.getName() + RED + " ne faisait pas partie du " + GREEN + BOLD + "Village" + GOLD + ".");
                            return;
                        }

                        player.getInventory().setItem(8, null);
                        player.updateInventory();
                        lgp.stopChoosing();
                        lgp.canSelectDead = false;
                        role.hidePlayers(lgp);
                        lgp.hideView();

                        LGRoleActionEvent e = new LGRoleActionEvent(role.getGame(), new ReviveAction(choosen), lgp);
                        Bukkit.getPluginManager().callEvent(e);
                        ReviveAction action = (ReviveAction) e.getAction();
                        if (!action.isCancelled() || action.isForceMessage()) {
                            lgp.sendMessage(GOLD + "Tu as ramené " + GRAY + BOLD + action.getTarget().getName() + GOLD + " à la vie.");
                            lgp.sendActionBarMessage(GRAY + BOLD + action.getTarget().getName() + GOLD + " sera réssuscité");
                        }
                        else player.sendMessage(RED + "Votre cible est immunisée.");

                        if (action.isCancelled()) {
                            role.callback.run();
                            return;
                        }

                        action.getTarget().sendMessage(GOLD + "Tu vas être ressuscité en tant que " + GREEN + BOLD + "Villageois" + GOLD + ".");
                        role.ressucited.put(action.getTarget(), action.getNewRole());
                        role.getPlayers().remove(lgp);//Pour éviter qu'il puisse sauver plusieurs personnes.
                        role.callback.run();
                    }, lgp);
                }));
            }
        });
    }

    private final WrappedDataWatcherObject invisible = new WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    private final PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, 0, 0, false, false, false);
    private final PacketListener metadataListener = new PacketAdapter(MainLg.getInstance(), ListenerPriority.NORMAL,
        PacketType.Play.Server.ENTITY_METADATA) {
        @Override
        public void onPacketSending(PacketEvent e) {
            WrapperPlayServerEntityMetadata event = new WrapperPlayServerEntityMetadata(e.getPacket());
            LGPlayer player = LGPlayer.get(e.getPlayer());
            if (player.getGame() != getGame()) return;

            for (LGPlayer lgp : getGame().getInGame())
                if (lgp.getPlayer().getEntityId() == event.getEntityID() &&
                    (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType()))) return;

            event.setMetadata(Collections.singletonList(new WrappedWatchableObject(invisible, (byte) 0)));
        }
    };
    private final PacketListener effectListener = new PacketAdapter(MainLg.getInstance(), ListenerPriority.NORMAL,
        PacketType.Play.Server.ENTITY_EFFECT) {
        @Override
        public void onPacketSending(PacketEvent e) {
            WrapperPlayServerEntityEffect event = new WrapperPlayServerEntityEffect(e.getPacket());
            LGPlayer player = LGPlayer.get(e.getPlayer());
            if (player.getGame() != getGame()) return;

            for (LGPlayer lgp : getGame().getInGame())
                if (lgp.getPlayer().getEntityId() == event.getEntityID() &&
                    (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType()))) return;

            event.setPotionEffect(invisibleEffect);
        }
    };
    private final Map<LGPlayer, Boolean> inMenu = new HashMap<>();
    private final Map<LGPlayer, Class<? extends Role>> ressucited = new HashMap<>();
    Runnable callback;
    private boolean listenerRegistered = false;

    public RPriest(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return GREEN + BOLD + "Prêtre";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Une fois dans la partie, tu peux ressusciter parmi les morts un membre du " +
            RoleWinType.VILLAGE.getColoredName(BOLD) + WHITE + ", qui reviendra à la vie sans ses pouvoirs.";
    }

    public static String _getTask() {
        return "Veux-tu ressusciter un allié défunt ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " récite ses ouvrages...";
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    @Override
    public boolean hasPlayersLeft() {
        for (LGPlayer pretre : getPlayers())
            for (LGPlayer lgp : getGame().getInGame())
                if (lgp.isDead() && (lgp.getRoleType() == RoleType.VILLAGER || lgp.getRoleType() == pretre.getRoleType()))
                    return super.hasPlayersLeft();
        return false;
    }

    public void openInventory(LGPlayer player) {
        inMenu.put(player, true);
        playerToRole.put(player, this);
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        if (!listenerRegistered) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(metadataListener);
            protocolManager.addPacketListener(effectListener);

            listenerRegistered = true;
        }

        player.showView();
        for (LGPlayer lgp : getGame().getInGame()) {
            if (!lgp.isDead() || (lgp.getRoleType() != RoleType.VILLAGER && lgp.getRoleType() != player.getRoleType())) {
                player.getPlayer().hidePlayer(MainLg.getInstance(), lgp.getPlayer());
                continue;
            }

            if (lgp.getPlayer() == null) continue;

            player.getPlayer().showPlayer(MainLg.getInstance(), lgp.getPlayer());
            WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
            meta.setEntityID(lgp.getPlayer().getEntityId());
            meta.setMetadata(Collections.singletonList(new WrappedWatchableObject(invisible, (byte) 0)));
            meta.sendPacket(player.getPlayer());
            WrapperPlayServerEntityEffect effect = new WrapperPlayServerEntityEffect();
            effect.setEntityID(lgp.getPlayer().getEntityId());
            effect.setPotionEffect(invisibleEffect);
            effect.sendPacket(player.getPlayer());
        }
        this.callback = callback;
        openInventory(player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        if (listenerRegistered) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.removePacketListener(metadataListener);
            protocolManager.removePacketListener(effectListener);

            listenerRegistered = false;
        }

        player.getPlayer().getInventory().setItem(8, null);
        player.stopChoosing();
        closeInventory(player);
        player.canSelectDead = false;
        player.getPlayer().updateInventory();
        hidePlayers(player);
        player.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
    }

    private void hidePlayers(LGPlayer player) {
        if (player.getPlayer() != null) {
            for (LGPlayer lgp : getGame().getInGame())
                if (lgp.getPlayer() != null && lgp != player) player.getPlayer().hidePlayer(MainLg.getInstance(), lgp.getPlayer());
        }
    }

    private void closeInventory(LGPlayer player) {
        inMenu.remove(player);
        playerToRole.remove(player);
        player.getPlayer().closeInventory();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        LGPlayer lgp = LGPlayer.get(player);

        if (lgp.getRole() != this || !ItemBuilder.checkId(e.getItem(), itemNoAction.getCustomId())) return;

        e.setCancelled(true);
        player.getInventory().setItem(8, null);
        player.updateInventory();
        lgp.stopChoosing();
        lgp.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
        lgp.canSelectDead = false;
        hidePlayers(lgp);
        callback.run();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDayStart(LGPreDayStartEvent e) {
        if (e.getGame() != getGame()) return;

        if (ressucited.size() == 0) return;

        for (LGPlayer lgp : ressucited.keySet()) {
            if (lgp.getPlayer() == null || !lgp.isDead()) continue;

            lgp.setDead(false);
            lgp.getCache().reset();
            Role role = getGame().getRole(ressucited.get(lgp));
            if (role == null) {
                try {
                    getGame().getRoles().add(role = ressucited.get(lgp).getConstructor(LGGame.class).newInstance(getGame()));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    ex.printStackTrace();
                    role = getGame().getRole(RVillager.class);
                    if (role == null) getGame().getRoles().add(role = new RVillager(getGame()));
                }
            }
            role.join(lgp, false);
            lgp.setRole(role);
            lgp.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            lgp.getPlayer().getInventory().setHelmet(null);
            lgp.getPlayer().updateInventory();
            LGCardItems.updateItem(lgp);

            lgp.joinChat(getGame().getChat(LGChatType.VILLAGE));//Pour qu'il ne parle plus dans le chat des morts (et ne le voit plus) et qu'il parle dans le chat des vivants
            VariousUtils.setWarning(lgp.getPlayer(), true);


            if (MainLg.getInstance().getConfig().getBoolean("compo.update_on_kill", true)) getGame().updateRoleScoreboard();

            getGame().broadcastMessage(GRAY + BOLD + lgp.getName() + GOLD + " a été ressuscité cette nuit.", true);

            for (LGPlayer player : getGame().getInGame())
                if (player.getPlayer() != null && player != lgp) player.getPlayer().showPlayer(MainLg.getInstance(), lgp.getPlayer());
        }
        ressucited.clear();
    }

    @EventHandler
    public void onQuitInventory(InventoryCloseEvent e) {
        LGPlayer player;
        if (!(e.getInventory().getHolder() instanceof LGInventoryHolder) || !e.getInventory().getHolder().equals(invHolder) ||
            (player = LGPlayer.get((Player) e.getPlayer())).getRole() != this || !inMenu.getOrDefault(player, false)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().openInventory(e.getInventory());
            }
        }.runTaskLater(MainLg.getInstance(), 1);
    }

    public static class ReviveAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced
    {
        @Getter
        @Setter
        private boolean cancelled;
        @Getter
        @Setter
        private LGPlayer target;
        @Getter
        @Setter
        private Class<? extends Role> newRole = RVillager.class;
        @Getter
        @Setter
        private boolean forceMessage;
        public ReviveAction(LGPlayer target) {
            this.target = target;
        }
    }
}
