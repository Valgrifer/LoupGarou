package fr.valgrifer.loupgarou.listeners;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGPlayerKilledEvent.Reason;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.Objects;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.RED;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        addPlayer(e.getPlayer(), true);

        e.setJoinMessage("");
    }

    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent e) {
        if (e.getStatus() == Status.SUCCESSFULLY_LOADED) {
            Player p = e.getPlayer();
            LGPlayer lgp = LGPlayer.get(p);
            lgp.showView();
            lgp.join(MainLg.getInstance().getCurrentGame());
        }
        else if (e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD)
            e.getPlayer().kickPlayer(RED + "Il vous faut le resource pack pour jouer ! (" + e.getStatus() + ")");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }


    public static void addPlayer(Player p, boolean init) {
        boolean noSpec = p.getGameMode() != GameMode.SPECTATOR;
        p.setFoodLevel(6);

        LGPlayer lgp = LGPlayer.get(p);

        if (init)
        {
            WrapperPlayServerScoreboardTeam myTeam = new WrapperPlayServerScoreboardTeam();
            myTeam.setName(p.getName());
            myTeam.setPrefix(WrappedChatComponent.fromText(""));
            myTeam.setPlayers(Collections.singletonList(p.getName()));
            myTeam.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);
            for (Player player : Bukkit.getOnlinePlayers())
                if (player != p)
                {
                    if (player.getGameMode() != GameMode.SPECTATOR) player.hidePlayer(MainLg.getInstance(), p);
                    WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
                    team.setName(player.getName());
                    team.setPrefix(WrappedChatComponent.fromText(""));
                    team.setPlayers(Collections.singletonList(player.getName()));
                    team.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);

                    team.sendPacket(p);
                    myTeam.sendPacket(player);
                }

            Objects.requireNonNull(p.getPlayer()).setResourcePack(VariousUtils.resourcePackAddress());
        }
        else lgp.join(MainLg.getInstance().getCurrentGame());

        lgp.showView();

        if (noSpec) p.setGameMode(GameMode.ADVENTURE);

        p.removePotionEffect(PotionEffectType.JUMP);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.setWalkSpeed(0.2f);
    }

    public static void removePlayer(Player p) {
        LGPlayer lgp = LGPlayer.get(p);
        if (lgp.getGame() != null) {
            lgp.leaveChat();
            if (lgp.getRole() != null && !lgp.isDead()) lgp.getGame().kill(lgp, Reason.DISCONNECTED, true);
            lgp.getGame().getInGame().remove(lgp);
            lgp.getGame().checkLeave();
        }
        LGPlayer.remove(p);
        lgp.remove();
    }
}
