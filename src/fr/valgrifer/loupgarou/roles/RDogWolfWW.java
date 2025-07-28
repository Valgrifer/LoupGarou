package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RDogWolfWW extends Role {
    public RDogWolfWW(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Chien-Loup";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getScoreBoardName() {
        return RDogWolf._getScoreBoardName();
    }

    public static String _getShortDescription() {
        return RDogWolf._getShortDescription();
    }

    public static String _getDescription() {
        return RDogWolf._getDescription();
    }

    public static String _getTask() {
        return RDogWolf._getTask();
    }

    public static String _getBroadcastedTask() {
        return RDogWolf._getBroadcastedTask();
    }

    public static RoleType _getType() {
        return RoleType.LOUP_GAROU;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.LOUP_GAROU;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public void join(LGPlayer player, boolean sendMessage) {
        super.join(player, sendMessage);
        player.setRole(this);
        RWereWolf lgs = RWereWolf.forceJoin(player);
        LGCardItems.updateItem(player);
        if (lgs != null) for (LGPlayer lgp : lgs.getPlayers())
            if (lgp != player) lgp.sendMessage(GRAY + BOLD + player.getName() + GOLD + " a rejoint les " + RED + BOLD + "Loups-Garous" + GOLD + ".");
    }
}
