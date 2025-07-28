package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class HiddenRole extends Role {

    private HiddenRole(LGGame game) throws Exception {
        super(game);
        throw new Exception("This class can not be created");
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.NONE;
    }

    public static String _getName() {
        return GRAY + MAGIC + BOLD + "Hidden";
    }

    public static String _getFriendlyName() {
        return _getName();
    }

    public static String _getShortDescription() {
        return "";
    }

    public static String _getDescription() {
        return "";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }
}