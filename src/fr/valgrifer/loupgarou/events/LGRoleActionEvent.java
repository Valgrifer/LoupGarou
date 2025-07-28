package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

@Getter
public class LGRoleActionEvent extends LGEvent {
    private final RoleAction action;
    private final List<LGPlayer> players;

    public LGRoleActionEvent(@Nonnull LGGame game, @Nonnull RoleAction action, @Nonnull LGPlayer... players) {
        this(game, action, Arrays.asList(players));
    }
    public LGRoleActionEvent(@Nonnull LGGame game, @Nonnull RoleAction action, @Nonnull List<LGPlayer> players) {
        super(game);
        this.action = action;
        this.players = players;
    }

    public <A extends RoleAction> boolean isAction(@Nonnull Class<A> aClass) {
        return aClass.isInstance(this.action);
    }

    public interface RoleAction { }
}