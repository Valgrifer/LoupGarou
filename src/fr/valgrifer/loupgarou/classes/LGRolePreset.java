package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import fr.valgrifer.loupgarou.listeners.JoinListener;
import fr.valgrifer.loupgarou.roles.Role;
import fr.valgrifer.loupgarou.roles.RoleType;
import fr.valgrifer.loupgarou.roles.RoleWinType;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LGRolePreset extends PaginationMapPreset<Class<? extends Role>> {
    public LGRolePreset(LGInventoryHolder holder) {
        super(holder);
    }

    @Override
    protected MenuPreset.Slot makeInfoButtonIcon() {
        return new MenuPreset.Slot(ItemBuilder.make(Material.BOOK)) {
            @Override
            public ItemBuilder getItem(LGInventoryHolder h) {
                return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1));
            }
        };
    }

    @Override
    protected ItemBuilder mapList(Class<? extends Role> clazz) {
        return Objects.requireNonNull(Role.getCard(clazz));
    }

    @Override
    protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, Class<? extends Role> clazz) { }

    private int selectedFilter = 0;
    private final List<CompoFilter> filters = Arrays.asList(
            new CompoFilter("Tous", null),
            new CompoFilter("Village", r -> Role.getType(r) == RoleType.VILLAGER),
            new CompoFilter("Loup", r -> Role.getType(r) == RoleType.LOUP_GAROU),
            new CompoFilter("Solo", r -> Role.getWinType(r) == RoleWinType.SOLO),
            new CompoFilter("Neutre", r -> Role.getType(r) == RoleType.NEUTRAL && Role.getWinType(r) != RoleWinType.SOLO),
            new CompoFilter("Vampire", r -> Role.getType(r) == RoleType.VAMPIRE)
    );

    @Override
    protected void preset() {
        setSlot(
                8, getHolder().getMaxLine() - 1, new MenuPreset.Slot(ItemBuilder.make(Material.PAPER).setCustomId("filter_compo").setDisplayName(AQUA + "Filtre:")) {
                    @Override
                    public ItemBuilder getItem(LGInventoryHolder holder) {
                        return super.getItem(holder).setLore(filters.stream()
                                                                     .map(compoFilter -> RESET + (filters.indexOf(compoFilter) == selectedFilter ? GOLD + BOLD : GRAY) + "-> " + compoFilter.name)
                                                                     .collect(Collectors.toList()));
                    }
                },
                (holder, event) -> {
                    selectedFilter = (selectedFilter + (event.isRightClick() ? -1 : 1)) % filters.size();
                    if (selectedFilter < 0)
                        selectedFilter = filters.size() - 1;
                    this.apply();
                }
        );
    }

    @Override
    public List<Class<? extends Role>> getObjects() {
        return MainLg.getInstance()
                       .getRoles()
                       .stream()
                       .filter(clazz -> {
                           CompoFilter filter = filters.get(selectedFilter);
                           return !MainLg.getInstance().getNotSelectableRoles().contains(clazz) && (filter.filter == null || filter.filter.test(clazz));
                       })
                       .collect(Collectors.toList());
    }

    @AllArgsConstructor
    private static class CompoFilter {
        @Nonnull
        String name;
        @Nullable
        Predicate<Class<? extends Role>> filter;
    }
}