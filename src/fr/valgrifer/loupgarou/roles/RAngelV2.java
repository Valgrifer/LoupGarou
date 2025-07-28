package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDayStartEvent;
import fr.valgrifer.loupgarou.events.LGNightPlayerPreKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RAngelV2 extends Role {

    public static final String TargetKey = "angel_target";
    public static final String LifeKey = "angel_life";
    public static final String SavedKey = "angel_saved";

    private static final MenuPreset preset = new MenuPreset(1) {
        @Override
        protected void preset() {
            setSlot(2, new MenuPreset.Slot(ItemBuilder.make(Material.TOTEM_OF_UNDYING)
                .setCustomId("ac_guardian")
                .setDisplayName(YELLOW + BOLD + "Ange Gardian")
                .setLore(DARK_GRAY + "Vous devrez protéger votre cible,", DARK_GRAY + "Vous disposerez de 2 protections",
                    DARK_GRAY + "Pour le garder en vie.")), ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RAngelV2)) return;

                RAngelV2 role = (RAngelV2) lgp.getRole();

                RAngelV2Guardian fallen = role.getGame().getRole(RAngelV2Guardian.class, true);
                lgp.setRole(fallen);
                fallen.join(lgp);

                role.closeInventory(lgp);
                lgp.sendActionBarMessage(BLUE + BOLD + "Tu devras garder ton protégé");
                lgp.sendMessage(GOLD + "Tu as choisi " + YELLOW + BOLD + "Ange Gardian" + GOLD + ".");
                lgp.hideView();
                role.callback.run();
            }));

            setSlot(4, new MenuPreset.Slot(ItemBuilder.make(Material.PLAYER_HEAD).setCustomId("ac_info").setDisplayName(BLUE + BOLD + "CIBLE")) {
                @Override
                public ItemBuilder getItem(LGInventoryHolder holder) {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return this.getDefaultItem();

                    LGPlayer target = ((LGPrivateInventoryHolder) holder).getPlayer().getCache().get(TargetKey);

                    return this.getDefaultItem().setSkull(target.getPlayer()).setDisplayName(BLUE + BOLD + target.getName());
                }
            });
            setSlot(6, new MenuPreset.Slot(ItemBuilder.make(Material.NETHERITE_SWORD)
                .setCustomId("ac_fallen")
                .setDisplayName(RED + BOLD + "Ange Déchu")
                .setLore(DARK_GRAY + "Vous devrez faire tuer votre cible,",
                    DARK_GRAY + "Par le vote du " + RoleType.VILLAGER.getColoredName(BOLD) + DARK_GRAY + " en étant le premier vote",
                    DARK_GRAY + "Pour gagner en fin de partie")), ((holder, event) -> {
                if (!(holder instanceof LGPrivateInventoryHolder)) return;

                LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                if (!(lgp.getRole() instanceof RAngelV2)) return;

                RAngelV2 role = (RAngelV2) lgp.getRole();

                RAngelV2Fallen fallen = role.getGame().getRole(RAngelV2Fallen.class, true);
                lgp.setRole(fallen);
                fallen.join(lgp);

                role.closeInventory(lgp);
                lgp.sendActionBarMessage(BLUE + BOLD + "Tu devras tué ta cible");
                lgp.sendMessage(GOLD + "Tu as choisi " + RED + BOLD + "Ange Déchu" + GOLD + ".");
                lgp.hideView();
                role.callback.run();
            }));
        }
    };
    private Runnable callback;
    private LGPrivateInventoryHolder invHolder;
    private boolean inMenu = false;

    public RAngelV2(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return LIGHT_PURPLE + BOLD + "Ange";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getName();
    }

    public static String _getScoreBoardName() {
        return _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes si tu remplis ton objectif";
    }

    public static String _getDescription() {
        return WHITE + "Tu es " + RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD) + WHITE +
            " et tu gagnes si tu remplis ton objectif en fonction de ce que tu as choisi. " + "Tu auras une cible à protéger en " + YELLOW + BOLD +
            "Ange Gardien" + WHITE + " ou à tuer en " + RED + BOLD + "Ange Déchu" + WHITE + ". " + "Vous aurez une vie supplémentaire contre les " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + " pour réussir votre mission. " + "En " + YELLOW + BOLD + "Ange Gardien" + WHITE +
            ", ton objectif est de protéger ta cible, pour cela tu pourras le protéger jusqu'à 2x durant la partie. " + "En " + RED + BOLD +
            "Ange Déchu" + WHITE + ", ton objectif est de tuer ta cible, pour cela tu devras être le premier à la voter durant le vote du " +
            RoleWinType.VILLAGE.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Choisis ton Role";
    }

    public static String _getBroadcastedTask() {
        return "L'" + _getName() + BLUE + " réfléchit.";
    }

    @Override
    public int getTimeout() {
        return 20;
    }

    @Override
    public boolean hasPlayersLeft() {
        return getGame().getNight() == 1;
    }

    @Override
    public void join(LGPlayer player) {
        super.join(player);
        player.getCache().set(LifeKey, 1);
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
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        Optional<LGPlayer> target = getGame().getAlive(lgPlayer -> lgPlayer != player && lgPlayer.getRoleType() == RoleType.VILLAGER)
            .stream()
            .sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
            .findAny();

        if (!target.isPresent()) {
            player.sendMessage(DARK_RED + "Une erreur s'est produite, vous resterez Ange sans pouvoir gagner");
            callback.run();
            return false;
        }

        target.get().sendMessage(AQUA + "Attention à vous, vous êtes la cible de l'ange, il peut être agressif comme protecteur.");

        player.showView();
        this.callback = callback;
        player.getCache().set(TargetKey, target.get());
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

        if (!e.isCancelled() && (e.getReason() == LGPlayerKilledEvent.Reason.LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.LOUP_BLANC ||
            e.getReason() == LGPlayerKilledEvent.Reason.GM_LOUP_GAROU || e.getReason() == LGPlayerKilledEvent.Reason.ASSASSIN) &&
            e.getKilled().getCache().get(LifeKey, 0) > 0 && e.getKilled().isRoleActive()) {
            e.setCancelled(true);
            e.getKilled().getCache().set(LifeKey, e.getKilled().getCache().<Integer> get(LifeKey) - 1);
            e.getKilled().getCache().set(SavedKey, true);
        }
    }

    @EventHandler

    public void onDay(LGDayStartEvent e) {
        if (e.getGame() != getGame()) return;

        getGame().getAlive(lgPlayer -> lgPlayer.getCache().getBoolean(SavedKey)).forEach(lgPlayer -> {
            lgPlayer.getCache().remove(SavedKey);
            lgPlayer.sendMessage(BLUE + "Votre vie a été consumée cette nuit, vous devrez être plus vigilant à partir de maintenant.");
        });
    }

    @EventHandler
    public void onTarget(LGRoleActionEvent e) {
        if (e.getGame() != getGame()) return;
        if (e.isAction(RPyromaniac.GasoilAction.class)) {
            RPyromaniac.GasoilAction action = (RPyromaniac.GasoilAction) e.getAction();
            if ((action.getTarget().getRole() instanceof RAngelV2 || action.getTarget().getRole() instanceof RAngelV2Guardian ||
                action.getTarget().getRole() instanceof RAngelV2Fallen) && action.getTarget().isRoleActive()) action.setCancelled(true);
        }
        else if (e.isAction(RVampire.VampiredAction.class)) {
            RVampire.VampiredAction action = (RVampire.VampiredAction) e.getAction();
            if ((action.getTarget().getRole() instanceof RAngelV2 || action.getTarget().getRole() instanceof RAngelV2Guardian ||
                action.getTarget().getRole() instanceof RAngelV2Fallen) && action.getTarget().isRoleActive()) action.setImmuned(true);
        }
    }
}
