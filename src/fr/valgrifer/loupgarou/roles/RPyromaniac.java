package fr.valgrifer.loupgarou.roles;

import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPyromaniac extends Role {
    private static final ItemBuilder itemNoAction = ResourcePack.getItem("ui_cancel")
        .setCustomId("ac_no")
        .setDisplayName(GRAY + BOLD + "Ne rien faire")
        .setLore(DARK_GRAY + "Passez votre tour");

    private static final MenuPreset preset = new MenuPreset(2) {
        @Override
        protected void preset() {
            setSlot(2, new MenuPreset.Slot(itemNoAction), ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RPyromaniac)) return;

                RPyromaniac role = (RPyromaniac) lgp.getRole();

                role.onNightTurnTimeout(lgp);
                role.callback.run();
            }));

            setSlot(4, new MenuPreset.Slot(ItemBuilder.make(Material.FLINT_AND_STEEL)
                .setCustomId("ac_fire")
                .setDisplayName(YELLOW + BOLD + "Mettre le feu")
                .setLore(DARK_GRAY + "Tuez les joueurs que vous avez", DARK_GRAY + "Précédemment recouvert de gasoil.")) {
                @Override
                public ItemBuilder getItem(LGInventoryHolder holder) {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return MenuPreset.lockSlot.getDefaultItem();

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    if (lgp.getCache().<List<LGPlayer>> get("pyromane_essence").size() == 0) return MenuPreset.lockSlot.getDefaultItem();
                    return getDefaultItem();
                }
            }, ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RPyromaniac)) return;

                RPyromaniac role = (RPyromaniac) lgp.getRole();

                role.closeInventory(lgp);
                if (lgp.getCache().<List<LGPlayer>> get("pyromane_essence").size() != 0) {
                    List<LGPlayer> liste = lgp.getCache().get("pyromane_essence");
                    for (LGPlayer scndPlayer : liste) {
                        if (!scndPlayer.isDead() && scndPlayer.getPlayer() != null) {
                            role.getGame().kill(scndPlayer, Reason.PYROMANE);
                        }
                    }
                    liste.clear();
                    lgp.sendMessage(GOLD + BOLD + "Tu as décidé de brûler tes victimes ce soir.");
                    lgp.sendActionBarMessage(GOLD + "Tes victimes brûleront ce soir.");
                }
                else lgp.sendMessage(GOLD + BOLD + "Personne n'a pris feu.");
                lgp.hideView();
                role.callback.run();
            }));

            setSlot(6, new MenuPreset.Slot(ItemBuilder.make(Material.LAVA_BUCKET)
                .setCustomId("ac_gasoil")
                .setDisplayName(RED + BOLD + "Recouvrir d'essence")
                .setLore(DARK_GRAY + "Recouvres jusqu'à deux joueurs d'essence")), ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();
                Player player = lgp.getPlayer();

                if (!(lgp.getRole() instanceof RPyromaniac)) return;

                RPyromaniac role = (RPyromaniac) lgp.getRole();

                role.closeInventory(lgp);

                //Pour éviter les missclick
                WrapperPlayServerHeldItemSlot held = new WrapperPlayServerHeldItemSlot();
                held.setSlot(0);
                held.sendPacket(player);

                player.getInventory().setItem(8, itemNoAction.build());
                player.updateInventory();

                lgp.sendMessage(GOLD + "Choisis deux joueurs à recouvrir de gasoil.");
                lgp.choose(choosen -> {
                    if (choosen != null) {
                        if (choosen == role.first) {
                            lgp.sendMessage(RED + "Tu as déjà versé du gasoil sur " + GRAY + BOLD + choosen.getName() + GOLD + ".");
                            return;
                        }
                        List<LGPlayer> gasoilList = lgp.getCache().get("pyromane_essence");
                        if (gasoilList.contains(choosen)) {
                            lgp.sendMessage(GRAY + BOLD + choosen.getName() + RED + " est déjà recouvert de gasoil.");
                            return;
                        }

                        lgp.sendMessage(GOLD + "Tu as versé du gasoil sur " + GRAY + BOLD + choosen.getName() + GOLD + ".");
                        lgp.sendActionBarMessage(GOLD + GRAY + BOLD + choosen.getName() + GOLD + " est recouvert de gasoil");
                        if (role.first != null || role.getGame().getAlive().size() == 2) {
                            lgp.hideView();
                            lgp.stopChoosing();
                            LGPlayer[] targets = new LGPlayer[]{ role.first, choosen };
                            Arrays.stream(targets).filter(Objects::nonNull).forEach(target -> role.gasoil(lgp, target));
                            player.getInventory().setItem(8, null);
                            player.updateInventory();
                            role.callback.run();
                        }
                        else {
                            lgp.sendMessage(GOLD + "Choisis un deuxième joueur à recouvrir de gasoil.");
                            role.first = choosen;
                        }
                    }
                }, lgp);
            }));
        }
    };
    Runnable callback;
    LGPlayer first;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

    public RPyromaniac(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return GOLD + BOLD + "Pyromane";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes " + RoleWinType.SOLO.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu peux recouvrir de gasoil deux joueurs au choix, ou immoler tous ceux que tu as précédemment visités. Les joueurs sauront qu'ils ont été recouverts de gasoil.";
    }

    public static String _getTask() {
        return "Que veux-tu faire cette nuit ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " joue avec une allumette...";
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;

        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Que veux-tu faire ?", player);
        if (!player.getCache().has("pyromane_essence")) player.getCache().set("pyromane_essence", new ArrayList<>());
        invHolder.setDefaultPreset(preset.clone(invHolder));

        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        first = null;
        player.showView();
        this.callback = callback;
        openInventory(player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        if (first != null) gasoil(player, first);
        player.getPlayer().getInventory().setItem(8, null);
        player.stopChoosing();
        closeInventory(player);
        player.getPlayer().updateInventory();
        player.hideView();
        player.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
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
        openInventory(lgp);
    }

    @EventHandler
    public void onKilled(LGPlayerKilledEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getPlayers())
            if (lgp.getCache().has("pyromane_essence")) {
                List<LGPlayer> liste = lgp.getCache().get("pyromane_essence");
                //Au cas où le mec soit rez
                liste.remove(e.getKilled());
            }
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

    //Win condition

    @EventHandler
    public void onEndgameCheck(LGEndCheckEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.SOLO && getPlayers().size() > 0) e.setWinType(LGWinType.PYROMANE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndGame(LGGameEndEvent e) {
        if (e.getWinType() == LGWinType.PYROMANE) {
            e.getWinners().clear();
            e.getWinners().addAll(getPlayers());
        }
    }

    public void gasoil(LGPlayer from, LGPlayer target) {
        List<LGPlayer> gasoilList = from.getCache().get("pyromane_essence");
        LGRoleActionEvent e = new LGRoleActionEvent(getGame(), new GasoilAction(target), from);
        Bukkit.getPluginManager().callEvent(e);
        GasoilAction action = (GasoilAction) e.getAction();
        if (!action.isCancelled() || action.isForceMessage()) action.getTarget().sendMessage(GOLD + "Tu es recouvert de gasoil...");
        else from.sendMessage(GRAY + BOLD + action.getTarget().getName() + RED + " est immunisée.");

        if (!action.isCancelled()) gasoilList.add(action.getTarget());
    }

    public static class GasoilAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, MessageForced
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
        public GasoilAction(LGPlayer target) {
            this.target = target;
        }
    }
}
