package fr.valgrifer.loupgarou.classes.config.inventory;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.config.ConfigValue;
import fr.valgrifer.loupgarou.classes.config.GamePreset;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LGGameOptions extends PaginationMapPreset<LGGameOptions.OptionItem<?>> {
    private static final List<OptionItem<?>> options = ConfigValue.getGameOptions()
                                                               .stream()
                                                               .map(cv -> {
                                                                   if (cv.optionType() == null)
                                                                       return null;

                                                                   if (Objects.equals(cv.optionType(), Boolean.TYPE))
                                                                       return new OptionItemBoolean((ConfigValue<Boolean, Boolean>) cv);

                                                                   new RuntimeException("Unsupported game options type").printStackTrace();
                                                                   return null;
                                                               })
                                                               .filter(Objects::nonNull)
                                                               .collect(Collectors.toUnmodifiableList());

    public LGGameOptions(LGInventoryHolder holder) {
        super(holder);
    }

    private static GamePreset current() {
        return MainLg.getInstance().getLgConfig().current();
    }

    @Override
    protected Slot makeInfoButtonIcon() {
        return new Slot(ItemBuilder.make(Material.BOOK)) {
            @Override
            public ItemBuilder getItem(LGInventoryHolder h) {
                return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1));
            }
        };
    }

    @Override
    protected ItemBuilder mapList(LGGameOptions.OptionItem<?> cv) {
        return cv.item();
    }

    @Override
    protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, LGGameOptions.OptionItem<?> cv) {
        cv.click(holder, event);
    }

    @Override
    public List<LGGameOptions.OptionItem<?>> getObjects() {
        return options;
    }

    public interface OptionItem<T> {
        @NotNull ConfigValue<T, T> getConfigValue();
        default @NotNull String name() {
            return getConfigValue().displayName(state());
        }
        @NotNull ItemBuilder item();
        @NotNull T state();

        void click(LGInventoryHolder holder, InventoryClickEvent event);
    }

    @Getter
    @AllArgsConstructor
    public static class OptionItemBoolean implements OptionItem<Boolean> {
        private @NotNull ConfigValue<Boolean, Boolean> configValue;

        @Override
        public @NotNull ItemBuilder item() {
            return ItemBuilder.make(state() ? Material.LIME_CONCRETE : Material.RED_CONCRETE)
                           .setCustomId("ac_" + configValue.getClass().getName().toLowerCase())
                           .setDisplayName(name());
        }

        @Override
        public @NotNull Boolean state() {
            return current().get(configValue);
        }

        @Override
        public void click(LGInventoryHolder holder, InventoryClickEvent event) {
            current().set(configValue, !state());
            MainLg.getInstance().getLgConfig().save();
            holder.reloadPreset();
        }
    }
}