package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryType;

public class LGPrivateInventoryHolder extends LGInventoryHolder {
    @Getter
    private final LGPlayer player;

    public LGPrivateInventoryHolder(int line, String title, LGPlayer player) {
        super(line, title);

        this.player = player;
    }

    public LGPrivateInventoryHolder(InventoryType type, String title, LGPlayer player) {
        super(type, title);

        this.player = player;
    }
}
