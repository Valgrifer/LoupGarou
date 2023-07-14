package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GOLD;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GRAY;

public abstract class PaginationMapPreset<T> extends MenuPreset
{
    @Getter @Setter
    private List<T> objectList = new ArrayList<>();
    public int getSize()
    {
        return objectList.size();
    }

    @Getter
    private int maxPerPage;
    @Getter
    private int maxPage;


    public PaginationMapPreset(LGInventoryHolder holder)
    {
        super(holder);

        if(holder.getInventory().getType() != InventoryType.CHEST)
            throw new RuntimeException("PaginationPreset need `InventoryType.CHEST`");
        if(holder.getMaxSlot() < 18)
            throw new RuntimeException("PaginationMapPreset need inventory with minimum 18 slots");

        setSlot(3, holder.getMaxLine()-1,
                new Slot(ItemBuilder.make(Material.ARROW)
                        .setCustomId("ac_page_previous"))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder holder) {
                        int index = getPageIndex();
                        if(index == 0)
                            return ItemBuilder.make(Material.AIR);
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Go to Page " + GOLD + index);
                    }
                },
                (h, event) -> setPageIndex(getPageIndex() - 1));

        setSlot(4, holder.getMaxLine()-1, makeInfoButtonIcon());

        setSlot(5, holder.getMaxLine()-1,
                new Slot(ItemBuilder.make(Material.ARROW)
                        .setCustomId("ac_page_next"))
                {
                    @Override
                    protected ItemBuilder getItem(LGInventoryHolder h) {
                        int index = getPageIndex();
                        if(index == getMaxPage()-1)
                            return ItemBuilder.make(Material.AIR);
                        return getDefaultItem()
                                .setDisplayName(GRAY + "Go to Page " + GOLD + (index + 2));
                    }
                },
                (h, event) -> setPageIndex(getPageIndex() + 1));
    }

    public void setPageIndex(int index)
    {
        getHolder().getCache().set("pageIndex", VariousUtils.MinMax(index, 0, getMaxPage()-1));
        apply();
    }
    public int getPageIndex()
    {
        return VariousUtils.MinMax(getHolder().getCache().get("pageIndex", 0), 0, getMaxPage()-1);
    }

    protected abstract Slot makeInfoButtonIcon();

    protected abstract ItemBuilder mapList(T obj);

    protected abstract void itemAction(LGInventoryHolder holder, InventoryClickEvent event, T obj);

    public void apply()
    {
        if(getHolder() == null)
            return;

        maxPerPage = (getHolder().getMaxLine()-2)*9;
        maxPage = (int) Math.ceil((double) objectList.size() / ((getHolder().getMaxLine()-2)*9));

        ItemBuilder[] contentBuilder = new ItemBuilder[getHolder().getMaxSlot()];

        int pageIndex = getPageIndex();

        int i = 0,
                offset = getMaxPerPage() * pageIndex,
                to = Math.min(getMaxPerPage(), objectList.size() - offset);
        for(; i < to; i++) {
            T obj = objectList.get(i + offset);
            contentBuilder[i] = mapList(obj);
            putAction(contentBuilder[i].getCustomId(), (holder, event) -> this.itemAction(holder, event, obj));
        }
        for(; i < getMaxPerPage(); i++)
            contentBuilder[i] = null;
        for(; i < getHolder().getMaxSlot(); i++)
        {
            Slot slot = content[i];
            try {
                if (slot == null)
                {
                    contentBuilder[i] = null;
                    continue;
                }
                contentBuilder[i] = slot.getItem(getHolder());
            } catch (Exception e) {
                e.printStackTrace();
                contentBuilder[i] = null;
            }
        }

        getHolder().getInventory().setContents(
                Arrays.stream(contentBuilder)
                        .map(builder -> {
                            try {
                                if(builder == null)
                                    return null;
                                return builder.build();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .toArray(ItemStack[]::new));
    }
}
