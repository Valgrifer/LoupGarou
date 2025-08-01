package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGChatType;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RTwinGirls extends Role implements Listener
{
    @Getter
    private final LGChat chat;

    public RTwinGirls(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.TWIN_GIRLS) {
            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                return DARK_PURPLE + context.getInterlocutor().getName() + " " + GOLD + "» " + WHITE + message;
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
        return GREEN + BOLD + "Jumelles";
    }

    public static String _getFriendlyName() {
        return "de la " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu peux discuter avec ta soeur, si elle est toujours en vie.";
    }

    @EventHandler
    public void onChangeRole(LGRoleTurnEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (this.getPlayers().size() <= 1)
            return;

        if (e.getNewRole() instanceof RWereWolf)
            this.getPlayers()
                    .stream()
                    .filter(LGPlayer::isRoleActive)
                    .forEach(LGPlayer::leaveChat);
        else
            this.getPlayers()
                    .stream()
                    .filter(LGPlayer::isRoleActive)
                    .forEach(player -> player.joinChat(this.getChat()));
    }
}
