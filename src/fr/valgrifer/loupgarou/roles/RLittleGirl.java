package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.Interlocutor;
import fr.valgrifer.loupgarou.classes.LGChatType;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.chat.LGChat;
import fr.valgrifer.loupgarou.events.LGNightStart;
import fr.valgrifer.loupgarou.events.LGRoleTurnEndEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class RLittleGirl extends Role implements Listener
{
    private final List<String> customNames = Stream.of("Alpha", "Glouton", "Méchant", "Burlesque", "Peureux", "Malingre", "Gentil", "Tueur", "Énervé",
        "Docteur", "Enrager", "Fou", "Pensif", "réfléchi").map(ended -> "Loup " + ended).collect(Collectors.toList());
    public boolean cryptAllName = false;

    @Getter
    private final LGChat chat;

    public RLittleGirl(LGGame game) {
        super(game);

        this.chat = new LGChat(game, LGChatType.SPY) {
            final Map<String, String> mapName = new HashMap<>();

            public String getCustomName(Interlocutor from) {
                return mapName.computeIfAbsent(from.getName(), k -> !cryptAllName || customNames.size() > mapName.size() ? customNames.get(mapName.size()) : MAGIC + "Loup 123456");
            }

            @Override
            public String receive(@Nonnull InterlocutorContext context, @Nonnull String message) {
                if (context.getChat().getType() != LGChatType.LOUP_GAROU) {
                    return null;
                }

                return RED + getCustomName(context.getInterlocutor()) + " " + GOLD + "» " + WHITE + message;
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
        return GREEN + BOLD + "Petite Fille";
    }

    public static String _getFriendlyName() {
        return "de la " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". Chaque nuit, tu peux espionner les " + RED + BOLD + "Loups" + WHITE + ".";
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

        RWereWolf wereWolf = e.getGame().getRole(RWereWolf.class);

        if (wereWolf == null) return;

        wereWolf.getChat().join(this.getChat());
    }


    @EventHandler
    public void onChangeRole(LGRoleTurnEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (e.getNewRole() instanceof RWereWolf)
            this.getPlayers()
                    .stream()
                    .filter(player -> !player.getCache().getBoolean("infected") && player.isRoleActive())
                    .forEach(player -> player.joinChat(this.getChat(), true));
        if (e.getPreviousRole() instanceof RWereWolf)
            this.getPlayers()
                    .stream()
                    .filter(player -> !player.getCache().getBoolean("infected") && player.isRoleActive())
                    .forEach(LGPlayer::leaveChat);
    }
}
