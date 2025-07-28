package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;

public class LGCustomItemChangeEvent extends LGEvent {
    @Getter
    private final LGPlayer player;
    @Getter
    private final LGCardItems.CardModifier constraints;

    public LGCustomItemChangeEvent(LGGame game, LGPlayer player, LGCardItems.CardModifier constraints) {
        super(game);
        this.player = player;
        this.constraints = constraints;
    }
}
