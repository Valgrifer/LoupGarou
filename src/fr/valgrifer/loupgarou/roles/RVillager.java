package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RVillager extends Role {
    public RVillager(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Villageois";
    }

    public static String _getFriendlyName() {
        return "des " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes avec le " + RoleWinType.VILLAGE.getColoredName(BOLD);
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Tu ne disposes d'aucun pouvoir particulier, uniquement ta perspicacité et ta force de persuasion.";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }
}
