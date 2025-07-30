package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

public class VoteListener implements Listener {

    @EventHandler
    public void onClick(PlayerAnimationEvent e) {
        if (e.getAnimationType() == PlayerAnimationType.ARM_SWING) LGPlayer.get(e.getPlayer()).chooseAction();

    }
}
