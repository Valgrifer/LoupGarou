package fr.valgrifer.loupgarou.classes;

import java.util.List;

public interface CampTeam {

    List<LGPlayer> getPlayers();

    List<LGPlayer> getHiddenPlayers();

    boolean addHiddenPlayer(LGPlayer player);

    boolean removeHiddenPlayer(LGPlayer player);

    boolean addAllHiddenPlayer(List<LGPlayer> players);

    boolean removeAllHiddenPlayer(List<LGPlayer> players);

    List<LGPlayer> getFakePlayers();

    boolean addFakePlayer(LGPlayer player);

    boolean removeFakePlayer(LGPlayer player);

    boolean addAllFakePlayer(List<LGPlayer> players);

    boolean removeAllFakePlayer(List<LGPlayer> players);

    /**
     * getPlayers + getHiddenPlayers - getFakePlayers
     *
     * @return List<LGPlayer>
     */
    List<LGPlayer> getVisiblePlayers();
}
