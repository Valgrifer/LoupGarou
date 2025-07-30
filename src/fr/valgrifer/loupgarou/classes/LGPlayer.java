package fr.valgrifer.loupgarou.classes;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.roles.Role;
import fr.valgrifer.loupgarou.roles.RoleType;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import fr.valgrifer.loupgarou.scoreboard.CustomScoreboard;
import fr.valgrifer.loupgarou.utils.NMSUtils;
import fr.valgrifer.loupgarou.utils.VariableCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.BLUE;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.RED;

@SuppressWarnings("ALL")
public class LGPlayer implements Interlocutor
{
    private static HashMap<Player, LGPlayer> cachedPlayers = new HashMap<Player, LGPlayer>();
    public boolean canSelectDead = false;
    @Getter
    @Setter
    boolean roleActive = true;
    @Getter
    boolean fakePlayer;
    @Getter
    boolean hidden = false;
    long lastChoose;
    @Getter
    private Player player = null;
    @Getter
    @Setter
    private boolean dead = false;
    @Setter
    @Getter
    private Role role = null;
    private LGChooseCallback chooseCallback = null;
    @Setter
    @Getter
    private boolean canChoose = true;
    private List<LGPlayer> blacklistedChoice = new ArrayList<>(0);
    @Getter
    private VariableCache cache = new VariableCache();
    @Getter
    @Setter
    private @Nullable LGGame game = null;
    @Getter
    private CustomScoreboard scoreboard = null;
    private String name = null;
    @Getter
    private LGChat chat = null;

    public LGPlayer(Player player) {
        this.player = player;
        this.fakePlayer = false;
    }

    public LGPlayer(String name) {
        this.name = name;
        this.fakePlayer = true;
    }

    public static LGPlayer get(Player player) {
        LGPlayer lgp = cachedPlayers.get(player);
        if (lgp == null) {
            lgp = new LGPlayer(player);
            cachedPlayers.put(player, lgp);
        }
        return lgp;
    }

    public static boolean has(Player player) {
        return cachedPlayers.containsKey(player);
    }

    public static void resetPlayer(Player player) {
        LGPlayer lgp = cachedPlayers.get(player);
        if (lgp == null) return;

        lgp.canSelectDead = false;
        lgp.roleActive = true;
        lgp.resetHidden();
        lgp.dead = false;
        lgp.role = null;
        lgp.chooseCallback = null;
        lgp.canChoose = true;
        lgp.blacklistedChoice = new ArrayList<>(0);
        lgp.cache = new VariableCache();
        lgp.game = null;
        lgp.scoreboard = null;
        lgp.chat = null;
    }

    public static LGPlayer remove(Player player) {
        return cachedPlayers.remove(player);//.remove();
    }

    public void setScoreboard(CustomScoreboard scoreboard) {
        if (player != null) {
            if (this.scoreboard != null) this.scoreboard.hide();

            this.scoreboard = scoreboard;

            if (scoreboard != null) scoreboard.show();
        }
    }

    public void sendActionBarMessage(String msg) {
        if (this.player != null) {
            WrapperPlayServerChat chat = new WrapperPlayServerChat();
            chat.setPosition((byte) 2);
            chat.setMessage(WrappedChatComponent.fromText(msg));
            chat.sendPacket(getPlayer());
        }
    }

    public void sendMessage(@Nonnull String msg) {
        if (this.player != null) getPlayer().sendMessage(msg);
    }

    public void sendTitle(String title, String subTitle, int stay) {
        if (this.player != null) {
            WrapperPlayServerTitle titlePacket = new WrapperPlayServerTitle();
            titlePacket.setAction(TitleAction.TIMES);
            titlePacket.setFadeIn(10);
            titlePacket.setStay(stay);
            titlePacket.setFadeOut(10);
            titlePacket.sendPacket(player);

            titlePacket = new WrapperPlayServerTitle();
            titlePacket.setAction(TitleAction.TITLE);
            titlePacket.setTitle(WrappedChatComponent.fromText(title));
            titlePacket.sendPacket(player);

            titlePacket = new WrapperPlayServerTitle();
            titlePacket.setAction(TitleAction.SUBTITLE);
            titlePacket.setTitle(WrappedChatComponent.fromText(subTitle));
            titlePacket.sendPacket(player);
        }
    }

    public void remove() {
        this.player = null;
    }

    public String getName() {
        return player != null ? getPlayer().getName() : name;
    }

