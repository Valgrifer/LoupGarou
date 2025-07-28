package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGVote;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import lombok.Getter;

public class LGVoteStartEvent extends LGEvent {
    @Getter
    private final LGVoteCause cause;
    @Getter
    private final LGVote vote;
    public LGVoteStartEvent(LGGame game, LGVote vote, LGVoteCause cause) {
        super(game);

        this.cause = cause;
        this.vote = vote;
    }
}
