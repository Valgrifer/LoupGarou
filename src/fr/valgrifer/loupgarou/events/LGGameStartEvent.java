package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;

public class LGGameStartEvent extends LGEvent {
    public LGGameStartEvent(LGGame game) {
        super(game);
    }
}
