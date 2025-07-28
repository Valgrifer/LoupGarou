package fr.valgrifer.loupgarou.roles;

import com.comphenix.packetwrapper.WrapperPlayServerHeldItemSlot;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.events.LGDayEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.events.LGVoteRequestedEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGPrivateInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RDictator extends Role {
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

                if (!(lgp.getRole() instanceof RDictator)) return;

                RDictator role = (RDictator) lgp.getRole();

                role.closeInventory(lgp);
                lgp.sendMessage(RED + "Vous ne faites pas votre coup d'état.");
//                            lgp.sendMessage(GRAY+ITALIC+"Vous aurez de nouveau le choix lors de la prochaine nuit.");
                lgp.hideView();
                role.callback.run();
            }));
            setSlot(5, new MenuPreset.Slot(ItemBuilder.make(Material.GUNPOWDER)
                    .setCustomId("ac_yes")
                    .setDisplayName(YELLOW + BOLD + "Coup d'État")
                    .setLore(DARK_GRAY + "Prends le contrôle du village", DARK_GRAY + "et choisis seul qui mourra demain.", "",
                        DARK_GRAY + "Si tu tues un " + GREEN + BOLD + "Villageois" + DARK_GRAY + ", tu", DARK_GRAY + "l'auras sur la conscience.")),
                ((holder, event) -> {
                    if (!(holder instanceof LGPrivateInventoryHolder)) return;

                    LGPlayer lgp = ((LGPrivateInventoryHolder) holder).getPlayer();

                    if (!(lgp.getRole() instanceof RDictator)) return;

                    RDictator role = (RDictator) lgp.getRole();

                    role.closeInventory(lgp);
                    lgp.sendActionBarMessage(BLUE + BOLD + "Tu effectueras un coup d'état");
                    lgp.sendMessage(GOLD + "Tu as décidé de faire un coup d'état.");
                    lgp.getCache().set("coup_d_etat", true);
                    lgp.getCache().set("just_coup_d_etat", true);
                    lgp.hideView();
                    role.callback.run();
                }));
        }
    };
    Runnable callback, run;
    private LGPrivateInventoryHolder invHolder;
    private boolean inMenu = false;

    public RDictator(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Dictateur";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Une fois dans la partie, tu peux choisir d'effectuer un " + YELLOW + ITALIC + BOLD +
            "coup d'état" + WHITE + ", tu choisiras alors seul qui mourra au jour suivant. Si tu fais le bon choix, tu deviendras " + DARK_PURPLE +
            BOLD + "Capitaine" + WHITE + " mais si tu tues un " + RoleType.VILLAGER.getColoredName(BOLD) + WHITE +
            ", tu te suicideras la nuit qui suit.";
    }

    public static String _getTask() {
        return "Veux-tu réaliser un coup d'état ?";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " décide s'il veut se dévoiler.";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    public void openInventory(LGPlayer player) {
        inMenu = true;
        invHolder = new LGPrivateInventoryHolder(1, BLACK + "Veux-tu faire un coup d'état ?", player);
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
        player.hideView();
        closeInventory(player);
    }

    private void closeInventory(LGPlayer player) {
        inMenu = false;
        player.getPlayer().closeInventory();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        LGPlayer lgp = LGPlayer.get(player);

        if (lgp.getRole() != this || !ItemBuilder.checkId(e.getItem(), itemNoAction.getCustomId())) return;

        e.setCancelled(true);

        getGame().cancelWait();
        lgp.stopChoosing();
        player.getInventory().setItem(8, null);
        player.updateInventory();
        getGame().broadcastMessage(GRAY + BOLD + lgp.getName() + BLUE + " n'a tué personne.", true);
        run.run();
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
    public void onVote(LGVoteRequestedEvent e) {
        if (e.getGame() != getGame()) return;

        if (e.getCause() != LGVoteCause.VILLAGE && e.getCause() != LGVoteCause.MAYOR) return;

        for (LGPlayer lgp : getPlayers())
            if (lgp.getCache().getBoolean("just_coup_d_etat") && lgp.isRoleActive()) {
                e.setCancelled(true);
                e.setContinuePeopleVote(false);
            }

        if (!e.isCancelled()) return;

        Iterator<LGPlayer> ite = ((ArrayList<LGPlayer>) getPlayers().clone()).iterator();
        new Runnable() {
            public void run() {
                run = this;
                if (ite.hasNext()) {
                    LGPlayer lgp = ite.next();
                    if (lgp.getCache().getBoolean("just_coup_d_etat")) {
                        getPlayers().remove(lgp);
                        lgp.getCache().remove("just_coup_d_etat");
                        getGame().broadcastMessage(GRAY + BOLD + lgp.getName() + " " + BLUE + "réalise un coup d'état.", true);
                        //lgp.sendTitle(GOLD+"Vous faites votre coup d'état", GREEN+"Choisissez qui tuer", 60);

                        //On le met sur le slot 0 pour éviter un missclick sur la croix
                        WrapperPlayServerHeldItemSlot hold = new WrapperPlayServerHeldItemSlot();
                        hold.setSlot(0);
                        hold.sendPacket(lgp.getPlayer());

                        lgp.sendMessage(GOLD + "Choisis un joueur à exécuter.");
                        getGame().wait(60, () -> {
                            lgp.stopChoosing();
                            getGame().broadcastMessage(GRAY + BOLD + lgp.getName() + BLUE + " n'a tué personne.", true);
                            lgp.getPlayer().getInventory().setItem(8, null);
                            lgp.getPlayer().updateInventory();
                            this.run();
                        }, (player, secondsLeft) ->
                            lgp == player ?
                                BLUE + BOLD + "C'est à ton tour !" :
                                GOLD + "Le Dictateur choisit sa victime (" + YELLOW + secondsLeft + " s" + GOLD + ")");
                        lgp.choose((choosen) -> {
                            if (choosen != null) {
                                getGame().cancelWait();
                                lgp.stopChoosing();
                                lgp.getPlayer().getInventory().setItem(8, null);
                                lgp.getPlayer().updateInventory();
                                kill(choosen, lgp, this);
                            }
                        });
                        lgp.getPlayer().getInventory().setItem(8, itemNoAction.build());
                        lgp.getPlayer().updateInventory();
                    }
                }
                else getGame().nextNight();
            }
        }.run();
    }

    protected void kill(LGPlayer choosen, LGPlayer dicta, Runnable callback) {
        RoleType roleType = choosen.getRoleType();

        LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), choosen, Reason.DICTATOR);
        Bukkit.getPluginManager().callEvent(killEvent);

        if (killEvent.isCancelled()) return;

        if (getGame().kill(killEvent.getKilled(), killEvent.getReason(), true)) return;

        if (roleType != RoleType.VILLAGER) {
            getGame().broadcastMessage(
                GRAY + BOLD + dicta.getName() + " " + BLUE + "devient le " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + " du village.", true);
            getGame().setMayor(dicta);
        }
        else {
            getGame().kill(dicta, Reason.DICTATOR_SUICIDE);
            for (LGPlayer lgp : getGame().getInGame()) {
                if (lgp == dicta) lgp.sendMessage(BLUE + ITALIC + "Ça ne s'est pas passé comme prévu...");
                else lgp.sendMessage(BLUE + "Le " + getName() + BLUE + " s'est trompé, il mourra la nuit suivante.");
            }
        }
        callback.run();
    }

    @EventHandler
    public void onNight(LGDayEndEvent e) {
        if (e.getGame() == getGame()) {
            LGPlayer lgp = getGame().getDeaths().get(Reason.DICTATOR_SUICIDE);
            if (lgp != null) lgp.sendMessage(DARK_GRAY + ITALIC + "Des pensées sombres hantent ton esprit...");
        }
    }
}
