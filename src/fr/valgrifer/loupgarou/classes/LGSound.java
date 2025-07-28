package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.MainLg;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class LGSound {
    @Getter
    private static final ArrayList<LGSound> values = new ArrayList<>();
    public static final LGSound KILL = register("KILL", MainLg.class.getClassLoader().getResourceAsStream("assets/sounds/kill.ogg"),
        SoundCategory.PLAYERS);
    public static final LGSound START_NIGHT = register("START_NIGHT",
        MainLg.class.getClassLoader().getResourceAsStream("assets/sounds/start_night.ogg"), SoundCategory.AMBIENT);
    public static final LGSound START_DAY = register("START_DAY", MainLg.class.getClassLoader().getResourceAsStream("assets/sounds/start_day.ogg"),
        SoundCategory.AMBIENT);
    public static final LGSound AMBIENT_NIGHT = register("AMBIENT_NIGHT",
        MainLg.class.getClassLoader().getResourceAsStream("assets/sounds/ambient_night.ogg"), SoundCategory.AMBIENT);
    public static final LGSound AMBIENT_DAY = register("AMBIENT_DAY",
        MainLg.class.getClassLoader().getResourceAsStream("assets/sounds/ambient_day.ogg"), SoundCategory.AMBIENT);
    public static final LGSound Button = LGSound.register("BUTTON", Sound.UI_BUTTON_CLICK, SoundCategory.MASTER);
    @Getter
    private final String name;
    @Getter
    private final String id;
    @Getter
    private final Sound sound;
    @Getter
    private final SoundCategory category;
    @Getter
    private final boolean isResourcePack;
    private LGSound(String name, Sound sound, SoundCategory category) {
        this.name = name.replaceAll("[^\\w]", "");

        this.sound = sound;
        this.category = category;
        this.id = sound.getKey().toString();
        isResourcePack = false;
        values.add(this);
    }
    private LGSound(String name, InputStream file, SoundCategory category) {
        this.name = name.replaceAll("[^\\w]", "");

        String id = name.toLowerCase();

        this.sound = null;
        this.category = category;
        this.id = "minecraft:" + name.toLowerCase();
        isResourcePack = true;
        ResourcePack.addFile(String.format("sounds/%s.ogg", id), file, true);
        values.add(this);
    }

    public static LGSound register(String name, Sound s, SoundCategory c) {
        LGSound sound;
        if ((sound = getSound(name)) != null) return sound;
        return new LGSound(name, s, c);
    }

    public static LGSound register(String name, InputStream file, SoundCategory c) {
        LGSound sound;
        if ((sound = getSound(name)) != null) return sound;
        return new LGSound(name, file, c);
    }

    public static LGSound getSound(String name) {
        for (LGSound sound : getValues())
            if (sound.getName().equalsIgnoreCase(name)) return sound;
        return null;
    }

    public void play(Player player, float var2, float var3) {
        if (isResourcePack()) player.playSound(player.getLocation(), getId(), getCategory(), var2, var3);
        else player.playSound(player.getLocation(), getSound(), getCategory(), var2, var3);
    }

    public void stop(Player player) {
        if (isResourcePack()) player.stopSound(getId(), getCategory());
        else player.stopSound(getSound(), getCategory());
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LGSound that = (LGSound) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
