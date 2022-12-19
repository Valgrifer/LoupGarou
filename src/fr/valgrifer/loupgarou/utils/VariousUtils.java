package fr.valgrifer.loupgarou.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.resourcepackhosting.ResourcePackHosting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class VariousUtils {
    private static String resourcePackAddress = null;
    public static String resourcePackAddress() {
        return resourcePackAddress(null);
    }
    public static String resourcePackAddress(MainLg main) {
        if(resourcePackAddress == null)
        {
            FileConfiguration config = main.getConfig();
            if(config.getBoolean("resourcepack.useResourcePackHosting", false) && main.getServer().getPluginManager().isPluginEnabled("ResourcePackHosting"))
            {
                resourcePackAddress = ResourcePackHosting.getAdresse();

                if(config.getBoolean("resourcepack.generateResourcePack", false))
                    resourcePackAddress += RandomString.generate(10);
            }
            else
                resourcePackAddress = config.getString("resourcepack.url", "http://leomelki.fr/mcgames/ressourcepacks/v32/loup_garou.zip");
        }

        return resourcePackAddress;
    }

	public static double distanceSquaredXZ(Location from, Location to) {
		return Math.pow(from.getX()-to.getX(), 2)+Math.pow(from.getZ()-to.getZ(), 2);
	}
	public static void setWarning(Player p, boolean warning) {
		PacketContainer container = new PacketContainer(PacketType.Play.Server.WORLD_BORDER);
		WorldBorder wb = p.getWorld().getWorldBorder();

		container.getWorldBorderActions().write(0, EnumWrappers.WorldBorderAction.INITIALIZE);
		
		container.getIntegers().write(0, 29999984);

		container.getDoubles().write(0, p.getLocation().getX());
		container.getDoubles().write(1, p.getLocation().getZ());

		container.getDoubles().write(3, wb.getSize());
		container.getDoubles().write(2, wb.getSize());

		container.getIntegers().write(2, (int) (warning ? wb.getSize() : wb.getWarningDistance()));
		container.getIntegers().write(1, 0);
		
		container.getLongs().write(0, (long) 0);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, container);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	private static final char[] hex = "0123456789abcdef".toCharArray();
	public static char toHex(int i) {
		return hex[i];
	}

    public static int MinMax(int value, int min, int max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static InputStream jsonToStream(JSONObject value)
    {
        return stringToStream(JSONValue.toJSONString(value).replaceAll("\\\\/", "/"));
    }
    public static InputStream stringToStream(String value)
    {
        return new ByteArrayInputStream(value.getBytes());
    }

    public static String streamToString(InputStream inputStream)
    {
        StringBuilder result = new StringBuilder();

        try
        {
            int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            while (inputStream.read(buffer) != -1)
            {
                for(byte b : buffer)
                {
                    if(b == 0) continue;
                    result.append((char) b);
                }

                buffer = new byte[BUFFER_SIZE];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
