package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGChatType;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGDayEndEvent;
import fr.valgrifer.loupgarou.events.LGNightStart;
import fr.valgrifer.loupgarou.events.LGPreDayStartEvent;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;

import javax.annotation.Nonnull;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RMedium extends Role {
    @Getter
    private final LGChat chat;

    public RMedium(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.MEDIUM) { // @Todo A FINIR
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                if (context.getChat() == this)
                    return RMedium.this.getName() + GOLD + " » " + WHITE + message;
                return GRAY + context.getInterlocutor().getName() + GOLD + " » " + WHITE + message;
            }
        };
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Médium";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE +
            ". Chaque nuit, tu peux communiquer avec les morts pour tenter de récupérer des informations cruciales.";
    }

    public static String _getTask() {
        return "";
    }

    public static String _getBroadcastedTask() {
        return "";
    }

    @EventHandler
    public void onLGNightStart(LGNightStart e) {
        if (e.getGame() != getGame()) return;

        if (e.getGame().getNight() != 0) return;

        this.getChat().joinChat(e.getGame().getChat(LGChatType.SPEC));
    }

    @EventHandler
    public void onNight(LGDayEndEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getPlayers()) {
            lgp.sendMessage(DARK_GRAY + ITALIC + "Tu entres en contact avec le monde des morts...");
            joinChat(lgp);
        }
    }


    private void joinChat(LGPlayer lgp) {
        lgp.joinChat(this.getChat());
    }

    @EventHandler
    public void onRoleTurn(LGRoleTurnEndEvent e) {
        if (e.getGame() == getGame())
            if (e.getPreviousRole() instanceof RWereWolf)
                for (LGPlayer lgp : getPlayers())
                    if (lgp.getChat() != getGame().getChat(LGChatType.SPEC) && lgp.isRoleActive()) {
                        lgp.sendMessage(GOLD + ITALIC + "Tu peux de nouveau parler aux morts...");
                        joinChat(lgp);
                    }
    }

    @EventHandler
    public void onDay(LGPreDayStartEvent e) {
        if (e.getGame() == getGame())
            for (LGPlayer lgp : getPlayers())
                if (lgp.isRoleActive())
                    lgp.leaveChat();
    }
}
