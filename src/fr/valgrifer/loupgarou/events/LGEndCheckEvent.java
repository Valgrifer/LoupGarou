package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import lombok.Getter;

import java.util.Map;

public class LGEndCheckEvent extends LGEvent {
    @Getter
    private final Map<RoleWinType, Integer> roleWinTypeAlive;
    @Getter
    private LGWinType winType;

    public LGEndCheckEvent(LGGame game, LGWinType winType, Map<RoleWinType, Integer> roleWinTypeAlive) {
        super(game);
        this.winType = winType;
        this.roleWinTypeAlive = roleWinTypeAlive;
    }

    public void setWinType(LGWinType winType) {
        this.winType = winType == null ? LGWinType.NONE : winType;
    }
}