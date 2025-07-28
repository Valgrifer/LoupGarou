package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWereWolf extends Role implements CampTeam {

    @Getter
    private final LGChat chat;
    @Getter
    private final List<LGPlayer> hiddenPlayers = new ArrayList<>();
    @Getter
    private final List<LGPlayer> fakePlayers = new ArrayList<>();
    boolean showSkins = false;
    LGVote vote;

    public RWereWolf(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.LOUP_GAROU) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                return RED + context.getInterlocutor().getName() + " " + GOLD + "» " + WHITE + message;
            }
        };
    }

    public static String _getName() {
        return RED + BOLD + "Loup-Garou";
    }

    public static String _getFriendlyName() {
        return "des " + RED + BOLD + "Loups-Garous";
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes avec les " + RoleType.LOUP_GAROU.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à éliminer.";
    }

    public static String _getTask() {
        return "Vote pour la cible à tuer.";
    }

    public static String _getBroadcastedTask() {
        return "Les " + RoleType.LOUP_GAROU.getColoredName(BOLD) + "s" + BLUE + " choisissent leur cible.";
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.LOUP_GAROU;
    }

    public static RWereWolf forceJoin(LGPlayer player) {
        LGGame game = player.getGame();

        if (game == null) return null;

        RWereWolf lg = game.getRole(RWereWolf.class, true);
        lg.join(player, false);
        return lg;
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        for (LGPlayer p : getPlayers())
            p.updatePrefix();
    }

    @Override
    public boolean addHiddenPlayer(LGPlayer player) {
        if (!getPlayers().contains(player) || hiddenPlayers.contains(player)) return false;
        return hiddenPlayers.add(player);
    }

    @Override
    public boolean removeHiddenPlayer(LGPlayer player) {
        if (!getPlayers().contains(player) || !hiddenPlayers.contains(player)) return false;
        return hiddenPlayers.remove(player);
    }

    @Override
    public boolean addAllHiddenPlayer(List<LGPlayer> players) {
        return hiddenPlayers.addAll(players.stream()
            .filter(player -> getPlayers().contains(player) && !hiddenPlayers.contains(player))
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public boolean removeAllHiddenPlayer(List<LGPlayer> players) {
        return hiddenPlayers.removeAll(players.stream()
            .filter(player -> getPlayers().contains(player) && hiddenPlayers.contains(player))
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public boolean addFakePlayer(LGPlayer player) {
        if (getPlayers().contains(player) || fakePlayers.contains(player)) return false;
        return fakePlayers.add(player);
    }

    @Override
    public boolean removeFakePlayer(LGPlayer player) {
        if (getPlayers().contains(player) || !fakePlayers.contains(player)) return false;
        return fakePlayers.remove(player);
    }

    @Override
    public boolean addAllFakePlayer(List<LGPlayer> players) {
        return fakePlayers.addAll(players.stream()
            .filter(player -> !getPlayers().contains(player) && !fakePlayers.contains(player))
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public boolean removeAllFakePlayer(List<LGPlayer> players) {
        return fakePlayers.removeAll(players.stream()
            .filter(player -> !getPlayers().contains(player) && fakePlayers.contains(player))
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public List<LGPlayer> getVisiblePlayers() {
        List<LGPlayer> players = (List<LGPlayer>) getPlayers().clone();

        players.removeAll(hiddenPlayers);
        players.addAll(fakePlayers);

        return players;
    }

    public void onNightTurn(Runnable callback) {
        LGVoteRequestedEvent event = new LGVoteRequestedEvent(getGame(), LGVoteCause.LOUPGAROU);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            callback.run();
            return;
        }

        vote = new LGVote(event.getCause(), getTimeout(), getTimeout() / 3, getGame(), event.isHideViewersMessage(), false,
            (player, secondsLeft) -> !getPlayers().contains(player) ?
                GOLD + "C'est au tour " + getFriendlyName() + " " + GOLD + "(" + YELLOW + secondsLeft + " s" + GOLD + ")" : player.getCache()
                .has("vote") ?
                BOLD + BLUE + "Vous votez contre " + RED + BOLD + player.getCache().<LGPlayer> get("vote").getName() :
                GOLD + "Il vous reste " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : "") + GOLD + " pour voter");

        for (LGPlayer player : getPlayers()) {
            player.sendMessage(GOLD + getTask());
            player.showView();
            //	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 100);
            player.joinChat(this.getChat());
        }
        vote.start(getPlayers(), getPlayers(), () -> {
            onNightTurnEnd();
            callback.run();
        });
    }

    private void onNightTurnEnd() {
        for (LGPlayer player : getPlayers()) {
            player.hideView();
            player.leaveChat();
        }

        LGPlayer choosen = vote.getChoosen();
        if (choosen == null) {
            if (!vote.getVotes().isEmpty()) {
                int max = 0;
                boolean equal = false;
                for (Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
                    if (entry.getValue().size() > max) {
                        equal = false;
                        max = entry.getValue().size();
                        choosen = entry.getKey();
                    }
                    else if (entry.getValue().size() == max) equal = true;
                if (equal) {
                    choosen = null;
                    List<LGPlayer> choosable = new ArrayList<>();
                    for (Entry<LGPlayer, List<LGPlayer>> entry : vote.getVotes().entrySet())
                        if (entry.getValue().size() == max && entry.getKey().getRoleType() != RoleType.LOUP_GAROU) choosable.add(entry.getKey());
                    if (!choosable.isEmpty()) choosen = choosable.get(getGame().getRandom().nextInt(choosable.size()));
                }
            }
        }
        if (choosen != null) {
            getGame().kill(choosen, Reason.LOUP_GAROU);
            for (LGPlayer player : getPlayers())
                player.sendMessage(
                    GOLD + "Les " + RED + BOLD + "Loups" + GOLD + " ont décidé de tuer " + GRAY + BOLD + choosen.getName() + GOLD + ".");
        }
        else for (LGPlayer player : getPlayers())
            player.sendMessage(GOLD + "Personne n'a été désigné pour mourir.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkinChange(LGSkinLoadEvent e) {
        if (e.getGame() == getGame())
            if (getVisiblePlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()) && showSkins) {
                e.getProfile().getProperties().removeAll("textures");
                e.getProfile().getProperties().put("textures", LGCustomSkin.WEREWOLF.getProperty());
            }
    }

    @EventHandler
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
//        System.out.printf("player: '%s', to: '%s', players contains: '%s', visible contains: '%s'%n", e.getPlayer().getName(), e.getTo().getName(), getVisiblePlayers().contains(e.getPlayer()), getPlayers().contains(e.getTo()));
        if (e.getGame() == getGame())
            if (getVisiblePlayers().contains(e.getPlayer()) && getPlayers().contains(e.getTo()))
                e.setColorName(ChatColor.RED);
    }

    @EventHandler
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.LOUPGAROU) for (LGPlayer lgp : getGame().getInGame())
            if (lgp.getRoleWinType() == RoleWinType.LOUP_GAROU)//Changed to wintype
                e.getWinners().add(lgp);
    }

    @EventHandler
    public void onDay(LGPreNightEndEvent e) {
        if (e.getGame() == getGame()) {
            showSkins = false;
            for (LGPlayer player : getVisiblePlayers())
                player.updateOwnSkin();
        }
    }

    @EventHandler
    public void onNight(LGDayEndEvent e) {
        if (e.getGame() == getGame()) {
            showSkins = true;
            for (LGPlayer player : getVisiblePlayers())
                player.updateOwnSkin();
        }
    }
}
