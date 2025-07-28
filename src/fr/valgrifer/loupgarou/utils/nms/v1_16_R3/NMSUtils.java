package fr.valgrifer.loupgarou.utils.nms.v1_16_R3;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.utils.nms.v1_16_R3.nbt.NBTCompound;
import fr.valgrifer.loupgarou.utils.nms.v1_16_R3.nbt.NBTList;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class NMSUtils extends fr.valgrifer.loupgarou.utils.NMSUtils {
    private static DataWatcherObject<Optional<IChatBaseComponent>> aq;
    private static DataWatcherObject<Boolean> ar;
    private static DataWatcherObject<Byte> S;

    static {
        try {
            Field f = Entity.class.getDeclaredField("aq");
            f.setAccessible(true);
            aq = (DataWatcherObject<Optional<IChatBaseComponent>>) f.get(null);
            f = Entity.class.getDeclaredField("ar");
            f.setAccessible(true);
            ar = (DataWatcherObject<Boolean>) f.get(null);
            f = Entity.class.getDeclaredField("S");
            f.setAccessible(true);
            S = (DataWatcherObject<Byte>) f.get(null);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void sendRespawn(Player player) {
        EntityPlayer cp = ((CraftPlayer) player).getHandle();
        World w = cp.getWorld();
        //Pour qu'il voit son skin changer (sa main et en f5), on lui dit qu'il respawn (alors qu'il n'est pas mort mais ça marche quand même mdr)
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(w.getDimensionManager(), w.getDimensionKey(), 0, EnumGamemode.ADVENTURE,
            EnumGamemode.ADVENTURE, false, false, false);
        cp.playerConnection.sendPacket(respawn);
    }

    @Override
    public ArmorStand newArmorStand() {
        return new CraftArmorStand((CraftServer) Bukkit.getServer(),
            new EntityArmorStand(((CraftWorld) Bukkit.getWorlds().get(0)).getHandle(), 0, 0, 0));
    }

    @Override
    public void updateArmorStandNameFor(ArmorStand as, int entityId, String name, List<LGPlayer> lgps) {
        EntityArmorStand eas = ((CraftArmorStand) as).getHandle();
        DataWatcher datawatcher = new DataWatcher(eas);
        datawatcher.register(S, (byte) 0x20);
        datawatcher.register(aq, Optional.ofNullable(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + name + "\"}")));
        datawatcher.register(ar, true);
        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(entityId, datawatcher, true);

        for (LGPlayer lgp : lgps)
            ((CraftPlayer) lgp.getPlayer()).getHandle().playerConnection.sendPacket(meta);
    }

    @Override
    public NBTCompound getItemTag(ItemStack itemStack) {
        @Nullable NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        if (tag == null) return null;
        return new NBTCompound(tag);
    }

    @Override
    public ItemStack setItemTag(ItemStack itemStack, fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound nbt) {
        net.minecraft.server.v1_16_R3.ItemStack item = CraftItemStack.asNMSCopy(itemStack);

        item.setTag((NBTTagCompound) nbt.getHandler());

        return CraftItemStack.asBukkitCopy(item);
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound newNBTCompound() {
        return new NBTCompound();
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.v1_16_R3.nbt.NBTList newNBTList() {
        return new NBTList();
    }
}
