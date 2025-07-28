package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGNightPlayerPreKilledEvent extends LGEvent implements Cancellable {
    @Getter
    private final LGPlayer killed;
    @Getter
    @Setter
    boolean cancelled;
    @Getter
    @Setter
    private LGPlayerKilledEvent.Reason reason;
    public LGNightPlayerPreKilledEvent(LGGame game, LGPlayer killed, LGPlayerKilledEvent.Reason reason) {
        super(game);
        this.killed = killed;
        this.reason = reason;
    }

}
