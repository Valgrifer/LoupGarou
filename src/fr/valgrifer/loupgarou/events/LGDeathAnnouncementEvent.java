package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.roles.Role;
import lombok.Getter;
import lombok.Setter;

public class LGDeathAnnouncementEvent extends LGEvent {
    @Getter
    private final LGPlayer player;
    @Getter
    private final LGPlayerKilledEvent.Reason reason;
    @Getter
    @Setter
    private LGPlayer killed;
    @Getter
    @Setter
    private Class<? extends Role> showedRole;
    @Getter
    @Setter
    private boolean messageHidden;
    public LGDeathAnnouncementEvent(LGGame game, LGPlayer player, LGPlayer killed, LGPlayerKilledEvent.Reason reason) {
        super(game);

        this.player = player;
        this.killed = killed;
        this.showedRole = killed.getRole().getClass();
        this.reason = reason;
    }
}
