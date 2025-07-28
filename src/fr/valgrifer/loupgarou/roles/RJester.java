package fr.valgrifer.loupgarou.roles;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGGameEndEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings("unused")
public class RJester extends Role {
    private final ArrayList<LGPlayer> needToPlay = new ArrayList<>();

    public RJester(LGGame game) {
        super(game);
    }

    public static RoleType _getType() {
        return RoleType.NEUTRAL;
    }

    public static RoleWinType _getWinType() {
        return RoleWinType.NONE;
    }

    public static String _getName() {
        return LIGHT_PURPLE + BOLD + "Bouffon";
    }

    public static String _getFriendlyName() {
        return "du " + _getName();
    }

    public static String _getShortDescription() {
        return WHITE + "Tu gagnes si tu remplis ton objectif";
    }

    public static String _getDescription() {
        return WHITE + "Tu es " + RoleType.NEUTRAL.getColoredName(LIGHT_PURPLE, BOLD) + WHITE +
            " et tu gagnes si tu remplis ton objectif. Ton objectif est d'être éliminé par le village lors de n’importe quel vote de jour. Si tu réussis, tu gagnes la partie, mais celle-ci continue. Tu pourras tuer l'une des personnes qui t'ont condamné.";
    }

    public static String _getTask() {
        return "Choisis quelqu’un à hanter parmi ceux qui ont voté pour ta mort.";
    }

    public static String _getBroadcastedTask() {
        return "L'esprit vengeur du " + _getName() + BLUE + " rôde sur le village...";
    }

    @Override
    public int getTimeout() {
        return 15;
    }

    public void onNightTurn(Runnable callback) {
        ArrayList<LGPlayer> players = (ArrayList<LGPlayer>) needToPlay.clone();
        new Runnable() {
            @Override
            public void run() {
                getGame().cancelWait();
                if (players.size() == 0) {
                    onTurnFinish(callback);
                    return;
                }
                LGPlayer player = players.remove(0);
                getGame().wait(getTimeout(), () -> {
                    RJester.this.onNightTurnTimeout(player);
                    this.run();
                }, (currentPlayer, secondsLeft) ->
                    currentPlayer == player ?
                        BLUE + BOLD + "C'est à ton tour !" :
                        GOLD + "C'est au tour " + getFriendlyName() + " " + GOLD + "(" + YELLOW + secondsLeft + " s" + GOLD + ")");
                player.sendMessage(GOLD + getTask());
                //	player.sendTitle(GOLD+"C'est à vous de jouer", GREEN+getTask(), 100);
                onNightTurn(player, this);
            }
        }.run();
    }

    public boolean hasPlayersLeft() {
        return needToPlay.size() > 0;
    }

    @Override
    protected boolean onNightTurn(LGPlayer player, Runnable callback) {
        needToPlay.remove(player);
        player.showView();
        player.getCache().set("bouffon_win", true);
        List<LGPlayer> choosable = getGame().getVote().getVotes(player);
        StringJoiner sj = new StringJoiner(GOLD + ITALIC + ", " + GOLD + ITALIC + BOLD);
        for (LGPlayer lgp : choosable)
            if (lgp.getPlayer() != null && lgp != player) sj.add(lgp.getName());

        String toPut = sj.toString();
        if (toPut.isEmpty()) player.sendMessage(GOLD + ITALIC + BOLD + "Personne" + GOLD + ITALIC + " n'a voté pour toi.");
        else player.sendMessage(GOLD + ITALIC + BOLD + toPut + GOLD + ITALIC + " " + (toPut.contains(",") ? "ont" : "a") + " voté pour toi.");

        player.choose((choosen) -> {
            if (choosen == null) return;

            if (!choosable.contains(choosen)) player.sendMessage(GRAY + BOLD + choosen.getName() + DARK_RED + " n'a pas voté pour vous.");
            else if (choosen.isDead()) player.sendMessage(GRAY + BOLD + choosen.getName() + DARK_RED + " est mort.");//fix
            else {
                player.stopChoosing();
                player.sendMessage(GOLD + "Ton fantôme va hanter l'esprit de " + GRAY + BOLD + choosen.getName() + GOLD + ".");
                getGame().kill(choosen, Reason.BOUFFON);
                player.hideView();
                callback.run();
            }
        }, player);

        return true;
    }

    @Override
    protected void onNightTurnTimeout(LGPlayer player) {
        player.stopChoosing();
    }

    @EventHandler
    public void onPlayerKill(LGPlayerKilledEvent e) {
        if (e.getKilled().getRole() == this && e.getReason() == Reason.VOTE && e.getKilled().isRoleActive()) {
            needToPlay.add(e.getKilled());
            getGame().broadcastMessage(BLUE + ITALIC + "Quelle erreur, le " + getName() + BLUE + ITALIC + " aura droit à sa vengeance...", true);
            e.getKilled().sendMessage(GOLD + "Tu as rempli ta mission, l'heure de la vengeance a sonné.");
        }
    }

    @EventHandler
    public void onWin(LGGameEndEvent e) {
        if (e.getGame() == getGame()) for (LGPlayer lgp : getGame().getInGame())
            if (lgp.getRole() == this && lgp.getCache().getBoolean("bouffon_win")) {
                e.getWinners().add(lgp);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        getGame().broadcastMessage(GOLD + ITALIC + "Le " + getName() + GOLD + ITALIC + " a rempli son objectif.", true);
                    }
                }.runTaskAsynchronously(MainLg.getInstance());
            }
    }
}
