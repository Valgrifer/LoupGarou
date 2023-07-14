package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.utils.VariableCache;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings({"unused", "NullableProblems"})
public class LGInventoryHolder implements InventoryHolder
{
    @Getter
    private final int maxLine;
    @Getter
    private final int maxSlot;
    private final Inventory inventory;
    private final Map<String, MenuPreset> presets = new HashMap<>();
    @Getter
    private final VariableCache cache = new VariableCache();

    @Getter
    private MenuPreset currentPreset = null;
    public void setDefaultPreset(MenuPreset preset)
    {
        if(!presets.containsKey("default")) {
            presets.put("default", currentPreset = preset);
            currentPreset.apply();
        }
    }
    public void savePreset(String name, MenuPreset preset)
    {
        if(!presets.containsKey(name))
            presets.put(name, preset);
    }
    public MenuPreset getPreset(String name)
    {
        return presets.getOrDefault(name, null);
    }
    public void loadPreset(String name)
    {
        if(!presets.containsKey(name))
            return;

        (currentPreset = presets.get(name)).apply();
    }
    public void reloadPreset()
    {
        if(currentPreset == null)
            return;
        currentPreset.apply();
    }

    public LGInventoryHolder(InventoryType type, String title)
    {
        maxLine = 0;
        inventory = Bukkit.createInventory(this, type, title);
        maxSlot = type.getDefaultSize();

        start();
    }
    public LGInventoryHolder(int line, String title)
    {
        maxLine = line;
        inventory = Bukkit.createInventory(this, maxSlot = 9*maxLine, title);

        start();
    }
    private void start()
    {
        new BukkitRunnable(){
            @Override
            public void run() {
                if (currentPreset != null && currentPreset.autoUpdate() && getInventory().getViewers().size() > 0)
                    currentPreset.apply();
            }
        }.runTaskTimerAsynchronously(MainLg.getInstance(), 20L, 20L);
    }

    public Inventory getInventory()
    {
        return this.inventory;
    }

    public HumanEntity getViewer()
    {
        return getInventory().getViewers().size() > 0 ? getInventory().getViewers().get(0) : null;
    }

    public void onClick(InventoryClickEvent event)
    {
        currentPreset.action(ItemBuilder.getCustomId(event.getCurrentItem()), event);
        event.setCancelled(true);
    }
}
