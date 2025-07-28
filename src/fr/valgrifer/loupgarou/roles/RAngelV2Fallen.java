package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGUpdatePrefixEvent;
import fr.valgrifer.loupgarou.events.LGVoteEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RAngelV2Fallen extends Role {

    private final List<LGPlayer> winners = new ArrayList<>();

    public RAngelV2Fallen(LGGame game) {
        super(game);
        this.setFakeTimer(true);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.SOLO;
    }

    public static String _getName() {
        return RED + BOLD + "Ange Déchu";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getScoreBoardName();
    }

    public static String _getScoreBoardName() {
        return RAngelV2._getScoreBoardName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes si tu remplis ton objectif";
    }

    public static String _getDescription() {
        return WHITE + "Tu es " + RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD) + WHITE + " et tu gagnes si tu remplis ton objectif. " +
            "Tu auras une cible à tuer en " + RED + BOLD + "Ange Déchu" + WHITE + ". " + "Vous aurez une vie supplémentaire contre les " +
            RoleWinType.LOUP_GAROU.getColoredName(BOLD) + WHITE + " pour réussir votre mission. " + "En " + RED + BOLD + "Ange Déchu" + WHITE +
            ", ton objectif est de tuer ta cible, pour cela tu devras être le premier à la voter durant le vote du " +
            RoleType.VILLAGER.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Choisis ton Role";
    }

    public static String _getBroadcastedTask() {
        return "L'" + _getScoreBoardName() + BLUE + " réfléchit.";
    }

    @Override
    public int getTimeout() {
        return 20;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdatePrefix(LGUpdatePrefixEvent e) {
        if (e.getGame() == getGame()) if (e.getTo().getRole() instanceof RAngelV2Fallen && e.getTo().getCache().has(RAngelV2.TargetKey) &&
            e.getTo().getCache().<LGPlayer> get(RAngelV2.TargetKey).equals(e.getPlayer())) e.setPrefix(RED + "⌖ " + WHITE + e.getPrefix());
    }

    @EventHandler
    public void onVoteEnd(LGVoteEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (e.getVote().getChoosen() == null) return;

        LGPlayer firstVote = e.getVote().getVotes().get(e.getVote().getChoosen()).get(0);

        if (getPlayers().contains(firstVote)) winners.add(firstVote);
    }

    @EventHandler
    public void onGameEnd(LGGameEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (!winners.isEmpty()) new BukkitRunnable() {
            @Override
            public void run() {
                getGame().broadcastMessage(GOLD + ITALIC + "L'" + getName() + GOLD + ITALIC + " a rempli son objectif.", true);
            }
        }.runTaskAsynchronously(MainLg.getInstance());

        e.getWinners().addAll(winners);
    }
}