    public boolean join(LGGame game) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR) {
            sendMessage(RED + "Étant en mode spectateur, vous ne rejoignez pas la partie !");
            return false;
        }
        if (game != null && game.tryToJoin(this)) {
            //To update the skin
            updateOwnSkin();
            getPlayer().setWalkSpeed(0.2f);
            //		sendMessage(DARK_GREEN+"Vous venez de rejoindre une partie de Loup-Garou. "+GREEN+"Bon jeu!");
            return true;
        }
        return false;
    }

    public void choose(LGChooseCallback callback, LGPlayer... blacklisted) {
        this.blacklistedChoice = blacklisted == null ? new ArrayList<>(0) : Arrays.asList(blacklisted);
        this.chooseCallback = callback;
        this.canChoose = true;
//		sendMessage(GRAY+ITALIC+"TIP: Regardez un joueur et tapez le afin de le sélectionner.");
    }

    public void stopChoosing() {
        this.blacklistedChoice = null;
        this.chooseCallback = null;
    }

    public void showView() {
        if (getGame() != null && player != null)
            for (LGPlayer lgp : getGame().getAlive())
                if (!lgp.isDead()) {
                    if (lgp != this && lgp.getPlayer() != null) getPlayer().showPlayer(lgp.getPlayer());
                    else {
                        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                        team.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED);
                        team.setName(lgp.getName());
                        team.setPrefix(WrappedChatComponent.fromText(""));
                        team.setPlayers(Arrays.asList(lgp.getName()));
                        team.sendPacket(getPlayer());

                        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                        ArrayList<PlayerInfoData> infos = new ArrayList<PlayerInfoData>();
                        info.setAction(PlayerInfoAction.ADD_PLAYER);
                        infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE,
                            WrappedChatComponent.fromText(getName())));
                        info.setData(infos);
                        info.sendPacket(getPlayer());
                    }
                }

        getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 2, false, false));
    }

    public void updatePrefix() {
        if (getGame() != null && !isDead() && player != null) {
            List<String> meList = Collections.singletonList(getName());
            for (LGPlayer lgp : getGame().getInGame()) {
                WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                List<PlayerInfoData> infos = new ArrayList<>();
                info.setAction(PlayerInfoAction.ADD_PLAYER);
                infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE,
                    WrappedChatComponent.fromText(getName())));
                info.setData(infos);
                info.sendPacket(lgp.getPlayer());

                WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                team.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED);
                team.setName(getName());
                team.setPrefix(WrappedChatComponent.fromText(""));
                team.setPlayers(meList);
                team.sendPacket(lgp.getPlayer());
            }
        }
    }

    public void hideView() {
        if (isDead()) return;

        if (getGame() != null && player != null) {
            WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
            List<PlayerInfoData> infos = new ArrayList<>();
            info.setAction(PlayerInfoAction.ADD_PLAYER);
            for (LGPlayer lgp : getGame().getAlive())
                if (lgp != this && lgp.getPlayer() != null) {
                    if (!lgp.isDead()) infos.add(
                        new PlayerInfoData(new WrappedGameProfile(lgp.getPlayer().getUniqueId(), lgp.getName()), 0, NativeGameMode.ADVENTURE,
                            WrappedChatComponent.fromText(lgp.getName())));
                    getPlayer().hidePlayer(lgp.getPlayer());
                }
            info.setData(infos);
            info.sendPacket(getPlayer());
        }

        getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));
    }

    public void updateSkin() {
        if (getGame() != null && player != null) {
            for (LGPlayer lgp : getGame().getInGame()) {
                if (lgp == this) {
                    WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
                    List<PlayerInfoData> infos = new ArrayList<>();
                    info.setAction(PlayerInfoAction.ADD_PLAYER);
                    infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE,
                        WrappedChatComponent.fromText(getName())));
                    info.setData(infos);
                    info.sendPacket(getPlayer());
                }
                else if (!isDead() && lgp.getPlayer() != null) {
                    lgp.getPlayer().hidePlayer(MainLg.getInstance(), getPlayer());
                    lgp.getPlayer().showPlayer(MainLg.getInstance(), getPlayer());
                }
            }
        }
    }

    public void updateOwnSkin() {
        if (player == null) return;
        //On change son skin avec un packet de PlayerInfo (dans le tab)
        WrapperPlayServerPlayerInfo infos = new WrapperPlayServerPlayerInfo();
        infos.setAction(PlayerInfoAction.ADD_PLAYER);
        WrappedGameProfile gameProfile = new WrappedGameProfile(getPlayer().getUniqueId(), getPlayer().getName());
        infos.setData(
            Arrays.asList(new PlayerInfoData(gameProfile, 10, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(getPlayer().getName()))));
        infos.sendPacket(getPlayer());
        //Pour qu'il voit son skin changer (sa main et en f5), on lui dit qu'il respawn (alors qu'il n'est pas mort mais ça marche quand même mdr)
        NMSUtils.getInstance().sendRespawn(getPlayer());
        //Enfin, on le téléporte à sa potion actuelle car sinon il se verra dans le vide
        getPlayer().teleport(getPlayer().getLocation());
        float speed = getPlayer().getWalkSpeed();
        getPlayer().setWalkSpeed(0.2f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer() == null) return;
                getPlayer().updateInventory();
                getPlayer().setWalkSpeed(speed);
            }
        }.runTaskLater(MainLg.getInstance(), 5);
        //Et c'est bon, le joueur se voit avec un nouveau skin avec quasiment aucun problème visible à l'écran :D
    }

    public LGPlayer getPlayerOnCursor(List<LGPlayer> list) {
        Location loc = getPlayer().getLocation();

        if (loc.getPitch() > 60)
            if (blacklistedChoice.contains(this)) return null;
            else return this;

        RayTraceResult result = this.getPlayer().getWorld().rayTraceEntities(
                getPlayer().getEyeLocation(), loc.getDirection(), 64D, e -> {
                    if (!(e instanceof Player))
                        return false;

                    LGPlayer lgp = LGPlayer.get((Player) e);
                    return lgp != this && this.getGame().getInGame().contains(lgp) && !blacklistedChoice.contains(lgp) && (!lgp.isDead() || canSelectDead);
                }
        );

        if (result != null && result.getHitEntity() instanceof Player)
            return LGPlayer.get((Player) result.getHitEntity());

        return null;
    }

    public RoleType getRoleType() {
        return this.getCache().get("RoleType", getRole().getType());
    }

    public void setRoleType(RoleType roleType) {
        this.getCache().set("RoleType", roleType);
    }

    public RoleWinType getRoleWinType() {
        try {
            return this.getCache().get("RoleWinType", getRole().getWinType());
        } catch (Exception ignored) {
            return getRole().getWinType();
        }
    }

    public void setRoleWinType(RoleWinType winType) {
        this.getCache().set("RoleWinType", winType);
    }

    public int getSpot() {
        return this.getGame().getSpots().indexOf(this);
    }

    public void die() {
        setHidden();
    }

    private void setHidden() {
        if (player != null)
            for (LGPlayer lgp : getGame().getInGame())
                if (lgp != this && lgp.getPlayer() != null)
                    lgp.getPlayer().hidePlayer(getPlayer());
        hidden = true;
    }

    public void resetHidden() {
        hidden = false;
    }

    public void joinChat(@Nonnull LGChat chat) {
        joinChat(chat, false);
    }

    public void joinChat(@Nonnull LGChat chat, boolean muted) {
        if (this.chat != null) this.chat.leave(this);

        if (!muted) this.chat = chat;

        if (chat != null && player != null) chat.join(this);
    }


    public void leaveChat() {
        if (this.chat != null) this.chat.leave(this);
        if (this.game != null) joinChat(this.game.getChat(LGChatType.NOCHAT));
        else this.chat = null;
    }

    public void onChat(@Nonnull String message) {
        if (chat != null)
            chat.sendMessage(this, message);
    }


    public void playAudio(LGSound sound, double volume) {
        if (player != null) sound.play(getPlayer(), (float) volume, 1);
    }

    public void stopAudio(LGSound sound) {
        if (player != null) sound.stop(getPlayer());
    }

    public void chooseAction() {
        long now = System.currentTimeMillis();
        if (lastChoose + 200 < now) {
            if (chooseCallback != null) chooseCallback.callback(getPlayerOnCursor(getGame().getInGame()));
            lastChoose = now;
        }
    }

    public void addEndGameReaveal(String msg) {
        List<String> cache = getCache().get("endReaveal", new ArrayList<>());
        if (!cache.contains(msg)) cache.add(msg);
        cache.sort(null);
        getCache().set("endReaveal", cache);
    }

    public String getRevealRole() {
        return getRevealRole(false);
    }

    public String getRevealRole(boolean endReaveal) {
        return getRevealRole(this.getRole().getClass(), endReaveal);
    }

    public String getRevealRole(Class<? extends Role> role) {
        return getRevealRole(role, false);
    }

    public String getRevealRole(Class<? extends Role> role, boolean endReaveal) {
        String result = Role.getName(role);

        if (endReaveal && getCache().has("endReaveal")) {
            List<String> cache = getCache().get("endReaveal");

            result += BLUE + " " + getCache().<List<String>> get("endReaveal").parallelStream().collect(Collectors.joining(BLUE + " "));
        }

        return result;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getName() + ")";
    }

    public interface LGChooseCallback {
        void callback(LGPlayer choosen);
    }
}
