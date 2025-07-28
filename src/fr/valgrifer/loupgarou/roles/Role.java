package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@Getter
@SuppressWarnings("unused")
public abstract class Role implements Listener {
    private static final Map<Class<? extends Role>, ItemBuilder> cards = new HashMap<>();
    private final ArrayList<LGPlayer> players = new ArrayList<>();
    private final LGGame game;
    @Setter
    private int waitedPlayers;
    @Setter
    private boolean fakeTimer = false;
    @Setter
    private boolean hidden = false;

    public Role(LGGame game) {
        this.game = game;
        Bukkit.getPluginManager().registerEvents(this, MainLg.getInstance());
        FileConfiguration config = MainLg.getInstance().getConfig();
        String roleConfigName = "role." + getId();
        if (config.contains(roleConfigName)) waitedPlayers = config.getInt(roleConfigName);
    }

    public static void setCard(Class<? extends Role> role, ItemBuilder card) {
        if (!cards.containsKey(role)) cards.put(role, card.clone());
    }

    public static ItemBuilder getCard(Class<? extends Role> role) {
        if (!cards.containsKey(role)) return null;
        return cards.get(role).clone();
    }

    public static String getId(Class<? extends Role> clazz) {
        return clazz.getSimpleName().substring(1).toLowerCase();
    }

    protected static Object getStatic(Class<? extends Role> clazz, String fn) {
        return getStatic(clazz, fn, true);
    }

    @SneakyThrows
    protected static Object getStatic(Class<? extends Role> clazz, String fn, boolean isCatch) {
        Method method = null;
        Class<? extends Role> clazzRole = clazz;
        while (method == null && clazzRole != Role.class) {
            try {
                method = clazzRole.getDeclaredMethod(fn);
            } catch (NoSuchMethodException ignored) {
                clazzRole = (Class<? extends Role>) clazzRole.getSuperclass();
            }
        }
        try {
            return method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            if (isCatch) return fn + "() is not defined (in static) in " + clazz.getSimpleName();
            else throw e;
        }
    }

    @SneakyThrows
    public static <T extends Role> T makeNew(Class<T> r, LGGame game) {
        return r.getConstructor(LGGame.class).newInstance(game);
    }

    public static String getName(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getName");
    }

    public static String getFriendlyName(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getFriendlyName");
    }

    public static String getScoreBoardName(Class<? extends Role> clazz) {
        try {
            return (String) getStatic(clazz, "_getScoreBoardName", false);
        } catch (Exception ignored) {
            return getName(clazz);
        }
    }

    @SneakyThrows
    public static boolean hasScoreBoardName(Class<? extends Role> clazz) {
        try {
            getStatic(clazz, "_getScoreBoardName", false);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String getShortDescription(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getShortDescription");
    }

    public static String getDescription(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getDescription");
    }

    public static String getTask(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getTask");
    }

    public static String getBroadcastedTask(Class<? extends Role> clazz) {
        return (String) getStatic(clazz, "_getBroadcastedTask");
    }

    public static RoleType getType(Class<? extends Role> clazz) {
        try {
            return (RoleType) getStatic(clazz, "_getType", false);
        } catch (Exception ignored) { }
        return RoleType.NEUTRAL;
    }

    public static RoleWinType getWinType(Class<? extends Role> clazz) {
        try {
            return (RoleWinType) getStatic(clazz, "_getWinType", false);
        } catch (Exception ignored) { }
        return RoleWinType.NONE;
    }

    public String getId() {
        return getId(getClass());
    }

    public String getName() {
        return getName(this.getClass());
    }

    public String getPublicName(LGPlayer lgp) {
        return getScoreBoardName();
    }

    public String getFriendlyName() {
        return getFriendlyName(this.getClass());
    }

    public String getScoreBoardName() {
        return getScoreBoardName(this.getClass());
    }

    public boolean hasScoreBoardName() {
        return hasScoreBoardName(this.getClass());
    }

    public String getShortDescription() {
        return getShortDescription(this.getClass());
    }

    public String getDescription() {
        return getDescription(this.getClass());
    }

    public String getTask() {
        return getTask(this.getClass());
    }

    public String getBroadcastedTask() {
        return getBroadcastedTask(this.getClass());
    }

    public RoleType getType() {
        return getType(this.getClass());
    }

    public RoleWinType getWinType() {
        return getWinType(this.getClass());
    }

    /**
     * @return Timeout in second for this role
     */
    public int getTimeout() {
        return -1;
    }

    public void onNightTurn(Runnable callback) {
        ArrayList<LGPlayer> players = (ArrayList<LGPlayer>) getPlayers().clone();
        new Runnable() {
            @Override
            public void run() {
                getGame().cancelWait();
                if (players.isEmpty()) {
                    onTurnFinish(callback);
                    return;
                }
                LGPlayer player = players.remove(0);

                boolean autoTimeout = !player.isRoleActive() || Role.this.isFakeTimer() || !onNightTurn(player, this);
                getGame().wait(getTimeout(), () -> {
                    if (autoTimeout)
                        return;

                    try {
                        Role.this.onNightTurnTimeout(player);
                    } catch (Exception err) {
                        MainLg.getInstance().getLogger().warning("Error when timeout role");
                        err.printStackTrace();
                    }
                    this.run();
                }, (currentPlayer, secondsLeft) ->
                           currentPlayer == player ?
                                   BLUE + BOLD + "C'est à ton tour !" :
                                   GOLD + "C'est au tour " + getFriendlyName() + " " + GOLD + "(" + YELLOW + secondsLeft + " s" + GOLD + ")");
                player.sendMessage(GOLD + getTask());
                //	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 100);

                if (autoTimeout) {
                    Runnable run = this;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            run.run();
                        }
                    }.runTaskLater(MainLg.getInstance(), 20L * (ThreadLocalRandom.current().nextInt(getTimeout() / 3 * 2 - 4) + 4));
                }
            }
        }.run();
    }

    public void join(LGPlayer player, boolean sendMessage) {
        getGame().logMessage(player.getName() + " est " + getName());
        players.add(player);
        if (player.getRole() == null) player.setRole(this);
        waitedPlayers--;
        if (sendMessage) {
            player.sendTitle(GOLD + "Tu es " + getName(), YELLOW + getShortDescription(), 200);
            player.sendMessage(GOLD + "Tu es " + getName() + GOLD + ".");
            player.sendMessage(GOLD + "Description : " + getDescription());
        }
    }

    public void join(LGPlayer player) {
        join(player, !getGame().isStarted());
        LGCardItems.updateItem(player);
    }

    public boolean hasPlayersLeft() {
        return !getPlayers().isEmpty();
    }

    protected void onNightTurnTimeout(LGPlayer player) { }

    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        return false;
    }

    protected void onTurnFinish(Runnable callback) {
        callback.run();
    }

    public int getTurnOrder() {
        try {
            return RoleSort.indexOfRoleSort(getClass().getSimpleName().substring(1));
        } catch (Throwable e) {
            return -1;
        }
    }//En combientième ce rôle doit être appellé
}
