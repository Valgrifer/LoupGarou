package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

public class LGUpdatePrefixEvent extends LGEvent {
    @Getter
    private final LGPlayer player, to;
    @Getter
    @Setter
    private String prefix;
    @Getter
    @Setter
    private ChatColor colorName;

    public LGUpdatePrefixEvent(LGGame game, LGPlayer player, LGPlayer to, String prefix, ChatColor colorName) {
        super(game);
        this.player = player;
        this.prefix = prefix;
        this.to = to;
        this.colorName = colorName;
    }

}
