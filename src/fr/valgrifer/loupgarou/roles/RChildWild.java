package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Random;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RChildWild extends Role {
    private static final Random random = new Random();

    public RChildWild(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return GREEN + BOLD + "Enfant-Sauvage";
    }

    public static String _getScoreBoardName() {
        return GREEN + BOLD + "Enfant" + GRAY + "-" + RED + BOLD + "Sauvage";
    }

    public static String _getFriendlyName() {
        return "de l'" + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Au début de la première nuit, tu dois choisir un joueur comme modèle. S'il meurt au cours de la partie, tu deviendras un " +
            RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ".";
    }

    public static String _getTask() {
        return "Qui veux-tu prendre comme modèle ?";
    }

    public static String _getBroadcastedTask() {
        return "L'" + _getName() + BLUE + " cherche ses marques...";
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        player.showView();
        player.sendMessage(GOLD + "Choisissez votre modèle.");
        player.choose(choosen -> {
            if (choosen != null) {
                player.stopChoosing();
                player.sendMessage(
                    GOLD + "Si " + GRAY + BOLD + choosen.getName() + GOLD + " meurt, tu deviendras " + RED + BOLD + "Loup-Garou" + GOLD + ".");
                player.sendActionBarMessage(GRAY + BOLD + choosen.getName() + GOLD + " est ton modèle");
                player.getCache().set("enfant_svg", choosen);
                choosen.getCache().set("enfant_svg_d", player);
                getPlayers().remove(player);//Pour éviter qu'il puisse avoir plusieurs modèles
                player.hideView();
                callback.run();
            }
        }, player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
        LGPlayer choosen = null;
        while (choosen == null || choosen == player) choosen = getGame().getAlive().get(random.nextInt(getGame().getAlive().size()));
        player.sendMessage(GOLD + "Si " + GRAY + BOLD + choosen.getName() + GOLD + " meurt, tu deviendras " + RED + BOLD + "Loup-Garou" + GOLD + ".");
        player.sendActionBarMessage(GRAY + BOLD + choosen.getName() + GOLD + " est ton modèle");
        player.getCache().set("enfant_svg", choosen);
        choosen.getCache().set("enfant_svg_d", player);
        getPlayers().remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKilled(LGPlayerKilledEvent e) {
        if (e.getGame() == getGame()) if (e.getKilled().getCache().has("enfant_svg_d")) {
            LGPlayer enfant = e.getKilled().getCache().remove("enfant_svg_d");
            if (!enfant.isDead() && enfant.getCache().remove("enfant_svg") == e.getKilled() && enfant.isRoleActive()) {
                enfant.sendMessage(
                    GRAY + BOLD + e.getKilled().getName() + GOLD + " est mort, tu deviens un " + RED + BOLD + "Loup-Garou" + GOLD + ".");
                getGame().getRole(RChildWildWW.class, true).join(enfant, false);
            }
        }
    }

}
