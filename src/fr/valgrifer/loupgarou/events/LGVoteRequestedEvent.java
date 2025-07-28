package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class LGVoteRequestedEvent extends LGEvent implements Cancellable {
    @Getter
    private final LGVoteCause cause;
    @Getter
    @Setter
    private boolean cancelled = false;
    @Getter
    @Setter
    private boolean continuePeopleVote = true;
    @Getter
    @Setter
    private boolean hideViewersMessage = false;
    public LGVoteRequestedEvent(LGGame game, LGVoteCause cause) {
        super(game);

        this.cause = cause;
    }
}
