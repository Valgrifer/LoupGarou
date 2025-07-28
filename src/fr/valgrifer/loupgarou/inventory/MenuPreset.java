package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.classes.LGSound;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class MenuPreset {
    public static final Slot lockSlot = new Slot(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" "));
    private static final ItemBuilder baseBackButton = ItemBuilder.make(Material.ARROW).setCustomId("ac_back");
    protected final Slot[] content;
    @Getter
    private final LGInventoryHolder holder;
    private final Map<String, ClickAction> itemActions = new HashMap<>();
    private final Slot defaultFill;
    private final int maxLine;

    public MenuPreset() {
        this(lockSlot);
    }

    public MenuPreset(Slot defaultFill) {
        this(null, defaultFill);
    }

    public MenuPreset(int line) {
        this(line, lockSlot);
    }

    public MenuPreset(LGInventoryHolder holder) {
        this(holder, lockSlot);
    }

    public MenuPreset(LGInventoryHolder holder, Slot defaultFill) {
        this.holder = holder;
        content = new Slot[holder != null ? holder.getMaxSlot() : 54];
        Arrays.fill(content, defaultFill);
        this.defaultFill = defaultFill;

        this.maxLine = (holder != null ? holder.getMaxLine() - 1 : 5);

        preset();
    }

    public MenuPreset(int line, Slot defaultFill) {
        this.maxLine = VariousUtils.MinMax(line, 1, 5);
        this.holder = null;
        content = new Slot[this.maxLine * 9];
        this.defaultFill = defaultFill;
        Arrays.fill(content, this.defaultFill);

        preset();
    }

    public static ItemBuilder baseBackButton() {
        return baseBackButton.clone();
    }

    public MenuPreset clone(LGInventoryHolder holder) {
        MenuPreset nmp = new MenuPreset(holder, null) {
            @Override
            protected void preset() { }
        };

        Arrays.setAll(nmp.content, i -> i < this.content.length ? this.content[i] : defaultFill);
        nmp.itemActions.putAll(itemActions);

        return nmp;
    }

    protected void putAction(String id, ClickAction action) {
        itemActions.put(id, action);
    }

    public void setSlot(int indexX, int indexY, Slot item) {
        setSlot(indexX, indexY, item, null);
    }

    public void setSlot(int indexX, int indexY, Slot item, ClickAction action) {
        if (indexX < 0 || indexX > 8) throw new RuntimeException("indexX is not between 0 & 8");
        if (indexY < 0 || indexY > this.maxLine) throw new RuntimeException("indexX is not between 0 & " + this.maxLine);

        int index = indexY * 9 + indexX;

        setSlot(index, item, action);
    }

    public void setSlot(int index, Slot item) {
        setSlot(index, item, null);
    }

    public void setSlot(int index, Slot item, ClickAction action) {
        this.content[index] = item;

        String customId = item.getDefaultItem().getCustomId();
        if (customId != null && action != null) putAction(customId, action);
    }

    public Slot getSlot(int indexX, int indexY) {
        return getSlot(indexY * 9 + indexX);
    }

    public Slot getSlot(int index) {
        return content[index];
    }

    public void swapSlot(int indexX1, int indexY1, int indexX2, int indexY2) {
        swapSlot(indexY1 * 9 + indexX1, indexY2 * 9 + indexX2);
    }

    public void swapSlot(int index1, int index2) {
        Slot slot1 = getSlot(index1);
        setSlot(index1, getSlot(index2));
        setSlot(index2, slot1);
    }

    public void action(String customId, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        try {
            ClickAction action;
            if ((action = itemActions.get(customId)) != null) {
                getHolder().getInventory()
                    .getViewers()
                    .forEach(he -> LGSound.Button.play((Player) he, he.getUniqueId().equals(player.getUniqueId()) ? .1f : .05f,
                        he.getUniqueId().equals(player.getUniqueId()) ? 1f : .75f));
                action.run(getHolder(), event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void preset();

    public void apply() {
        if (holder == null) return;

        holder.getInventory().setContents(Arrays.stream(content).map(slot -> {
            try {
                if (slot == null) return null;
                ItemBuilder item;
                if ((item = slot.getItem(holder)) == null) return null;
                return item.build();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).toArray(ItemStack[]::new));
    }

    public boolean autoUpdate() {
        return false;
    }

    public static class Slot {
        private final ItemBuilder defaultItem;

        public Slot(ItemBuilder defaultItem) {
            this.defaultItem = defaultItem;
        }

        public ItemBuilder getDefaultItem() {
            return this.defaultItem.clone();
        }

        public ItemBuilder getItem(LGInventoryHolder holder) {
            return this.defaultItem.clone();
        }
    }
}