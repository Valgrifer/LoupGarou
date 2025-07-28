package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RTipster extends Role {
    public RTipster(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Pronostiqueur";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu peux espionner un joueur et découvrir s'il est gentil ou non. Cependant, dans certaines parties, vos pronostiques ne sont pas exacts...";
    }

    public static String _getTask() {
        return "Choisis un joueur sur lequel pronostiquer.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " s'apprête à pronostiquer...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();

        player.choose(choosen -> {
            if (choosen != null && choosen != player) {
                //player.sendTitle(GOLD+"Vous avez regardé un rôle", YELLOW+BOLD+choosen.getName()+GOLD+BOLD+" est "+YELLOW+BOLD+choosen.getRole().getName(), 5*20);
                String gentilMechant = choosen.getRoleWinType() == player.getRoleWinType() ? GREEN + BOLD + "gentil" : RED + BOLD + "méchant";
                player.sendActionBarMessage(YELLOW + BOLD + choosen.getName() + GOLD + " est " + gentilMechant);
                player.sendMessage(GOLD + "Votre instinct vous dit que " + GRAY + BOLD + choosen.getName() + GOLD + " est " + gentilMechant + GOLD + ".");
                player.stopChoosing();
                player.hideView();
                callback.run();
            }
        });

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }
}
