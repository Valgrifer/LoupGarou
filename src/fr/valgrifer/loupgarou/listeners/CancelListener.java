package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CancelListener implements Listener {
    @EventHandler
    public void onPluie(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        LGPlayer lgp = LGPlayer.get(e.getPlayer());
        if (lgp.getGame() != null && lgp.getGame().isStarted() && e.getFrom().distanceSquared(Objects.requireNonNull(e.getTo())) > 0.001)
            e.setTo(e.getFrom());
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        e.setFoodLevel(6);
    }


    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(e.getPlayer().getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerDeathEvent e) {
        e.setDeathMessage("");
        e.setKeepInventory(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) e.setCancelled(true);
    }


    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        if (!e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
            if (LGPlayer.get((Player) e.getWhoClicked()).getGame() != null) e.setCancelled(true);
    }

    @EventHandler
    public void onClickInventory(PlayerSwapHandItemsEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) if (LGPlayer.get(e.getPlayer()).getGame() != null) e.setCancelled(true);
    }


    @EventHandler
    public void onRecipe(PlayerRecipeDiscoverEvent e) {
        e.setCancelled(true);
        e.getPlayer().undiscoverRecipes(e.getPlayer().getDiscoveredRecipes());
    }

    @EventHandler
    public void onRecipe(PrepareItemCraftEvent e) {
        e.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onRecipe(CraftItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onRecipe(FurnaceBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onRecipe(FurnaceSmeltEvent e) {
        e.setCancelled(true);
    }
}
