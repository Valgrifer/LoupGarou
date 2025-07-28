package fr.valgrifer.loupgarou.roles;

import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RPirate extends Role {
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

                if (!(lgp.getRole() instanceof RPirate)) return;

                RPirate role = (RPirate) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(GOLD + "Tu n'as rien fait cette nuit.");
                lgp.hideView();
                role.callback.run();
            }));

            setSlot(5, new MenuPreset.Slot(ItemBuilder.make(Material.ROTTEN_FLESH)
                    .setCustomId("ac_takeotage")
                    .setDisplayName(GOLD + BOLD + "Prendre un otage")
                    .setLore(DARK_GRAY + "Tu peux prendre un joueur en otage", DARK_GRAY + "Si tu meurs du vote, il mourra à ta place.")),
                ((holder, event) -> {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return;

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();
                    Player player = lgp.getPlayer();

                    if (!(lgp.getRole() instanceof RPirate)) return;

                    RPirate role = (RPirate) lgp.getRole();

                    role.closeInventory(lgp);
                    player.getInventory().setItem(8, itemNoAction.build());
                    player.updateInventory();
                    //Pour éviter les missclick
                    WrapperPlayServerHeldItemSlot held = new WrapperPlayServerHeldItemSlot();
                    held.setSlot(0);
                    held.sendPacket(player);

                    lgp.sendMessage(GOLD + "Choisissez votre otage.");
                    lgp.choose(choosen -> {
                        if (choosen != null) {
                            player.getInventory().setItem(8, null);
                            player.updateInventory();
                            lgp.stopChoosing();
                            lgp.sendMessage(GOLD + "Tu as pris " + GRAY + BOLD + choosen.getName() + GOLD + " en otage.");
                            lgp.sendActionBarMessage(GRAY + BOLD + choosen.getName() + GOLD + " est ton otage");
                            lgp.getCache().set("pirate_otage", choosen);
                            choosen.getCache().set("pirate_otage_d", lgp);
                            choosen.addEndGameReaveal(GREEN + "Otage");
                            role.getPlayers().remove(lgp);//Pour éviter qu'il puisse prendre plusieurs otages
                            choosen.sendMessage(GRAY + BOLD + lgp.getName() + GOLD + " t'a pris en otage, il est " + role.getName() + GOLD + ".");
                            lgp.hideView();
                            role.callback.run();
                        }
                    }, lgp);
                }));
        }
    };
    Runnable callback;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

    public RPirate(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return GREEN + BOLD + "Pirate";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Une fois dans la partie, tu peux prendre en otage un autre joueur. Si tu es désigné à l'issue du vote de jour, ton " + BOLD + "otage" +
            WHITE + " mourra à ta place et ton rôle sera dévoilé au reste du village";
    }

    public static String _getTask() {
        return "Veux-tu prendre quelqu'un en otage ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " aiguise son crochet...";
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Veux-tu prendre un otage ?", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKilled(LGPlayerKilledEvent e) {
        if (e.getGame() == getGame() && e.getReason() == Reason.VOTE)
            if (e.getKilled().getCache().has("pirate_otage") && e.getKilled().isRoleActive()) {
                LGPlayer otage = e.getKilled().getCache().remove("pirate_otage");
                if (!otage.isDead() && otage.getCache().get("pirate_otage_d") == e.getKilled()) {
                    getGame().broadcastMessage(
                        GRAY + BOLD + e.getKilled().getName() + GOLD + " est " + getName() + GOLD + ", c'est son otage qui va mourir.", true);
                    e.setKilled(otage);
                    e.setReason(Reason.PIRATE);
                }
            }
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
        lgp.hideView();
        callback.run();
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
}
