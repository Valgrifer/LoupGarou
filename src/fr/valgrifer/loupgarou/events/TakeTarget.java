package fr.valgrifer.loupgarou.events;

import fr.valgrifer.loupgarou.classes.LGPlayer;

public interface TakeTarget {
    LGPlayer getTarget();

    void setTarget(LGPlayer value);
}
