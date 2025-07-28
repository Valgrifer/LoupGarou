package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGNightEndEvent extends LGEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled;

    public LGNightEndEvent(LGGame game) {
        super(game);
    }
}