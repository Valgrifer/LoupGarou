package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LoupGarouListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() == null ||
            !(event.getClickedInventory().getHolder() instanceof LGInventoryHolder)) return;

        ((LGInventoryHolder) event.getClickedInventory().getHolder()).onClick(event);
    }
}
