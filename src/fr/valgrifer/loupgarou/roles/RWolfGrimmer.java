package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import fr.valgrifer.loupgarou.events.LGDeathAnnouncementEvent;
import fr.valgrifer.loupgarou.events.LGVoteEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWolfGrimmer extends Role {
    public static final String canGrimmedKey = "can_grimmed";
    public static final String grimmedKey = "grimmed";
    public static final String grimmedStoleKey = "grimmedStole";

    public RWolfGrimmer(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Loup Grimeur";
    }

    public static String _getFriendlyName() {
        return RWereWolf._getFriendlyName();
    }

    public static String _getShortDescription() {
        return RWereWolf._getShortDescription();
    }

    public static String _getDescription() {
        return RWereWolf._getDescription() +
                       " Au vote du Village si tu fais partie des premiers à voter le condamné, celui-ci sera indiqué comme étant un " +
                       RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE + "." +
                       WHITE + "(Role info: Pour faire partie des premiers à voter, il faut prendre le nombre de participant au vote, le divisé par 5 et le résultat est la limite pour être parmi les premiers participant.)";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.LOUP_GAROU;
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        RWereWolf.forceJoin(player);
        player.getCache().set(canGrimmedKey, true);
    }

    @EventHandler
    public void onVoteEnd(LGVoteEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (e.getVote().getCause() != LGVoteCause.VILLAGE || e.getVote().getChoosen() == null) return;

        int limit = (int) Math.max(Math.floor(e.getVote().getParticipants().size() / 5.0), 1);

        LGPlayer firstVote = e.getVote().getVotes().get(e.getVote().getChoosen())
                                     .stream()
                                     .limit(limit)
                                     .filter(lgp -> getPlayers().contains(lgp) && lgp.getCache().getBoolean(canGrimmedKey))
                                     .findFirst()
                                     .orElse(null);

        if (firstVote != null) {
            firstVote.getCache().set(canGrimmedKey, false);
            e.getVote().getChoosen().getCache().set(grimmedKey, true);
//            firstVote.getCache().set(grimmedStoleKey, e.getVote().getChoosen().getRole());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeathAnnouncement(LGDeathAnnouncementEvent e) {
        if (e.getGame() != getGame())
            return;

        if (e.getKilled().getCache().getBoolean(grimmedKey))
            e.setShowedRole(RWereWolf.class);
//        if (e.getKilled().getCache().has(grimmedStoleKey))
//            e.setShowedRole((Class<? extends Role>) e.getKilled().getCache().get(grimmedStoleKey).getClass());
    }
}
