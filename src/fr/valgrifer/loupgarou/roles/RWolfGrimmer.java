package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDeathAnnouncementEvent;
import fr.valgrifer.loupgarou.events.LGVoteEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWolfGrimmer extends Role {
    public static final String canGrimmedKey = "can_grimmed";
    public static final String grimmedKey = "grimmed";

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
            ". Au vote du Village si vous êtes le premier à voter sur le condamné, celui-ci sera indiqué comme étant un " +
            RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ".";
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

        if (e.getVote().getChoosen() == null) return;

        LGPlayer firstVote = e.getVote().getVotes().get(e.getVote().getChoosen()).get(0);

        if (getPlayers().contains(firstVote) && firstVote.getCache().getBoolean(canGrimmedKey)) {
            firstVote.getCache().set(canGrimmedKey, false);
            e.getVote().getChoosen().getCache().set(grimmedKey, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeathAnnouncement(LGDeathAnnouncementEvent e) {
        if (e.getGame() == getGame() && e.getKilled().getCache().getBoolean(grimmedKey)) e.setShowedRole(RWereWolf.class);
    }
}
