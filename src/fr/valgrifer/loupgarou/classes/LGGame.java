package fr.valgrifer.loupgarou.classes;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGCardItems.Constraint;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.classes.chat.LGNoChat;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.roles.Role;
import fr.valgrifer.loupgarou.roles.RoleType;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import fr.valgrifer.loupgarou.scoreboard.CustomScoreboard;
import fr.valgrifer.loupgarou.utils.MultipleValueMap;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("ALL")
public class LGGame implements Listener {
    private static final boolean autoStart = false;


    @Getter
    private final SecureRandom random = new SecureRandom();
    @Getter
    private final int maxPlayers;
    @Getter
    private final ArrayList<LGPlayer> inGame = new ArrayList<>();
    @Getter
    private final ArrayList<Role> roles = new ArrayList<>();
    @Getter
    private final LGSpot spots = new LGSpot();
    private final Map<LGChatType, LGChat> chat = new WeakHashMap<>();
    @Getter
    private final MultipleValueMap<LGPlayerKilledEvent.Reason, LGPlayer> deaths = new MultipleValueMap<>();
    @Getter
    public long time = 0;
    @Setter
    boolean ended;
    boolean isPeopleVote = false;
    @Getter
    private boolean started;
    @Getter
    private int night = 0;
    private BukkitTask startingTask;
    @Getter
    @Setter
    private int waitTicks;
    @Getter
    private boolean day;
    private BukkitTask waitTask;
    @Getter
    private LGPlayer mayor;
    @Getter
    private LGVote vote;

