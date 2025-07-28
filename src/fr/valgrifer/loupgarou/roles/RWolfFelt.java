package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.*;
import org.bukkit.event.EventHandler;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RWolfFelt extends Role
{
    public static final String feltKey = "feltRole";

    public RWolfFelt(LGGame game) {
        super(game);
    }

    public static String _getName() {
        return RED + BOLD + "Loup Feutré";
    }

    public static String _getFriendlyName() {
        return RWereWolf._getFriendlyName();
    }

    public static String _getShortDescription() {
        return RWereWolf._getShortDescription();
    }

    public static String _getDescription() {
        return RWereWolf._getDescription() +
            " Au début de chaque nuit, un rôle de façade te seras attribué (parmi les rôles encore présents qui ne sont pas des " + RoleType.LOUP_GAROU.getColoredName(BOLD) + WHITE + ") . Si un autre rôle tente de connaître ton rôle, celui d’apparat lui sera donné à la place.";
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
    }

    @EventHandler(ignoreCancelled = true)
    public void onNightStart(LGDayEndEvent event) {
        if (event.getGame() != this.getGame()) return;

        List<Role> roles = this.getGame().getRoles().stream().filter(role -> role.getType() != RoleType.LOUP_GAROU && role.hasPlayersLeft()).collect(Collectors.toList());

        if (roles.isEmpty())
            roles = this.getGame().getRoles().stream().filter(Role::hasPlayersLeft).collect(Collectors.toList());

        List<Role> finalRoles = roles;
        getPlayers().stream()
                .filter(lgp -> !lgp.isDead() && lgp.isRoleActive())
                .forEach(lgp -> {
                    SecureRandom rand = this.getGame().getRandom();

                    Class<? extends Role> felt = finalRoles.get(rand.nextInt(finalRoles.size())).getClass();

                    lgp.getCache().set(feltKey, felt);

                    lgp.sendMessage(BLUE + "À partir de maintenant, ton rôle de façade est \"" + Role.getName(felt) + BLUE + "\".");
                });
    }

    @EventHandler
    public void onAction(LGRoleActionEvent event) {
        if (event.getGame() != this.getGame()) return;

        if (event.isAction(RClairvoyant.LookAction.class)) {
            RClairvoyant.LookAction action = (RClairvoyant.LookAction) event.getAction();

            if (!action.getTarget().isRoleActive() || !this.getPlayers().contains(action.getTarget()))
                return;

            action.setRoleView(getFeltRole(action.getTarget()));
        }
        else if (event.isAction(RBearShowman.SniffAction.class)) {
            RBearShowman.SniffAction action = (RBearShowman.SniffAction) event.getAction();

            if (!action.getTarget().isRoleActive() || !this.getPlayers().contains(action.getTarget()))
                return;

            action.setRoleType(Role.getType(getFeltRoleClass(action.getTarget())));
        }
        else if (event.isAction(RDetective.CompareAction.class)) {
            RDetective.CompareAction action = (RDetective.CompareAction) event.getAction();

            if (action.getTarget1().isRoleActive() && this.getPlayers().contains(action.getTarget1()))
                action.setDifferentCamp(RDetective.CompareAction.isDifferentCamp(Role.getType(getFeltRoleClass(action.getTarget1())), action.getTarget2().getRoleType()));
            if (action.getTarget2().isRoleActive() && this.getPlayers().contains(action.getTarget2()))
                action.setDifferentCamp(RDetective.CompareAction.isDifferentCamp(action.getTarget1().getRoleType(), Role.getType(getFeltRoleClass(action.getTarget2()))));
        }
        else if (event.isAction(RFox.SniffAction.class)) {
            RFox.SniffAction action = (RFox.SniffAction) event.getAction();

            if (!action.getTarget().isRoleActive() || !this.getPlayers().contains(action.getTarget()))
                return;

            action.setRoleType(Role.getType(getFeltRoleClass(action.getTarget())));
        }
    }

    private Class<? extends Role> getFeltRoleClass(LGPlayer player) {
        return player.getCache().get(feltKey, RWolfFelt.class);
    }

    private Role getFeltRole(LGPlayer player) {
        return this.getGame().getRole(player.getCache().get(feltKey, RWolfFelt.class));
    }
}
