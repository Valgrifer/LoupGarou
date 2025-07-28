package fr.valgrifer.loupgarou.utils;

import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound;
import fr.valgrifer.loupgarou.utils.nms.nbt.NBTList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class NMSUtils {
    private static String version = null;
    private static NMSUtils instance = null;

    public static String getBukkitVersion() {
        if (version == null) version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        return version;
    }

    @SneakyThrows
    public static NMSUtils getInstance() {
        if (NMSUtils.instance == null) NMSUtils.instance = (NMSUtils) Class.forName(
            "fr.valgrifer.loupgarou.utils.nms." + getBukkitVersion() + "." + NMSUtils.class.getSimpleName()).getConstructor().newInstance();

        return NMSUtils.instance;
    }

    public abstract void sendRespawn(Player player);

    public abstract ArmorStand newArmorStand();

    public abstract void updateArmorStandNameFor(ArmorStand as, int entityId, String name, List<LGPlayer> lgps);

    public abstract NBTCompound getItemTag(ItemStack itemStack);

    public abstract ItemStack setItemTag(ItemStack itemStack, NBTCompound nbt);

    public abstract NBTCompound newNBTCompound();

    public abstract NBTList newNBTList();
}
