package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;

public class LGPlayerGotKilledEvent extends LGEvent {
    @Getter
    private final boolean endGame;
    @Getter
    private final LGPlayer killed;
    @Getter
    private final LGPlayerKilledEvent.Reason reason;
    @Getter
    @Setter
    private boolean hideMessage = false;
    public LGPlayerGotKilledEvent(LGGame game, LGPlayer killed, LGPlayerKilledEvent.Reason reason, boolean endGame) {
        super(game);
        this.killed = killed;
        this.reason = reason;
        this.endGame = endGame;
    }
}
