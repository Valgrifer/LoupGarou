package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGVote;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import lombok.Getter;

public class LGVoteEndEvent extends LGEvent {
    @Getter
    private final LGVoteCause cause;
    @Getter
    private final LGVote vote;
    public LGVoteEndEvent(LGGame game, LGVote vote, LGVoteCause cause) {
        super(game);

        this.cause = cause;
        this.vote = vote;
    }
}
