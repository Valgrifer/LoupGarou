package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GOLD;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GRAY;

public abstract class PaginationPreset extends MenuPreset {
    @Getter
    private final int maxPerPage;
    @Getter
    private final int maxPage;
    private List<Slot> registeredSlots;
    public PaginationPreset(LGInventoryHolder holder) {
        super(holder);

        if (holder.getInventory().getType() != InventoryType.CHEST) throw new RuntimeException("PaginationPreset need `InventoryType.CHEST`");
        if (holder.getMaxSlot() < 18) throw new RuntimeException("PaginationPreset need inventory with minimum 18 slots");

        //noinspection DataFlowIssue
        registeredSlots = Collections.unmodifiableList(registeredSlots);

        maxPerPage = (holder.getMaxLine() - 2) * 9;
        maxPage = Math.max((int) Math.ceil((double) registeredSlots.size() / ((holder.getMaxLine() - 2) * 9)), 1);

        setSlot(3, holder.getMaxLine() - 1, new Slot(ItemBuilder.make(Material.ARROW).setCustomId("ac_page_previous")) {
            @Override
            public ItemBuilder getItem(LGInventoryHolder holder) {
                int index = getPageIndex();
                if (index == 0) return ItemBuilder.make(Material.AIR);
                return getDefaultItem().setDisplayName(GRAY + "Go to Page " + GOLD + index);
            }
        }, (h, event) -> setPageIndex(getPageIndex() - 1));

        setSlot(4, holder.getMaxLine() - 1, makeInfoButtonIcon());

        setSlot(5, holder.getMaxLine() - 1, new Slot(ItemBuilder.make(Material.ARROW).setCustomId("ac_page_next")) {
            @Override
            public ItemBuilder getItem(LGInventoryHolder h) {
                int index = getPageIndex();
                if (index == getMaxPage() - 1) return ItemBuilder.make(Material.AIR);
                return getDefaultItem().setDisplayName(GRAY + "Go to Page " + GOLD + (index + 2));
            }
        }, (h, event) -> setPageIndex(getPageIndex() + 1));
    }

    public int getSize() {
        return registeredSlots.size();
    }

    public int getPageIndex() {
        return VariousUtils.MinMax(getHolder().getCache().get("pageIndex", 0), 0, getMaxPage() - 1);
    }

    public void setPageIndex(int index) {
        getHolder().getCache().set("pageIndex", VariousUtils.MinMax(index, 0, getMaxPage() - 1));
        apply();
    }

    protected abstract Slot makeInfoButtonIcon();

    protected void registerItem(Slot slot) {
        registerItem(slot, null);
    }

    protected void registerItem(Slot item, ClickAction action) {
        if (registeredSlots == null) registeredSlots = new ArrayList<>();
        try {
            this.registeredSlots.add(item);

            String customId = item.getDefaultItem().getCustomId();
            if (customId != null && action != null) putAction(customId, action);
        } catch (UnsupportedOperationException ignored) {
        }
    }


    public void apply() {
        if (getHolder() == null) return;

        int pageIndex = getPageIndex();

        int i = 0, offset = getMaxPerPage() * pageIndex, to = Math.min(getMaxPerPage(), registeredSlots.size() - offset);
        for (; i < to; i++)
            content[i] = registeredSlots.get(i + offset);
        for (; i < getMaxPerPage(); i++)
            content[i] = null;

        super.apply();
    }
}
