package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPsychopath extends Role implements Listener {
    public static final String LifeKey = "psychopath_life";
    public static final String SavedKey = "psychopath_saved";
    public static final String PsychopathItemId = "psychopath";
    public static final ItemStack PsychopathItem = ItemBuilder.make(Material.BOOK)
                                                           .setCustomId(PsychopathItemId)
                                                           .setDisplayName(RESET + "Deviner & Tuer")
                                                           .setLore(
                                                                   GRAY + "Durant le jour",
                                                                   GRAY + "tu peux deviner le role d'un joueur",
                                                                   GRAY + "si tu as raison il meurt sur le coup",
                                                                   GRAY + "mais si tu as tort tu mourras."
                                                           )
                                                           .build();
    public static final LGPlayerKilledEvent.Reason PSYCHOPATH_GOOD = LGPlayerKilledEvent.Reason.register(
            "PSYCHOPATH_GOOD",
            GRAY + BOLD + "%s" + DARK_RED + " est mort d'une crise cardiaque?"
    );
    public static final LGPlayerKilledEvent.Reason PSYCHOPATH_BAD = LGPlayerKilledEvent.Reason.register(
            "PSYCHOPATH_BAD",
            PSYCHOPATH_GOOD.getMessage()
    );
    public static final LGWinType PSYCHOPATH = LGWinType.register(
            "PSYCHOPATH",
            GOLD + BOLD + ITALIC + "La partie a été gagnée par " + _getFriendlyName() + GOLD + BOLD + ITALIC + " !"
    );
    private static final String PsychopathInventoryKey = "psychopath_inventory";
    private static final String PsychopathPlayerSelectedKey = "psychopath_player_selected";
    private LGVote vote = null;

    public RPsychopath(LGGame game) {
        super(game);
    }

    private LGPrivateInventoryHolder makeInventory(@Nonnull LGPlayer player) {
        LGPrivateInventoryHolder inventoryHolder = new LGPrivateInventoryHolder(6, _getName(), player);

        PaginationMapPreset<LGPlayer> playerPreset = new PaginationMapPreset<LGPlayer>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK)) {
                    @Override
                    public ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                       .setLore(AQUA + BOLD + "Click " + GRAY + "pour sélectionner le joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(LGPlayer player) {
                return ItemBuilder.make(Material.PLAYER_HEAD)
                               .setCustomId(player.getPlayer().getUniqueId().toString())
                               .setDisplayName(RESET + player.getName())
                               .setSkull(player.getPlayer());
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, LGPlayer player) {
                holder.getCache().set(PsychopathPlayerSelectedKey, player);
                holder.loadPreset("role");
            }

            @Override
            public List<LGPlayer> getObjects() {
                assert getGame() != null;
                return getGame().getAlive(lgp -> lgp != player);
            }
        };

        PaginationMapPreset<Role> rolePreset = new PaginationMapPreset<Role>(inventoryHolder) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new Slot(ItemBuilder.make(Material.BOOK)) {
                    @Override
                    public ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                       .setLore(AQUA + BOLD + "Click " + GRAY + "pour sélectionner le role du joueur");
                    }
                };
            }

            @Override
            protected ItemBuilder mapList(Role role) {
                return Role.getCard(role.getClass());
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, Role role) {
                LGPlayer target = holder.getCache().get(PsychopathPlayerSelectedKey);

                LGRoleActionEvent guessTargetEvent = new LGRoleActionEvent(getGame(), new PsychopathGuessAction(target, role), player);
                Bukkit.getPluginManager().callEvent(guessTargetEvent);

                PsychopathGuessAction action = (PsychopathGuessAction) guessTargetEvent.getAction();

                if (!action.isCancelled() || action.isForceConsume())
                    player.getPlayer().getInventory().setItem(8, null);

                player.getPlayer().closeInventory();

                if (action.isCancelled())
                    return;

                LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(
                        getGame(), action.isGoodGuess() ? action.getTarget() : player,
                        action.isGoodGuess() ? PSYCHOPATH_GOOD : PSYCHOPATH_BAD
                );
                Bukkit.getPluginManager().callEvent(killEvent);

                if (!killEvent.isCancelled()) {
                    getGame().kill(killEvent.getKilled(), killEvent.getReason(), true);

                    vote.getParticipants().remove(killEvent.getKilled());
                    vote.getVotes().remove(killEvent.getKilled());
                    killEvent.getKilled().getPlayer().closeInventory();
                }
            }

            @Override
            public List<Role> getObjects() {
                return playerPreset.getObjects().stream().map(LGPlayer::getRole).distinct().collect(Collectors.toList());
            }
        };

        inventoryHolder.savePreset("player", playerPreset);
        inventoryHolder.savePreset("role", rolePreset);

        return inventoryHolder;
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return DARK_AQUA + BOLD + "Kira";
    }

    public static String _getFriendlyName() {
        return "le " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes " + RoleWinType.SOLO.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
                       ". Durant le jour, tu peux deviner le role d'un joueur, si tu as raison il meurt sur le coup, mais si tu as tort tu mourras. " +
                       "Vous aurez également une vie supplémentaire contre les " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ". ";
    }

    @Override
    public void join(LGPlayer player) {
        super.join(player);
        player.getCache().set(LifeKey, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLGVoteStart(LGVoteStartEvent event) {
        if (event.getCause() != LGVoteCause.VILLAGE) return;
        if (event.getGame() != getGame()) return;

        vote = event.getVote();

        getGame().getAlive(lgPlayer -> lgPlayer.getCache().getBoolean(SavedKey)).forEach(lgPlayer -> {
            lgPlayer.getCache().remove(SavedKey);
            lgPlayer.sendMessage(DARK_AQUA + "Votre vie a été consumée cette nuit, vous devrez être plus vigilant à partir de maintenant.");
        });

        getPlayers()
                .stream()
                .filter(LGPlayer::isRoleActive)
                .forEach(player -> player.getPlayer().getInventory().setItem(8, PsychopathItem));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLGVoteEnd(LGVoteEndEvent event) {
        if (event.getCause() != LGVoteCause.VILLAGE) return;
        if (event.getGame() != getGame()) return;

        vote = null;

        getPlayers().forEach(player -> {
            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().closeInventory();
        });
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        LGPlayer player = LGPlayer.get(event.getPlayer());

        if (player.getGame() != getGame()) return;

        if (!ItemBuilder.checkId(event.getItem(), PsychopathItemId)) return;

        event.setCancelled(true);

        LGInventoryHolder inventoryHolder = player.getCache().computeIfAbsent(PsychopathInventoryKey, () -> makeInventory(player));

        inventoryHolder.loadPreset("player");
        inventoryHolder.getCache().remove(PsychopathPlayerSelectedKey);

        player.getPlayer().openInventory(inventoryHolder.getInventory());
    }

    @EventHandler
    public void onDeathAnnouncement(LGDeathAnnouncementEvent e) {
        if (e.getGame() != getGame())
            return;

        if ((e.getReason() == PSYCHOPATH_GOOD || e.getReason() == PSYCHOPATH_BAD || (e.getKilled().getRole() == this && getGame().getRole(RPriestess.class) != null)))
            e.setShowedRole(HiddenRole.class);
    }

    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && !getPlayers().isEmpty()) e.setWinType(PSYCHOPATH);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if (e.getWinType() == PSYCHOPATH) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }

        getPlayers().forEach(player -> {
            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().closeInventory();
        });
    }


    @EventHandler
    public void onPlayerKill(LGNightPlayerPreKilledEvent e) {
        if (e.getGame() != getGame()) return;

        if (!e.isCancelled() && (e.getReason() == LGPlayerKilledEvent.Reason.LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.LOUP_BLANC ||
                                         e.getReason() == LGPlayerKilledEvent.Reason.GM_LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.ASSASSIN) &&
                    e.getKilled().getCache().get(LifeKey, 0) > 0 && e.getKilled().isRoleActive()) {
            e.setCancelled(true);
            e.getKilled().getCache().set(LifeKey, e.getKilled().getCache().<Integer> get(LifeKey) - 1);
            e.getKilled().getCache().set(SavedKey, true);
        }

        if (e.getKilled().getRole() instanceof RPsychopath) {
            e.getKilled().getPlayer().getInventory().setItem(8, null);
            e.getKilled().getPlayer().closeInventory();
        }
    }

    @Setter
    @Getter
    public static class PsychopathGuessAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, AbilityConsume {
        private boolean cancelled;
        private boolean forceConsume;
        private LGPlayer target;
        private Role role;

        public PsychopathGuessAction(LGPlayer target, Role role) {
            this.target = target;
            this.role = role;
        }

        public boolean isGoodGuess() {
            return this.target.getRole() == this.role;
        }
    }
}
