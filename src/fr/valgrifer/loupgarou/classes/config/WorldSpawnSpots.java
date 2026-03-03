package fr.valgrifer.loupgarou.classes.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldSpawnSpots {
    private final World world;
    private final File configFile;
    private final FileConfiguration config;

    public WorldSpawnSpots(World world) {
        this.world = world;
        this.configFile = new File(world.getWorldFolder().getAbsolutePath() + File.separator + "SpawnSpots.yml");

        this.config = new YamlConfiguration();

        try {
            if (this.configFile.exists())
                this.config.load(this.configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            if (!this.configFile.exists())
                this.configFile.createNewFile();

            this.config.save(this.configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addSpots(String key, Spot spot) {
        List<Spot> spots = this.config.getObject(key, List.class, new ArrayList<Spot>());

        spots.add(spot);

        this.config.set(key, spots);
    }

    public List<Spot> getSpots(String key) {
        return this.config.getObject(key, List.class, new ArrayList<Spot>());
    }

    public Location toLocation(Spot spot) {
        return new Location(this.world, spot.getX(), spot.getY(), spot.getZ(), spot.getYaw(), spot.getPitch());
    }

    @AllArgsConstructor
    @Getter
    public static class Spot implements ConfigurationSerializable {
        private double x;
        private double y;
        private double z;
        private float pitch;
        private float yaw;

        public Spot(@NotNull Map<String, Object> map) {
            this.x = (double) map.get("x");
            this.y = (double) map.get("y");
            this.z = (double) map.get("z");
            this.pitch = (float) (double) map.get("pitch");
            this.yaw = (float) (double) map.get("yaw");
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            return Map.of(
                    "x", x,
                    "y", y,
                    "z", z,
                    "pitch", pitch,
                    "yaw", yaw
            );
        }
    }
}
