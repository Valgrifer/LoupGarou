package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled())
            return;
        LGPlayer player = LGPlayer.get(e.getPlayer());
        player.onChat(e.getMessage());
        e.setCancelled(true);
    }
}
