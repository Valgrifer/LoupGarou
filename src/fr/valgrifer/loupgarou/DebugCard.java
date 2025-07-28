package fr.valgrifer.loupgarou;

import fr.valgrifer.loupgarou.classes.LGCardItems;
import fr.valgrifer.loupgarou.classes.ResourcePack;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.PaginationPreset;
import fr.valgrifer.loupgarou.roles.Role;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings({ "unused" })
public class DebugCard extends LGInventoryHolder {
    private static DebugCard mainDebugCard = null;

    public DebugCard() {
        super(6, BLACK + "Lg Config");

        setDefaultPreset(new PaginationPreset(this) {
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
            protected void preset() {
                setSlot(0, getMaxLine() - 1,
                    new Slot(ItemBuilder.make(Material.LAVA_BUCKET).setCustomId("clear").setDisplayName(GOLD + "Vider la main")), (holder, event) -> {
                        event.getWhoClicked().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                        ((Player) event.getWhoClicked()).updateInventory();
                    });

                MainLg.getInstance()
                    .getRoles()
                    .forEach(role -> LGCardItems.getVariantMappings()
                        .forEach(variant -> registerItem(new Slot(ResourcePack.getItem(String.format("card_%s_%s", Role.getId(role), variant))
                            .addLore("", RESET + WHITE + variant)
                            .setCustomId("card")))));

                putAction("card", (holder, event) -> {
                    event.getWhoClicked().getInventory().setItemInOffHand(event.getCurrentItem());
                    ((Player) event.getWhoClicked()).updateInventory();
                });
            }
        });
    }

    public static DebugCard getMainDebugCard() {
        return mainDebugCard == null ? (mainDebugCard = new DebugCard()) : mainDebugCard;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("loupgarou.admin")) {
            event.getWhoClicked().sendMessage("§4Erreur: Vous n'avez pas la permission...");
            return;
        }
        super.onClick(event);
    }
}
