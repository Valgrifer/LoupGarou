package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

import java.util.List;

public class LGGameEndEvent extends LGEvent implements Cancellable {
    @Getter
    private final LGWinType winType;
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    @Setter
    private List<LGPlayer> winners;

    public LGGameEndEvent(LGGame game, LGWinType winType, List<LGPlayer> winners) {
        super(game);
        this.winType = winType;
        this.winners = winners;
    }
}