    public LGGame(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        Bukkit.getPluginManager().registerEvents(this, MainLg.getInstance());

        LGChat spectatorChat = new LGChat(this, LGChatType.SPEC) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                if (!context.getChat().equals(this))
                    return context.getChat().receive(context, message);

                return GRAY + context.getInterlocutor().getName() + " " + GOLD + "» " + WHITE + message;
            }
        };
        LGChat dayChat = new LGChat(this, LGChatType.VILLAGE) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                return YELLOW + context.getInterlocutor().getName() + " " + GOLD + "» " + WHITE + message;
            }
        };

        new LGNoChat(this);

        dayChat.join(spectatorChat);
    }

    public void registerChat(@Nonnull LGChat chat) {
        if (this.chat.containsKey(chat.getType()))
            throw new IllegalArgumentException("Chat already registered");
        this.chat.put(chat.getType(), chat);
    }

    public LGChat getChat(@Nonnull LGChatType type) {
        return chat.get(type);
    }

    public Collection<LGChat> getChats() {
        return chat.values();
    }

    public void sendActionBarMessage(String msg) {
        WrapperPlayServerChat chat = new WrapperPlayServerChat();
        chat.setPosition((byte) 2);
        chat.setMessage(WrappedChatComponent.fromText(msg));
        for (LGPlayer lgp : inGame)
            chat.sendPacket(lgp.getPlayer());
    }

    public void broadcastMessage(String msg) {
        broadcastMessage(msg, false);
    }

    public void broadcastMessage(String msg, boolean log) {
        for (LGPlayer lgp : inGame)
            lgp.sendMessage(msg);
        if (log) logMessage(msg);
    }

    public void logMessage(String msg) {
        MainLg.getInstance().getLogger().info(msg.replaceAll("§[0-9a-fk-r]", ""));
    }

    public void broadcastSpacer() {
        for (LGPlayer lgp : inGame)
            lgp.getPlayer().sendMessage("\n");
    }

    public void wait(int seconds, Runnable callback) {
        wait(seconds, callback, null);
    }

    public void wait(int seconds, Runnable callback, TextGenerator generator) {
        cancelWait();
        waitTicks = seconds * 20;
        waitTask = new BukkitRunnable() {
            @Override
            public void run() {
                WrapperPlayServerExperience exp = new WrapperPlayServerExperience();
                exp.setLevel((short) (Math.floorDiv(waitTicks, 20) + 1));
                exp.setExperienceBar((float) waitTicks / (seconds * 20F));
                for (LGPlayer player : getInGame()) {
                    exp.sendPacket(player.getPlayer());
                    if (generator != null) player.sendActionBarMessage(generator.generate(player, Math.floorDiv(waitTicks, 20) + 1));
                }
                if (waitTicks == 0) {
                    for (LGPlayer player : getInGame())
                        player.sendActionBarMessage("");
                    waitTask = null;
                    cancel();
                    callback.run();
                }
                waitTicks--;
            }
        }.runTaskTimer(MainLg.getInstance(), 0, 1);
    }

    public void wait(int seconds, int initialSeconds, Runnable callback, TextGenerator generator) {
        cancelWait();
        waitTicks = seconds * 20;
        waitTask = new BukkitRunnable() {
            @Override
            public void run() {
                WrapperPlayServerExperience exp = new WrapperPlayServerExperience();
                exp.setLevel((short) (Math.floorDiv(waitTicks, 20) + 1));
                exp.setExperienceBar((float) waitTicks / (initialSeconds * 20F));
                for (LGPlayer player : getInGame()) {
                    exp.sendPacket(player.getPlayer());
                    if (generator != null) player.sendActionBarMessage(generator.generate(player, Math.floorDiv(waitTicks, 20) + 1));
                }
                if (waitTicks == 0) {
                    for (LGPlayer player : getInGame())
                        player.sendActionBarMessage("");
                    waitTask = null;
                    cancel();
                    callback.run();
                }
                waitTicks--;
            }
        }.runTaskTimer(MainLg.getInstance(), 0, 1);
    }

    public void cancelWait() {
        if (waitTask != null) {
            waitTask.cancel();
            waitTask = null;
        }
    }

    public void kill(LGPlayer player, Reason reason) {
        if (!deaths.containsValue(player) && !player.isDead()) {
            LGNightPlayerPreKilledEvent event = new LGNightPlayerPreKilledEvent(this, player, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) deaths.put(event.getReason(), player);
        }
    }

    public boolean tryToJoin(LGPlayer lgp) {
        if (ended) return false;
        if (!started && inGame.size() < maxPlayers) {//Si la partie n'a pas démarrée et qu'il reste de la place
            lgp.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            VariousUtils.setWarning(lgp.getPlayer(), false);
            if (lgp.isHidden()) lgp.resetHidden();

            Player player = lgp.getPlayer();

            // Clear votes

            WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
            destroy.setEntityIds(new int[]{ Integer.MIN_VALUE + player.getEntityId() });
            int[] ids = new int[getInGame().size() + 1];
            for (int i = 0; i < getInGame().size(); i++) {
                Player l = getInGame().get(i).getPlayer();
                if (l == null) continue;
                ids[i] = Integer.MIN_VALUE + l.getEntityId();
                destroy.sendPacket(l);
            }

            ids[ids.length - 1] = -player.getEntityId();// Clear voting

            destroy = new WrapperPlayServerEntityDestroy();
            destroy.setEntityIds(ids);
            destroy.sendPacket(player);

            // End clear votes/voting

            player.getInventory().clear();
            player.updateInventory();
            player.closeInventory();

            lgp.joinChat(this.getChat(LGChatType.VILLAGE));

            lgp.setGame(this);
            inGame.add(lgp);

            lgp.setScoreboard(null);

            for (LGPlayer other : getInGame()) {
                other.updatePrefix();
                if (lgp != other) {
                    player.hidePlayer(MainLg.getInstance(), other.getPlayer());
                    player.showPlayer(MainLg.getInstance(), other.getPlayer());

                    other.getPlayer().hidePlayer(MainLg.getInstance(), player);
                    other.getPlayer().showPlayer(MainLg.getInstance(), player);
                }
            }

            player.setGameMode(GameMode.ADVENTURE);
            broadcastMessage(
                GRAY + "Le joueur " + DARK_GRAY + lgp.getName() + GRAY + " a rejoint la partie " + BLUE + "(" + DARK_GRAY + inGame.size() + GRAY +
                    "/" + DARK_GRAY + maxPlayers + BLUE + ")", true);

            Bukkit.getPluginManager().callEvent(new LGGameJoinEvent(this, lgp));
            //AutoStart
            if (autoStart) updateStart();
            return true;
        }
        return false;
    }

    public void checkLeave() {
        if (startingTask != null) {
            startingTask.cancel();
            startingTask = null;
            broadcastMessage(RED + ITALIC + "Un joueur s'est déconnecté. Le décompte de lancement a donc été arrêté.", true);
        }
    }

    public void updateStart() {
        if (!isStarted()) if (inGame.size() ==
            maxPlayers) {//Il faut que la partie soit totalement remplie pour qu'elle démarre car sinon, tous les rôles ne seraient pas distribués
            for (LGPlayer lgp : getInGame()) {
                CustomScoreboard scoreboard = new CustomScoreboard(GRAY + "", lgp);
                scoreboard.getLine(0).setDisplayName(GOLD + "La partie va démarrer...");
                lgp.setScoreboard(scoreboard);
            }
            if (startingTask == null) {
                startingTask = new BukkitRunnable() {
                    int timeLeft = 5 + 1;

                    @Override
                    public void run() {
                        if (--timeLeft == 0)//start
                            start();
                        else sendActionBarMessage(GOLD + "Démarrage dans " + YELLOW + timeLeft + GOLD + "...");
                    }
                }.runTaskTimer(MainLg.getInstance(), 20, 20);
            }
        }
        else if (startingTask != null) {
            startingTask.cancel();
            broadcastMessage(RED + ITALIC + "Le démarrage de la partie a été annulé car une personne l'a quittée !", true);
        }
    }

    public void start() {
        if (startingTask != null) {
            startingTask.cancel();
            startingTask = null;
        }
        started = true;

        Bukkit.getPluginManager().callEvent(new LGGameStartEvent(this));

        MainLg main = MainLg.getInstance();

        //Registering roles
        List<?> original = MainLg.getInstance().getConfig().getList("spawns");
        assert original != null;
        List<Object> list = new ArrayList<>(original);
        Collections.shuffle(list);
        for (LGPlayer lgp : getInGame()) {
            List<Double> location = (List<Double>) list.remove(0);
            Player p = lgp.getPlayer();
            p.setWalkSpeed(0);
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999, 180, false, false));
            spots.set(original.indexOf(location), lgp);
            p.teleport(new Location(p.getWorld(), location.get(0) + 0.5, location.get(1), location.get(2) + 0.5, location.get(3).floatValue(), location.get(4).floatValue()));
            WrapperPlayServerUpdateHealth update = new WrapperPlayServerUpdateHealth();
            update.setFood(6);
            update.setFoodSaturation(1);
            update.setHealth(20);
            update.sendPacket(p);
            lgp.getScoreboard().getLine(0).setDisplayName(GOLD + "Attribution des rôles...");
        }

        try {
            for (Class<? extends Role> role : main.getRoles())
                if (main.getConfig().getInt("role." + Role.getId(role)) > 0) roles.add(Role.makeNew(role, this));
        } catch (Exception err) {
            Bukkit.broadcastMessage(DARK_RED + BOLD + "Une erreur est survenue lors de la création des roles... Regardez la console !");
            err.printStackTrace();
        }

        new BukkitRunnable() {
            int timeLeft = 5 * 2;
            int actualRole = getRoles().size();

            @Override
            public void run() {
                if (--timeLeft == 0) {
                    cancel();
                    _start();
                    return;
                }
                if (timeLeft == 5 * 2 - 1) {
                    for (LGPlayer lgp : getInGame()) {
                        lgp.sendMessage(
                            DARK_GRAY + "Projet organisé par : " + YELLOW + BOLD + "Shytoos" + DARK_GRAY + ".\nPlugin développé par : " + YELLOW +
                                BOLD + "Leomelki" + DARK_GRAY + ".\nPlugin Continué par : " + YELLOW + BOLD + "Valgrifer" + DARK_GRAY + ".\n");
                        lgp.sendTitle("",
                            DARK_GRAY + DARK_GRAY + "Plugin LoupGarou par " + YELLOW + BOLD + "Leomelki" + DARK_GRAY + " & " + YELLOW + BOLD +
                                "Shytoos" + DARK_GRAY + " & " + YELLOW + BOLD + "Valgrifer", 40);
                        lgp.getPlayer().getInventory().clear();
                        lgp.getPlayer().updateInventory();
                    }
                    broadcastMessage(DARK_GREEN + "Attribution des rôles...", true);
                }

                if (--actualRole < 0) actualRole = getRoles().size() - 1;

                ItemStack stack = LGCardItems.getItem(getRoles().get(actualRole));
                for (LGPlayer lgp : getInGame()) {
                    lgp.getPlayer().getInventory().setItemInOffHand(stack);
                    lgp.getPlayer().updateInventory();
                }
            }
        }.runTaskTimer(MainLg.getInstance(), 0, 4);
    }

    private void _start() {
        broadcastMessage(DARK_GRAY + ITALIC + "Début de la partie...", true);
        //Give roles...
        ArrayList<LGPlayer> toGive = (ArrayList<LGPlayer>) inGame.clone();
        started = false;

        for (Role role : getRoles().stream()
            .sorted((o1, o2) -> (o2 instanceof CampTeam ? 1 : 0) - (o1 instanceof CampTeam ? 1 : 0))
            .collect(Collectors.toCollection(ArrayList::new))) {
            while (role.getWaitedPlayers() > 0) {
                int randomized = random.nextInt(toGive.size());
                LGPlayer player = toGive.remove(randomized);

                role.join(player);
                WrapperPlayServerUpdateHealth update = new WrapperPlayServerUpdateHealth();
                update.setFood(6);
                update.setFoodSaturation(1);
                update.setHealth(20);
                update.sendPacket(player.getPlayer());
            }
        }
        started = true;

        updateRoleScoreboard();

        //Start day one
        nextNight(10);
    }

    public void updateRoleScoreboard() {
        if (MainLg.getInstance().getConfig().getBoolean("compo.hidden", false)) {
            for (LGPlayer lgp : getInGame())
                lgp.getScoreboard().getLine(0).setDisplayName(GOLD + "Composition Caché");
            return;
        }

        HashMap<Role, Integer> rolesAlive = new HashMap<>();
        for (LGPlayer lgp : getAlive())
            if (lgp.getRole() != null && !lgp.getRole().isHidden())
                if (rolesAlive.containsKey(lgp.getRole())) rolesAlive.put(lgp.getRole(), rolesAlive.get(lgp.getRole()) + 1);
                else rolesAlive.put(lgp.getRole(), 1);

        List<Role> roles = new ArrayList<>(rolesAlive.keySet());

        for (int i = 0; i < roles.size(); i++)
            if (!roles.get(i).hasScoreBoardName()) for (int x = 0; x < roles.size(); x++)
                if (i != x && roles.get(i).getScoreBoardName().equals(roles.get(x).getScoreBoardName())) {
                    rolesAlive.put(roles.get(x), rolesAlive.get(roles.get(x)) + 1);
                    roles.remove(i);
                    i--;
                    break;
                }

        roles.sort(Comparator.comparing(Role::getScoreBoardName));

        for (LGPlayer lgp : getInGame())
            lgp.getScoreboard()
                .setLines(roles.parallelStream()
                    .map(role -> YELLOW + "" + rolesAlive.get(role) + " " + GOLD + "- " + YELLOW +
                        role.getScoreBoardName().replace(BOLD.toString(), ""))
                    .toArray(String[]::new));
    }

    public List<LGPlayer> getAlive() {
        return getAlive(null);
    }

    public List<LGPlayer> getAlive(Predicate<LGPlayer> filter) {
        Stream<LGPlayer> result = getInGame().parallelStream().filter(lgp -> !lgp.isDead());

        if (filter != null) result = result.filter(filter);

        return result.collect(Collectors.toList());
    }

    public void nextNight() {
        nextNight(5);
    }

    public void nextNight(int timeout) {
        if (ended) return;
        LGNightStart event = new LGNightStart(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (mayorKilled()) {//mort du maire
            broadcastMessage(BLUE + "Le " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + " est mort, il désigne un joueur en remplaçant.", true);
            getMayor().sendMessage(GOLD + "Choisis un joueur qui deviendra " + DARK_PURPLE + BOLD + "Capitaine" + GOLD + " à son tour.");
            LGGame.this.wait(30, () -> {
                mayor.stopChoosing();
                setMayor(getAlive().get(random.nextInt(getAlive().size())));
                broadcastMessage(GRAY + BOLD + mayor.getName() + BLUE + " devient le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + ".", true);
                nextNight();
            }, (player, secondsLeft) -> YELLOW + mayor.getName() + GOLD + " choisit qui sera le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + GOLD +
                " (" + YELLOW + secondsLeft + " s" + GOLD + ")");
            mayor.choose((choosen) -> {
                if (choosen != null) {
                    mayor.stopChoosing();
                    cancelWait();
                    setMayor(choosen);
                    broadcastMessage(GRAY + BOLD + mayor.getName() + BLUE + " devient le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + ".",
                        true);
                    nextNight();
                }
            }, mayor);
            return;
        }

        new BukkitRunnable() {
            int timeoutLeft = timeout * 20;

            @Override
            public void run() {
                if (--timeoutLeft <= 20 + 20 * 2) {
                    if (timeoutLeft == 20) cancel();
                    WrapperPlayServerUpdateTime time = new WrapperPlayServerUpdateTime();
                    time.setAgeOfTheWorld(0);
                    time.setTimeOfDay(LGGame.this.time = (long) (18000 - (timeoutLeft - 20D) / (20 * 2D) * 12000D));
                    for (LGPlayer lgp : getInGame())
                        time.sendPacket(lgp.getPlayer());
                }
            }
        }.runTaskTimer(MainLg.getInstance(), 1, 1);
        LGGame.this.wait(timeout, this::nextNight_,
            (player, secondsLeft) -> GOLD + "La nuit va tomber dans " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : ""));
    }

    private void nextNight_() {
        if (ended) return;
        night++;
        broadcastSpacer();
        broadcastMessage(BLUE + "----------- " + BOLD + "Nuit n°" + night + BLUE + " -----------", true);
        broadcastMessage(DARK_GRAY + ITALIC + "La nuit tombe sur le village...");
        for (LGPlayer player : getAlive())
            player.leaveChat();
        for (LGPlayer player : getInGame()) {
            player.stopAudio(LGSound.AMBIENT_DAY);
            player.playAudio(LGSound.START_NIGHT, 0.5);
            player.playAudio(LGSound.AMBIENT_NIGHT, 0.07);
        }
        day = false;
        Bukkit.getPluginManager().callEvent(new LGDayEndEvent(this));
        for (LGPlayer player : getInGame())
            player.hideView();

        //Classe les roles afin de les appeler dans le bon ordre
        ArrayList<Role> roles = (ArrayList<Role>) getRoles().clone();
        roles.sort(Comparator.comparingInt(Role::getTurnOrder));
        new Runnable() {
            Role lastRole;

            public void run() {
                Runnable run = this;
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (roles.size() == 0) {
                            Bukkit.getPluginManager().callEvent(new LGRoleTurnEndEvent(LGGame.this, null, lastRole));
                            lastRole = null;
                            endNight();
                            return;
                        }
                        Role role = roles.remove(0);
                        Bukkit.getPluginManager().callEvent(new LGRoleTurnEndEvent(LGGame.this, role, lastRole));
                        lastRole = role;
                        if (role.getTurnOrder() == -1 || !role.hasPlayersLeft()) this.run();
                        else {
                            broadcastMessage(BLUE + role.getBroadcastedTask());
                            role.onNightTurn(run);
                        }
                    }
                }.runTaskLater(MainLg.getInstance(), 60);
            }
        }.run();
    }

    public boolean kill(LGPlayer killed, Reason reason, boolean endGame) {
        if (killed.getPlayer() != null) {
            killed.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1, false, false));
            killed.die();

            killed.showView();

            for (LGPlayer lgp : getInGame())
                if (lgp == killed) {
                    WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                    List<PlayerInfoData> infos = new ArrayList<>();
                    info.setAction(PlayerInfoAction.REMOVE_PLAYER);
                    infos.add(new PlayerInfoData(new WrappedGameProfile(lgp.getPlayer().getUniqueId(), lgp.getName()), 0, NativeGameMode.ADVENTURE,
                        WrappedChatComponent.fromText(lgp.getName())));
                    info.setData(infos);
                    info.sendPacket(lgp.getPlayer());
                }
                else lgp.getPlayer().hidePlayer(MainLg.getInstance(), killed.getPlayer());

            if (vote != null) vote.remove(killed);

            //Lightning effect
            killed.getPlayer().getWorld().strikeLightningEffect(killed.getPlayer().getLocation());
            killed.setDead(true);

            for (LGPlayer lgp : inGame) {
                LGDeathAnnouncementEvent event = new LGDeathAnnouncementEvent(this, lgp, killed, reason);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isMessageHidden()) lgp.sendMessage(
                    String.format(reason.getMessage(), killed.getName()) + ", il était " + killed.getRevealRole(event.getShowedRole()) + DARK_RED +
                        ".");
            }


            logMessage(String.format(reason.getMessage(), killed.getName()) + ", il était " + killed.getRevealRole() + DARK_RED + ".");

            for (Role role : getRoles())
                role.getPlayers().remove(killed);

            killed.sendMessage(GRAY + "Vous pouvez faire " + WHITE + "/spec" + GRAY + " pour voir les rôles de tout le monde");

            Bukkit.getPluginManager().callEvent(new LGPlayerGotKilledEvent(this, killed, reason, !checkEndGame(false) && endGame));

            VariousUtils.setWarning(killed.getPlayer(), true);

            killed.getPlayer().getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));

            LGCardItems.updateItem(killed);

            //killed.leaveChat();
            killed.joinChat(this.getChat(LGChatType.SPEC));
        }

        //Update scoreboard
        if (MainLg.getInstance().getConfig().getBoolean("compo.update_on_kill", true)) updateRoleScoreboard();

        return checkEndGame(endGame);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() == this && e.getWinType() == LGWinType.VILLAGEOIS)
            for (LGPlayer lgp : getInGame())
                if (lgp.getRoleType() == RoleType.VILLAGER) e.getWinners().add(lgp);
    }

    public void endGame(LGWinType winType) {
        if (ended) return;

        LGGameEndEvent event = new LGGameEndEvent(this, winType, new ArrayList<>());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        for (LGPlayer lgp : getInGame())//Avoid bugs
            if (lgp.getPlayer() != null) {
                event.getGame()
                    .broadcastMessage(
                        GRAY + "" + (lgp.isDead() ? STRIKETHROUGH : BOLD) + lgp.getName() + BLUE + (lgp.isDead() ? " était " : " est ") +
                            lgp.getRevealRole(true) + BLUE + ".");
                lgp.getPlayer().closeInventory();
            }

        cancelWait();//Also avoid bugs

        ended = true;
        //We unregister every role listener because they are unused after the game's end !
        for (Role role : getRoles())
            HandlerList.unregisterAll(role);

        List<LGPlayer> reverseWinners = event.getWinners();
        Collections.reverse(reverseWinners);

        broadcastMessage(winType.getMessage(), true);
        if (winType != LGWinType.EQUAL)
            broadcastMessage(GOLD + BOLD + ITALIC + "Bravo à " + reverseWinners.stream().reduce("", (previous, lgPlayer) -> {
                if (previous.equals("")) return GRAY + "" + BOLD + lgPlayer.getName();
                if (!previous.contains(" ")) return GRAY + "" + BOLD + lgPlayer.getName() + GOLD + BOLD + ITALIC + " et " + previous;
                return GRAY + "" + BOLD + lgPlayer.getName() + GOLD + BOLD + ITALIC + ", " + previous;
            }, String::concat) + GOLD + BOLD + ITALIC + " !", true);
        for (LGPlayer lgp : getInGame()) {
            lgp.leaveChat();
            lgp.joinChat(this.getChat(LGChatType.SPEC));

            lgp.setScoreboard(null);

            if (event.getWinners().contains(lgp)) lgp.sendTitle(GREEN + BOLD + "Victoire !", GOLD + "Vous avez gagné la partie.", 200);
            else if (winType == LGWinType.EQUAL || winType == LGWinType.NONE)
                lgp.sendTitle(GRAY + BOLD + "Égalité", DARK_GRAY + "Personne n'a gagné...", 200);
            else lgp.sendTitle(RED + BOLD + "Défaite...", DARK_RED + "Vous avez perdu la partie.", 200);


            Player p = lgp.getPlayer();
            p.removePotionEffect(PotionEffectType.JUMP);
            p.setWalkSpeed(0.2f);
            p.getInventory().clear();
            p.updateInventory();
        }

        if (MainLg.getInstance().getCurrentGame() == this) MainLg.makeNewGame();

        for (LGPlayer lgp : getInGame())
            if (lgp.getPlayer().isOnline()) {
                LGPlayer.resetPlayer(lgp.getPlayer());
                WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                team.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED);
                team.setName("you_are");
                team.sendPacket(lgp.getPlayer());
                lgp = LGPlayer.get(lgp.getPlayer());
                lgp.join(MainLg.getInstance().getCurrentGame());
                lgp.showView();
            }
    }

    public boolean mayorKilled() {
        return getMayor() != null && getMayor().isDead();
    }

    public void endNight() {
        if (ended) return;

        for (LGPlayer player : getInGame()) {
            player.updatePrefix();
            Bukkit.getScheduler().runTaskLater(MainLg.getInstance(), () -> player.hideView(), 20L);
        }

        LGPreNightEndEvent eventPreNightEnd = new LGPreNightEndEvent(this);
        Bukkit.getPluginManager().callEvent(eventPreNightEnd);
        if (eventPreNightEnd.isCancelled()) return;

        wait(2, () -> {
            broadcastSpacer();
            broadcastMessage(BLUE + "----------- " + BOLD + "Jour n°" + night + BLUE + " -----------", true);
            broadcastMessage(DARK_GRAY + ITALIC + "Le jour se lève sur le village...");

            for (LGPlayer p : getInGame()) {
                p.stopAudio(LGSound.AMBIENT_NIGHT);
                p.playAudio(LGSound.START_DAY, 0.5);
                p.playAudio(LGSound.AMBIENT_DAY, 0.07);
            }

            LGNightEndEvent eventNightEnd = new LGNightEndEvent(this);
            Bukkit.getPluginManager().callEvent(eventNightEnd);
            if (eventNightEnd.isCancelled()) return;

            int died = 0;
            boolean endGame = false;

            for (Entry<Reason, LGPlayer> entry : deaths.entrySet()) {
                if (entry.getKey() == Reason.DONT_DIE) continue;
                if (entry.getValue()
                    .isDead())//On ne fait pas mourir quelqu'un qui est déjà mort (résout le problème du dictateur tué par le chasseur)
                    continue;
                if (entry.getValue().getPlayer() != null) {//S'il a deco bah au moins ça crash pas hehe
                    LGPlayerKilledEvent event = new LGPlayerKilledEvent(this, entry.getValue(), entry.getKey());
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        endGame |= kill(event.getKilled(), event.getReason(), false);
                        died++;
                    }
                }
            }
            deaths.clear();
            if (died == 0) broadcastMessage(BLUE + "Étonnamment, personne n'est mort cette nuit.");

            day = true;
            for (LGPlayer player : getInGame()) {
                player.showView();

                Bukkit.getScheduler().runTaskLater(MainLg.getInstance(), () -> {
                    player.updatePrefix();
                    player.updateOwnSkin();
                    player.updateSkin();
                }, 20L);
            }


            new BukkitRunnable() {
                int timeoutLeft = 20;

                @Override
                public void run() {
                    if (timeoutLeft++ > 20) {
                        if (timeoutLeft == 20 + (2 * 20)) cancel();
                        WrapperPlayServerUpdateTime time = new WrapperPlayServerUpdateTime();
                        time.setAgeOfTheWorld(0);
                        time.setTimeOfDay(LGGame.this.time = (long) (18000 - (timeoutLeft - 20D) / (20 * 2D) * 12000D));
                        for (LGPlayer lgp : getInGame())
                            time.sendPacket(lgp.getPlayer());
                    }
                }
            }.runTaskTimer(MainLg.getInstance(), 1, 1);

            LGPreDayStartEvent dayStart = new LGPreDayStartEvent(this);
            Bukkit.getPluginManager().callEvent(dayStart);
            if (dayStart.isCancelled()) return;

            if (endGame) checkEndGame();
            else startDay();
        });
    }

    public void startDay() {
        for (LGPlayer player : getInGame())
            player.joinChat(this.getChat(LGChatType.VILLAGE), player.isDead());

        LGDayStartEvent dayStart = new LGDayStartEvent(this);
        Bukkit.getPluginManager().callEvent(dayStart);

        if (dayStart.isCancelled()) return;


        if (mayorKilled()) {//mort du maire
            broadcastMessage(BLUE + "Le " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + " est mort, il désigne un joueur en remplaçant.", true);
            getMayor().sendMessage(GOLD + "Choisis un joueur qui deviendra " + DARK_PURPLE + BOLD + "Capitaine" + GOLD + " à son tour.");
            LGGame.this.wait(30, () -> {
                mayor.stopChoosing();
                setMayor(getAlive().get(random.nextInt(getAlive().size())));
                broadcastMessage(GRAY + BOLD + mayor.getName() + BLUE + " devient le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + ".", true);
                startDay();
            }, (player, secondsLeft) -> YELLOW + mayor.getName() + GOLD + " choisit qui sera le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + GOLD +
                " (" + YELLOW + secondsLeft + " s" + GOLD + ")");
            mayor.choose((choosen) -> {
                if (choosen != null) {
                    mayor.stopChoosing();
                    cancelWait();
                    setMayor(choosen);
                    broadcastMessage(GRAY + BOLD + mayor.getName() + BLUE + " devient le nouveau " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + ".",
                        true);
                    startDay();
                }
            }, mayor);
            return;
        }
        new BukkitRunnable() {

            @Override
            public void run() {
                if (getMayor() == null && getAlive().size() > 2) mayorVote();
                else peopleVote();
            }
        }.runTaskLater(MainLg.getInstance(), 40);
    }

    public void setMayor(LGPlayer mayor) {
        LGPlayer latestMayor = this.mayor;
        this.mayor = mayor;
        if (mayor != null && mayor.getPlayer().isOnline()) {
            LGCardItems.updateItem(mayor);
            mayor.updateSkin();
            mayor.updateOwnSkin();
        }
        if (latestMayor != null && latestMayor.getPlayer() != null && latestMayor.getPlayer().isOnline()) {
            LGCardItems.updateItem(latestMayor);
            latestMayor.updateSkin();
            latestMayor.updateOwnSkin();
        }
    }

    @EventHandler
    public void onCustomItemChange(LGCustomItemChangeEvent e) {
        if (e.getGame() == this) {
            if (getMayor() == e.getPlayer()) e.getConstraints().add(Constraint.MAYOR);
            if (e.getPlayer().isDead()) e.getConstraints().add(Constraint.DEAD);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSkinChange(LGSkinLoadEvent e) {
        if (e.getGame() == this) {
            e.getProfile().getProperties().removeAll("textures");
            e.getProfile()
                .getProperties()
                .put("textures", getMayor() == e.getPlayer() ? LGCustomSkin.MAYOR.getProperty() : LGCustomSkin.VILLAGER.getProperty());
        }
    }

    private void mayorVote() {
        if (ended) return;

        LGVoteRequestedEvent event = new LGVoteRequestedEvent(this, LGVoteCause.MAYOR);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (event.isContinuePeopleVote()) peopleVote();
            return;
        }

        broadcastMessage(BLUE + "Il est temps de voter pour élire un " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + ".", true);
        vote = LGVote.builder(this, event.getCause())
                       .hideViewersMessage(event.isHideViewersMessage())
                       .randomIfEqual(true)
                       .generator(
                               (player, secondsLeft) ->
                                       player.getCache().has("vote") ?
                                               GOLD + "Tu votes pour " + GRAY + BOLD + player.getCache().<LGPlayer> get("vote").getName() :
                                               GOLD + "Il te reste " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : "") + GOLD + " pour voter")
                       .build();
        vote.start(getAlive(), getInGame(), () -> {
            if (vote.getChoosen() == null) setMayor(getAlive().get(random.nextInt(getAlive().size())));
            else setMayor(vote.getChoosen());

            broadcastMessage(GRAY + BOLD + mayor.getName() + GOLD + " devient le " + DARK_PURPLE + BOLD + "Capitaine " + GOLD + "du village.", true);
            peopleVote();
        });
    }

    @EventHandler
    public void leaderChange(LGVoteLeaderChange e) {
        if (isPeopleVote && vote != null && e.getGame() == this) {
            for (LGPlayer player : e.getLatest())
                if (!e.getNow().contains(player)) VariousUtils.setWarning(player.getPlayer(), false);

            for (LGPlayer player : e.getNow())
                if (!e.getLatest().contains(player) && !player.isFakePlayer()) VariousUtils.setWarning(player.getPlayer(), true);
        }
    }

    private void peopleVote() {
        if (ended) return;

        LGVoteRequestedEvent event = new LGVoteRequestedEvent(this, LGVoteCause.VILLAGE);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        broadcastMessage(BLUE + "La phase des votes a commencé.", true);
        isPeopleVote = true;

        vote = LGVote.builder(this, event.getCause())
                       .hideViewersMessage(event.isHideViewersMessage())
                       .allowBlankVote(true)
                       .generator((
                               player, secondsLeft) ->
                                          player.getCache().has("vote") ?
                                                  GOLD + "Tu votes pour " + GRAY + BOLD + player.getCache().<LGPlayer> get("vote").getName() :
                                                  GOLD + "Il te reste " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : "") + GOLD + " pour voter")
                       .build();
        vote.start(getAlive(), getInGame(), () -> {
            isPeopleVote = false;
            LGVoteEndEvent voteEnd = new LGVoteEndEvent(this, vote, vote.getCause());
            Bukkit.getPluginManager().callEvent(voteEnd);
            if (vote.getChoosen() == null || (vote.isMayorVote() && getMayor() == null))
                broadcastMessage(BLUE + "Personne n'est mort aujourd'hui.", true);
            else {
                LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(this, vote.getChoosen(), Reason.VOTE);
                Bukkit.getPluginManager().callEvent(killEvent);
                if (killEvent.isCancelled()) {
                    nextNight();
                    return;
                }
                if (kill(killEvent.getKilled(), killEvent.getReason(), true)) {
                    nextNight();
                    return;
                }
            }
            nextNight();
        }, mayor);
    }

    public boolean checkEndGame() {
        return checkEndGame(true);
    }

    public boolean checkEndGame(boolean doEndGame) {
        LGWinType winType = LGWinType.NONE;
        Map<RoleWinType, Integer> roleWinTypeAlive = new HashMap<>();

        if (getAlive().size() > 0) {
            for (LGPlayer lgp : getAlive())
                roleWinTypeAlive.put(lgp.getRoleWinType(), roleWinTypeAlive.getOrDefault(lgp.getRoleWinType(), 0) + 1);

            roleWinTypeAlive.remove(RoleWinType.NONE);

            if (roleWinTypeAlive.size() == 1) {
                RoleWinType roleWinType = roleWinTypeAlive.keySet().iterator().next();

                if (!roleWinType.equals(RoleWinType.SOLO) ||
                    (roleWinType.equals(RoleWinType.SOLO) && roleWinTypeAlive.getOrDefault(RoleWinType.SOLO, 0) == 1))
                    winType = roleWinType.getWinType();
            }
        }
        else winType = LGWinType.EQUAL;

        LGEndCheckEvent event = new LGEndCheckEvent(this, winType, roleWinTypeAlive);

        Bukkit.getPluginManager().callEvent(event);

        if (doEndGame && event.getWinType() != null && event.getWinType() != LGWinType.NONE) endGame(event.getWinType());

        return event.getWinType() != LGWinType.NONE;
    }

    public <R extends Role> R getRole(Class<R> clazz) {
        return getRole(clazz, false);
    }

    public <R extends Role> R getRole(Class<R> clazz, boolean addRole) {
        for (Role role : getRoles())
            if (role.getClass().equals(clazz)) return (R) role;

        if (!addRole) return null;

        R role = Role.makeNew(clazz, this);

        this.getRoles().add(role);

        return role;
    }

    public interface TextGenerator {
        String generate(LGPlayer player, int secondsLeft);
    }
}
