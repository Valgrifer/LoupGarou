package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPuppeteer extends Role implements Listener {
    public static final String PowerFreeUse = "puppeteer_free_use";
    public static final String PowerCountKey = "puppeteer_power";
    private static final int PowerDefault = 2;

    public static final String PuppetKey = "puppet";
    public static final String PuppetTargetKey = "puppet_target";
    private static final int PuppetTime = 2;

    public static final String PuppeteerItemId = "puppeteer";
    public static final ItemStack PuppeteerItem = ItemBuilder.make(Material.BOOK)
                                                          .setCustomId(PuppeteerItemId)
                                                          .setDisplayName(RESET + "Geass")
                                                          .setLore(GRAY + "Manipulation du village")
                                                          .build();

    public static final LGWinType PUPPETEER = LGWinType.register(
            "PUPPETEER",
            GOLD + BOLD + ITALIC + "La partie a été gagnée par " + _getFriendlyName() + GOLD + BOLD + ITALIC + " !"
    );
    private static final String PuppeteerInventoryKey = "puppeteer_inventory";
    private static final String PuppeteerVoterSelectedKey = "puppeteer_voter_selected";

    @Getter
    private final LGChat chat;

    private LGVote vote = null;

    private List<LGPlayer> previousTargets = null;
    private List<LGPlayer> targets = new ArrayList<>();

    public RPuppeteer(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.SPY) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                return context.getChat().receive(context, message);
            }
        };
    }

    private LGPrivateInventoryHolder makeInventory(@Nonnull LGPlayer player) {
        LGPrivateInventoryHolder inventoryHolder = new LGPrivateInventoryHolder(6, _getName(), player);

        PaginationMapPreset<LGPlayer> voterPreset = new PaginationMapPreset<LGPlayer>(inventoryHolder) {
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
                holder.getCache().set(PuppeteerVoterSelectedKey, player);
                holder.loadPreset("voted");
            }

            @Override
            public List<LGPlayer> getObjects() {
                return vote.getParticipants().stream().filter(lgp -> lgp != player && lgp.getCache().get(PuppetKey, 0) <= 0).collect(Collectors.toList());
            }
        };

        PaginationMapPreset<LGPlayer> votedPreset = new PaginationMapPreset<LGPlayer>(inventoryHolder) {
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
                if (LGVote.getBlank().equals(player))
                    return LGVote.getItemBlankVote();

                return ItemBuilder.make(Material.PLAYER_HEAD)
                               .setCustomId(player.getPlayer().getUniqueId().toString())
                               .setDisplayName(RESET + player.getName())
                               .setSkull(player.getPlayer());
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, LGPlayer voted) {
                LGPlayer voter = holder.getCache().get(PuppeteerVoterSelectedKey);

                LGRoleActionEvent guessTargetEvent = new LGRoleActionEvent(getGame(), new PuppeteerOrderAction(voter, voted), player);
                Bukkit.getPluginManager().callEvent(guessTargetEvent);

                PuppeteerOrderAction action = (PuppeteerOrderAction) guessTargetEvent.getAction();

                if (!action.isCancelled() || action.isForceConsume()) {
                    if (player.getCache().getBoolean(PowerFreeUse)) {
                        player.getCache().set(PowerFreeUse, false);
                        player.sendMessage(DARK_AQUA + String.format(
                                "Vous avez utilisez, votre utilisation gratuit, il vous reste %s utilisation.",
                                player.getCache().get(PowerCountKey, 0)
                        ));
                    }
                    else {
                        int remaining = player.getCache().get(PowerCountKey, 0) - 1;

                        player.getCache().set(PowerCountKey, remaining);
                        if (remaining > 0)
                            player.sendMessage(DARK_AQUA + String.format("Il vous reste %s utilisation.", remaining));
                        else
                            player.sendMessage(DARK_AQUA + "Il ne vous reste plus de pouvoir.");

                        if (remaining <= 0)
                            player.getPlayer().getInventory().setItem(8, null);
                    }

                    player.getPlayer().closeInventory();
                }

                if (action.isCancelled())
                    return;

                vote.vote(action.getVoter(), action.getVoted());
                action.getVoter().setCanChoose(false);
                action.getVoter().getCache().set(PuppetKey, PuppetTime);
                action.getVoter().sendMessage(RED + "Vous êtes manipulé par " + _getFriendlyName() + RESET + RED + ".");
                action.getVoted().getCache().get(PuppetTargetKey, true);

                Player plv = action.getVoter().getPlayer();

                if (plv == null)
                    return;

                Location loc = plv.getLocation();

                if (action.getVoter() == action.getVoted())
                    loc.setPitch(75);
                else if (action.getVoted().getPlayer() != null) {
                    loc.setYaw(VariousUtils.getAngle(action.getVoted().getPlayer().getLocation(), plv.getLocation()));
                    loc.setPitch(0);
                }

                plv.teleport(loc);

                plv.swingMainHand();
            }

            @Override
            public List<LGPlayer> getObjects() {
                List<LGPlayer> blacklisted = vote.getBlacklisted();
                List<LGPlayer> list        = getGame().getAlive().stream().filter(lgp -> !blacklisted.contains(lgp)).collect(Collectors.toList());
                if (vote.isAllowBlankVote())
                    list.add(LGVote.getBlank());
                return list;
            }
        };

        inventoryHolder.savePreset("voter", voterPreset);
        inventoryHolder.savePreset("voted", votedPreset);

        return inventoryHolder;
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return DARK_BLUE + BOLD + "Lelouch";
    }

    public static String _getFriendlyName() {
        return _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes " + RoleWinType.SOLO.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
                       ". Tu peux forcer un vote par phase (jour ou nuit) et par consommation de charge. Les manipulés ne peuvent plus changer de vote ni être re-ciblés pendant un jour. Tu entends et vois les " + RoleWinType.LOUP_GAROU.getColoredName(
                BOLD) + WHITE + " la nuit. Et tu gagnes une charge si ta manipulation tue le maire ou la cible survivante des " + RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + " le vote suivant.";
    }

    @Override
    public void join(LGPlayer player) {
        super.join(player);
        player.getCache().set(PowerCountKey, PowerDefault);
        player.getCache().set(PowerFreeUse, false);
    }

    @EventHandler
    public void onNightStart(LGNightStart e) {
        if (e.getGame() != getGame()) return;

        if (e.getGame().getNight() != 0) return;

        RWereWolf wereWolf = e.getGame().getRole(RWereWolf.class);

        if (wereWolf == null) return;

        wereWolf.getChat().join(this.getChat());
    }

    @EventHandler
    public void onNightEnd(LGNightEndEvent event) {
        if (event.getGame() != getGame()) return;

        this.previousTargets = this.targets;
        this.targets = new ArrayList<>();
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKilled(LGPlayerKilledEvent event) {
        if (event.getGame() != getGame()) return;

        if (event.getKilled() == null) return;

        if (event.isCancelled()) {
            if (event.getReason() == LGPlayerKilledEvent.Reason.LOUP_GAROU || event.getReason() == LGPlayerKilledEvent.Reason.GM_LOUP_GAROU)
                targets.add(event.getKilled());
        }
        else {
            if (event.getKilled().getRole() instanceof RPuppeteer) {
                event.getKilled().getPlayer().getInventory().setItem(8, null);
                event.getKilled().getPlayer().closeInventory();
            }

            if (this.previousTargets.contains(event.getKilled()) &&
                        event.getKilled().getCache().getBoolean(PuppetTargetKey) &&
                        (event.getReason() == LGPlayerKilledEvent.Reason.LOUP_GAROU ||
                        event.getReason() == LGPlayerKilledEvent.Reason.VOTE))
                getPlayers()
                        .stream()
                        .filter(LGPlayer::isRoleActive)
                        .forEach(lgp -> {
                            lgp.getCache().set(PowerCountKey, lgp.getCache().get(PowerCountKey, 0) + 1);
                            lgp.sendMessage(DARK_AQUA + "Votre pouvoir se renforce.");
                        });
        }
    }

    @EventHandler
    public void onRoleTurnEnd(LGRoleTurnEndEvent event) {
        if (event.getGame() != getGame()) return;

        if (event.getNewRole() instanceof RWereWolf)
            this.getPlayers()
                    .stream()
                    .filter(player -> !player.getCache().getBoolean("infected") && player.isRoleActive())
                    .forEach(player -> {
                        player.showView();
                        player.joinChat(this.getChat(), true);
                    });
        if (event.getPreviousRole() instanceof RWereWolf)
            this.getPlayers()
                    .stream()
                    .filter(player -> !player.getCache().getBoolean("infected") && player.isRoleActive())
                    .forEach(player -> {
                        player.leaveChat();
                        player.hideView();
                    });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLGVoteStart(LGVoteStartEvent event) {
        if (event.getGame() != getGame()) return;
        if (event.getCause() != LGVoteCause.VILLAGE && event.getCause() != LGVoteCause.LOUPGAROU) return;

        vote = event.getVote();

        if (getGame().getMayor() != null)
            targets.add(getGame().getMayor());

        getGame().getInGame()
                .stream()
                .filter(lgp -> lgp.getCache().get(PuppetKey, 0) > 0)
                .forEach(lgp -> lgp.getCache().set(PuppetKey, lgp.getCache().get(PuppetKey, 0) - 1));

        getPlayers()
                .stream()
                .filter(LGPlayer::isRoleActive)
                .forEach(player -> {
                    player.getCache().set(PowerFreeUse, true);
                    player.sendMessage(DARK_AQUA + "Votre utilisation gratuit vous a été attribué.");
                    List<String> targets = this.targets.stream().filter(lgp -> lgp != player).map(LGPlayer::getName).collect(Collectors.toList());
                    if (!targets.isEmpty())
                        player.sendMessage(DARK_AQUA + String.format("Si %s meurs du vote, votre pouvoir se renforcera.", VariousUtils.frenchFormatList(targets, "ou")));
                    player.getPlayer().getInventory().setItem(8, PuppeteerItem);
                });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLGVoteEnd(LGVoteEndEvent event) {
        if (event.getGame() != getGame()) return;

        vote = null;

        getPlayers().forEach(player -> {
            player.getCache().set(PowerFreeUse, false);
            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().closeInventory();
        });
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        LGPlayer player = LGPlayer.get(event.getPlayer());

        if (player.getGame() != getGame()) return;

        if (!ItemBuilder.checkId(event.getItem(), PuppeteerItemId)) return;

        event.setCancelled(true);

        LGInventoryHolder inventoryHolder = player.getCache().computeIfAbsent(PuppeteerInventoryKey, () -> makeInventory(player));

        inventoryHolder.loadPreset("voter");
        inventoryHolder.getCache().remove(PuppeteerVoterSelectedKey);

        player.getPlayer().openInventory(inventoryHolder.getInventory());
    }


    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && !getPlayers().isEmpty()) e.setWinType(PUPPETEER);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if (e.getWinType() == PUPPETEER) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }

        getPlayers().forEach(player -> {
            player.getPlayer().getInventory().setItem(8, null);
            player.getPlayer().closeInventory();
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkinChange(LGSkinLoadEvent e) {
        if (e.getGame() != getGame())
            return;

        RWereWolf role = getGame().getRole(RWereWolf.class);

        if (role == null)
            return;

        if (role.getVisiblePlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()) && role.showSkins) {
            e.getProfile().getProperties().removeAll("textures");
            e.getProfile().getProperties().put("textures", LGCustomSkin.WEREWOLF.getProperty());
        }
    }

    @EventHandler
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() != getGame())
            return;

        RWereWolf role = getGame().getRole(RWereWolf.class);

        if (role == null)
            return;

        if (e.getGame() == getGame())
            if (role.getVisiblePlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()))
                e.setColorName(ChatColor.RED);
    }

    @Setter
    @Getter
    public static class PuppeteerOrderAction implements LGRoleActionEvent.RoleAction, Cancellable, AbilityConsume {
        private boolean cancelled;
        private boolean forceConsume;
        private @Nonnull LGPlayer voter;
        private @Nonnull LGPlayer voted;

        public PuppeteerOrderAction(@Nonnull LGPlayer voter, @Nonnull LGPlayer voted) {
            this.voter = voter;
            this.voted = voted;
        }
    }
}
