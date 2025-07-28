package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGDayStartEvent;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGNightStart;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RHunter extends Role {
    private final List<LGPlayer> needToPlay = new ArrayList<>();

    public RHunter(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.VILLAGER;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.VILLAGE;
    }

    public static String _getName() {
        return GREEN + BOLD + "Chasseur";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return RVillager._getShortDescription();
    }

    public static String _getDescription() {
        return _getShortDescription() + WHITE + ". À ta mort, tu dois éliminer un joueur en utilisant ta dernière balle.";
    }

    public static String _getTask() {
        return "Tu dois choisir qui va mourir avec toi.";
    }

    public static String _getBroadcastedTask() {
        return "Le " + _getName() + BLUE + " choisit qui il va emporter avec lui.";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        getGame().wait(getTimeout(), () -> {
            this.onNightTurnTimeout(player);
            callback.run();
        }, (currentPlayer, secondsLeft) ->
            currentPlayer == player ?
                BLUE + BOLD + "C'est à ton tour !" : GOLD + "Le Chasseur choisit sa cible (" + YELLOW + secondsLeft + " s" + GOLD + ")");
        getGame().broadcastMessage(BLUE + getBroadcastedTask(), true);
        player.sendMessage(GOLD + getTask());
        //player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 60);
        player.choose((choosen) -> {
            if (choosen == null) return;

            player.stopChoosing();
            getGame().cancelWait();

            LGPlayerKilledEvent killEvent = new LGPlayerKilledEvent(getGame(), choosen, Reason.CHASSEUR);
            Bukkit.getPluginManager().callEvent(killEvent);

            if (killEvent.isCancelled()) {
                callback.run();
                return;
            }

            if (getGame().kill(killEvent.getKilled(), killEvent.getReason(), true)) return;
            callback.run();
        }, player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        getGame().broadcastMessage(BLUE + "Il n'a pas tiré sur la détente...", true);
        player.stopChoosing();
    }

    @EventHandler
    public void onPlayerKill(LGPlayerKilledEvent e) {
        if (e.getKilled().getRole() == this && e.getReason() != Reason.DISCONNECTED && e.getKilled().isRoleActive()) needToPlay.add(e.getKilled());
    }

    @EventHandler
    public void onDayStart(LGDayStartEvent e) {
        if (e.getGame() != getGame()) return;

        if (needToPlay.size() > 0) e.setCancelled(true);

        if (!e.isCancelled()) return;
        new Runnable() {
            public void run() {
                if (needToPlay.size() == 0) {
                    e.getGame().startDay();
                    return;
                }
                LGPlayer player = needToPlay.remove(0);
                onNightTurn(player, this);
            }
        }.run();
    }

    @EventHandler
    public void onEndGame(LGGameEndEvent e) {
        if (e.getGame() != getGame()) return;

        if (needToPlay.size() > 0) e.setCancelled(true);

        if (!e.isCancelled()) return;
        new Runnable() {
            public void run() {
                if (needToPlay.size() == 0) {
                    e.getGame().checkEndGame(true);
                    return;
                }
                LGPlayer player = needToPlay.remove(0);
                onNightTurn(player, this);
            }
        }.run();
    }
	
/*	Deprecated by #onDayStart(LGDayStartEvent)
 * 
 * @EventHandler
	public void onVote(LGVoteEvent e) {
		if(e.getGame() == getGame()) {
			if(needToPlay.size() > 0) {
				e.setCancelled(true);
				new Runnable() {
					public void run() {
						if(needToPlay.size() == 0) {
							e.getGame().nextNight();
							return;
						}
						LGPlayer player = needToPlay.remove(0);
						onNightTurn(player, this);
					}
				}.run();
			}
		}
	}*/

    @EventHandler
    public void onNight(LGNightStart e) {
        if (e.getGame() == getGame() && !e.isCancelled()) {
            if (needToPlay.size() > 0) {
                e.setCancelled(true);
                new Runnable() {
                    public void run() {
                        if (needToPlay.size() == 0) {
                            e.getGame().nextNight();
                            return;
                        }
                        LGPlayer player = needToPlay.remove(0);
                        onNightTurn(player, this);
                    }
                }.run();
            }
        }
    }
}
