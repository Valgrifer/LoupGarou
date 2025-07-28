package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RDogWolf extends Role {
    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(3, new Slot(ItemBuilder.make(Material.GOLDEN_APPLE)
                .setCustomId("ac_villager")
                .setDisplayName(DARK_GREEN + "Devenir Villageois")
                .setLore(GRAY + BOLD + "Vous n'aurez aucun pouvoir mais",
                    GRAY + BOLD + "resterez dans le camp du " + GREEN + BOLD + "Village" + GRAY + BOLD + ".")), (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RDogWolf)) return;

                RDogWolf role = (RDogWolf) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendActionBarMessage(GOLD + "Tu resteras fidèle au " + GREEN + BOLD + "Village" + GOLD + ".");
                lgp.sendMessage(GOLD + "Tu resteras fidèle au " + GREEN + BOLD + "Village" + GOLD + ".");
                lgp.hideView();
                role.callback.run();
            });

            setSlot(5, new Slot(ItemBuilder.make(Material.ROTTEN_FLESH)
                .setCustomId("ac_loupgarou")
                .setDisplayName(RED + "Devenir Loup-Garou")
                .setLore(RED + "Vous rejoindrez le camp des " + RED + BOLD + "Loups")), (holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RDogWolf)) return;

                RDogWolf role = (RDogWolf) lgp.getRole();

                role.closeInventory(lgp);

                lgp.sendActionBarMessage(GOLD + "Tu as changé de camp.");
                lgp.sendMessage(GOLD + "Tu as changé de camp.");

                //On le fait aussi rejoindre le camp des loups pour le tour pendant la nuit.
                role.getGame().getRole(RDogWolfWW.class, true).join(lgp, false);
                lgp.updateOwnSkin();

                lgp.hideView();
                role.callback.run();
            });
        }
    };
    private static final String roleChosenKey = "dogRoleChosen";

    private Runnable callback;
    private boolean inMenu = false;
    private LGPrivateInventoryHolder invHolder = null;

    public RDogWolf(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return GREEN + BOLD + "Chien-Loup";
    }

    public static String _getScoreBoardName() {
        return GREEN + BOLD + "Chien" + GRAY + "-" + RED + BOLD + "Loup";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Au début de la première nuit, tu peux choisir entre rester fidèle aux " +
            RoleType.VILLAGER.getColoredName(BOLD) + WHITE + " ou alors rejoindre le clan des " + RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE +
            ".";
    }

    public static String _getTask() {
        return "Souhaites-tu devenir un " + RED + BOLD + "Loup-Garou" + GOLD + " ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " pourrait trouver de nouveaux amis...";
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

    @Override
    public boolean hasPlayersLeft() {
        return getPlayers().stream().anyMatch(player -> !player.getCache().getBoolean(roleChosenKey));
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Choisis ton camp.", player);
        invHolder.setDefaultPreset(preset.clone(invHolder));
        player.getPlayer().closeInventory();
        player.getPlayer().openInventory(invHolder.getInventory());
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        if (player.getCache().getBoolean(roleChosenKey))
            return false;

        player.showView();
        this.callback = callback;
        player.getCache().set(roleChosenKey, true);
        openInventory(player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        closeInventory(player);
        player.hideView();
        //player.sendTitle(RED+"Vous n'infectez personne", DARK_RED+"Vous avez mis trop de temps à vous décider...", 80);
        player.sendActionBarMessage(GOLD + "Tu rejoins le " + GREEN + BOLD + "Village.");
        player.sendMessage(GOLD + "Tu rejoins le " + GREEN + BOLD + "Village.");
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        invHolder = null;
        player.getPlayer().closeInventory();
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
}
