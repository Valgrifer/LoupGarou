package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.AbilityConsume;
import fr.valgrifer.loupgarou.events.LGRoleActionEvent;
import fr.valgrifer.loupgarou.events.MessageForced;
import fr.valgrifer.loupgarou.events.TakeTarget;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RFox extends Role {
    private static final String powerLostKey = "foxPowerLost";

    public RFox(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Renard";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu peux sentir trois personnes pour savoir si un " + RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE + " se cache parmi elles. Si tu ne trouves pas de loup, tu ne pourras plus renifler, mais tu pourras innocenter trois personnes.";
    }

    public static String _getTask() {
        return "Tu peux choisir un joueur et ses deux voisins pour les renifler.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " s’apprête à renifler trois personnes...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        if (player.getCache().getBoolean(powerLostKey))
            return false;

        player.showView();

        player.choose(choosen -> {
            if (choosen == null || choosen == player) return;

            player.stopChoosing();
            player.hideView();

            int index = choosen.getSpot();

            Set<SniffAction> targets = new HashSet<>(Arrays.asList(this.getGame().getSpots().previousOf(index), choosen, this.getGame().getSpots().nextOf(index)))
                                            .stream()
                                            .map(SniffAction::new)
                                            .collect(Collectors.toSet());

            targets.forEach(action -> {
                LGRoleActionEvent event = new LGRoleActionEvent(this.getGame(), action, player);
                Bukkit.getPluginManager().callEvent(event);
            });

            String ppl = VariousUtils.frenchFormatList(targets.stream().map(action -> action.getTarget().getName()).collect(Collectors.toList()), "et");

            boolean foundLG = targets.stream().anyMatch(action -> action.isLG() || action.isForceMessage());

            if (foundLG)
                player.sendMessage(GOLD + String.format("Un %s se cache parmi %s.", RoleType.LOUP_GAROU.getColoredName(BOLD) + GOLD, ppl));
            else
                player.sendMessage(GOLD + String.format("Il n’y a pas de %s parmi %s.", RoleType.LOUP_GAROU.getColoredName(BOLD) + GOLD, ppl));

            if (!foundLG || targets.stream().anyMatch(SniffAction::isForceConsume)) {
                player.getCache().set(powerLostKey, true);
                player.sendMessage(RED + "Malheureusement, par cette action, vous perdez votre pouvoir.");
            }

            callback.run();
        });

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
        player.hideView();
    }

    @Getter
    @Setter
    public static class SniffAction implements LGRoleActionEvent.RoleAction, TakeTarget, Cancellable, AbilityConsume, MessageForced
    {
        private boolean cancelled;
        private LGPlayer target;
        private RoleType roleType;
        private boolean forceMessage;
        private boolean forceConsume = false;
        public SniffAction(LGPlayer target) {
            this.target = target;
            this.roleType = target.getRoleType();
        }

        private boolean isLG() {
            if (this.isCancelled())
                return false;

            return this.getRoleType() == RoleType.LOUP_GAROU;
        }
    }
}
