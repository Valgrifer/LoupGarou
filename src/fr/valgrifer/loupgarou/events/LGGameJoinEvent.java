package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;

public class LGGameJoinEvent extends LGEvent {
    @Getter
    private final LGPlayer player;

    public LGGameJoinEvent(LGGame game, LGPlayer player) {
        super(game);
        this.player = player;
    }
}
