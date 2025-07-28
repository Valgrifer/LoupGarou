package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RChildWildWW extends Role {
    public RChildWildWW(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Enfant-Sauvage";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getName();
    }

    public static String _getScoreBoardName() {
        return RChildWild._getScoreBoardName();
    }

    public static String _getShortDescription() {
        return RChildWild._getShortDescription();
    }

    public static String _getDescription() {
        return RChildWild._getDescription();
    }

    public static String _getTask() {
        return RChildWild._getTask();
    }

    public static String _getBroadcastedTask() {
        return RChildWild._getBroadcastedTask();
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
