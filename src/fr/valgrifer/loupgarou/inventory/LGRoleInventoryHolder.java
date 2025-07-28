package fr.valgrifer.loupgarou.inventory;

import fr.valgrifer.loupgarou.roles.Role;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryType;

public class LGRoleInventoryHolder<R extends Role> extends LGInventoryHolder {
    @Getter
    private final R role;

    public LGRoleInventoryHolder(int line, String title, R role) {
        super(line, title);

        this.role = role;
    }

    public LGRoleInventoryHolder(InventoryType type, String title, R role) {
        super(type, title);

        this.role = role;
    }
}
