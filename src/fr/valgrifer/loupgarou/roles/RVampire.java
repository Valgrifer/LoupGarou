package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.classes.LGCardItems.Constraint;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.*;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import lombok.Getter;
import lombok.Setter;
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

public class RVampire extends Role implements CampTeam {

    @Getter
    private final LGChat chat;
    @Getter
    private final List<LGPlayer> hiddenPlayers = new ArrayList<>();
    @Getter
    private final List<LGPlayer> fakePlayers = new ArrayList<>();
    int nextCanInfect = 0;
    LGVote vote;

    public RVampire(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.VAMPIRE) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                return DARK_PURPLE + context.getInterlocutor().getName() + " " + GOLD + "» " + WHITE + message;
            }
        };
    }

    public static String _getName() {
        return DARK_PURPLE + BOLD + "Vampire";
    }

    public static String _getFriendlyName() {
        return "des " + DARK_PURPLE + BOLD + "Vampires";
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes avec les " + RoleWinType.VAMPIRE.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu te réunis avec tes compères pour décider d'une victime à transformer en " +
            RoleWinType.VAMPIRE.getColoredName(BOLD) + WHITE + "... Lorsqu'une transformation a lieu, tous les " +
            RoleWinType.VAMPIRE.getColoredName(BOLD) + WHITE +
            " doivent se reposer la nuit suivante. Un joueur transformé perd tous les pouvoirs liés à son ancien rôle, et gagne avec les " +
            RoleWinType.VAMPIRE.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Votez pour une cible à mordre.";
    }

    public static String _getBroadcastedTask() {
        return "Les " + DARK_PURPLE + BOLD + "Vampires" + BLUE + " choisissent leur cible.";
    }

    public static RoleType _getType() {
        return RoleType.VAMPIRE;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VAMPIRE;
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    @Override
    public boolean hasPlayersLeft() {
        return nextCanInfect < getGame().getNight() && super.hasPlayersLeft();
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
        LGVoteRequestedEvent event = new LGVoteRequestedEvent(getGame(), LGVoteCause.VAMPIRE);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            callback.run();
            return;
        }

        vote = LGVote.builder(getGame(), event.getCause())
                       .timeout(getTimeout())
                       .littleTimeout(getTimeout() / 3)
                       .hideViewersMessage(event.isHideViewersMessage())
                       .randomIfEqual(false)
                       .generator(
                               (player, secondsLeft) ->
                                       !getPlayers().contains(player) ?
                                               GOLD + "C'est au tour " + getFriendlyName() + " " + GOLD + "(" + YELLOW + secondsLeft + " s" + GOLD + ")" :
                                               player.getCache().has("vote") ?
                                                       BOLD + BLUE + "Vous votez pour " + RED + BOLD + player.getCache().<LGPlayer> get("vote").getName() :
                                                       GOLD + "Il vous reste " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : "") + GOLD + " pour voter")
                       .build();

        for (LGPlayer lgp : getGame().getAlive())
            if (lgp.getRoleType() == RoleType.VAMPIRE) lgp.showView();
        for (LGPlayer player : getPlayers()) {
            player.sendMessage(GOLD + getTask());
            //	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 100);
            player.joinChat(chat);
        }
        vote.start(getPlayers(), getPlayers(), () -> {
            onNightTurnEnd();
            callback.run();
        }, getPlayers());
    }

    private void onNightTurnEnd() {
        for (LGPlayer lgp : getGame().getAlive())
            if (lgp.getRoleType() == RoleType.VAMPIRE) lgp.hideView();
        for (LGPlayer player : getPlayers())
            player.leaveChat();

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
                        if (entry.getValue().size() == max && entry.getKey().getRoleType() != RoleType.VAMPIRE) choosable.add(entry.getKey());
                    if (!choosable.isEmpty()) choosen = choosable.get(getGame().getRandom().nextInt(choosable.size()));
                }
            }
        }
        if (choosen != null) {
            if (choosen.getRoleType() == RoleType.LOUP_GAROU || choosen.getRoleType() == RoleType.VAMPIRE) {
                for (LGPlayer player : getPlayers())
                    player.sendMessage(RED + "Votre cible est immunisée.");
                return;
            }
            else if (choosen.getRole() instanceof RVampireHunter) {
                for (LGPlayer player : getPlayers())
                    player.sendMessage(RED + "Votre cible est immunisée.");
                getGame().kill(getPlayers().get(getPlayers().size() - 1), Reason.CHASSEUR_DE_VAMPIRE);
                return;
            }

            LGRoleActionEvent event = new LGRoleActionEvent(getGame(), new VampiredAction(choosen), getPlayers());
            Bukkit.getPluginManager().callEvent(event);
            RVampire.VampiredAction action = (RVampire.VampiredAction) event.getAction();

            if (action.isImmuned() && !action.isForceMessage()) {
                for (LGPlayer player : event.getPlayers())
                    player.sendMessage(RED + "Votre cible est immunisée.");
                return;
            }
            else if (action.isProtect() && !action.isForceMessage()) {
                for (LGPlayer player : event.getPlayers())
                    player.sendMessage(RED + "Votre cible est protégée.");
                return;
            }

            for (LGPlayer player : getPlayers())
                player.sendMessage(
                    GRAY + BOLD + action.getTarget().getName() + " s'est transformé en " + DARK_PURPLE + BOLD + "Vampire" + GOLD + ".");

            if (!action.isForceMessage()) {
                action.getTarget()
                    .sendMessage(
                        GOLD + "Tu as été infecté par les " + DARK_PURPLE + BOLD + "Vampires " + GOLD + "pendant la nuit. Tu as perdu tes pouvoirs.");
                action.getTarget()
                    .sendMessage(GOLD + ITALIC + "Tu gagnes désormais avec les " + DARK_PURPLE + BOLD + ITALIC + "Vampires" + GOLD + ITALIC + ".");
                action.getTarget().setRoleWinType(RoleWinType.VAMPIRE);
                action.getTarget().setRoleType(RoleType.VAMPIRE);
                action.getTarget().setRoleActive(false);
                action.getTarget().getCache().set("vampire", true);
                action.getTarget().getCache().set("just_vampire", true);
                action.getTarget().addEndGameReaveal(DARK_PURPLE + "Vampire");
                join(action.getTarget(), false);
                LGCardItems.updateItem(action.getTarget());
            }
            nextCanInfect = getGame().getNight() + 1;
        }
        else for (LGPlayer player : getPlayers())
            player.sendMessage(GOLD + "Personne n'a été infecté.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDayStart(LGNightEndEvent e) {
        if (e.getGame() != getGame()) return;

        getGame().getAlive().stream().filter(player -> player.getCache().getBoolean("just_vampire")).forEach(player -> {
            player.getCache().remove("just_vampire");
            for (LGPlayer lgp : getGame().getInGame()) {
                if (lgp.getRoleType() == RoleType.VAMPIRE)
                    lgp.sendMessage(GRAY + BOLD + player.getName() + GOLD + " s'est transformé en " + DARK_PURPLE + BOLD + "Vampire" + GOLD + "...");
                else lgp.sendMessage(GOLD + "Quelqu'un s'est transformé en " + DARK_PURPLE + BOLD + "Vampire" + GOLD + "...");
            }

            if (getGame().checkEndGame()) e.setCancelled(true);
        });
    }

    /*	@EventHandler(priority = EventPriority.HIGHEST)
        public void onSkinChange(LGSkinLoadEvent e) {
            if(e.getGame() == getGame())
                if(getPlayers().contains(e.getPlayer()) && getVisiblePlayers().contains(e.getTo()) && showSkins) {
                    e.getProfile().getProperties().removeAll("textures");
                    e.getProfile().getProperties().put("textures", LGCustomSkin.WEREWOLF.getProperty());
                }
        }*/
    @EventHandler
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() == getGame() && e.getWinType() == LGWinType.VAMPIRE) for (LGPlayer lgp : getGame().getInGame())
            if (lgp.getRoleWinType() == RoleWinType.VAMPIRE)//Changed to wintype
                e.getWinners().add(lgp);
    }

    @EventHandler
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() == getGame())
            if ((e.getPlayer().getRoleType() == this.getType() || getVisiblePlayers().contains(e.getPlayer())) && getPlayers().contains(e.getTo()))
                e.setColorName(ChatColor.DARK_PURPLE);
    }

    @EventHandler
    public void onCustomItemChange(LGCustomItemChangeEvent e) {
        if (e.getGame() == getGame()) if (e.getPlayer().getCache().getBoolean("vampire")) e.getConstraints().add(Constraint.VAMPIRED);
    }

    public static class VampiredAction implements LGRoleActionEvent.RoleAction, TakeTarget, MessageForced
    {
        @Getter
        @Setter
        private boolean immuned, protect;
        @Getter
        @Setter
        private LGPlayer target;
        @Getter
        @Setter
        private boolean forceMessage;
        public VampiredAction(LGPlayer target) {
            this.target = target;
        }
    }
}
