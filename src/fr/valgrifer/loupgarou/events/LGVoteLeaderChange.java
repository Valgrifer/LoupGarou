package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVote;
import lombok.Getter;

import java.util.List;

public class LGVoteLeaderChange extends LGEvent {

    @Getter
    private final List<LGPlayer> latest, now;
    @Getter
    private final LGVote vote;
    public LGVoteLeaderChange(LGGame game, LGVote vote, List<LGPlayer> latest, List<LGPlayer> now) {
        super(game);
        this.latest = latest;
        this.now = now;
        this.vote = vote;
    }

}
