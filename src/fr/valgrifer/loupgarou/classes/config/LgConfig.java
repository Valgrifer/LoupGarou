package fr.valgrifer.loupgarou.classes.config;

import fr.valgrifer.loupgarou.MainLg;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class LgConfig {
    private final @NotNull MainLg main;

    private final Map<Key, GamePreset> cache = new HashMap<>();

    public LgConfig(@NotNull MainLg main) {
        this.main = main;

        this.cache.computeIfAbsent(UniqueKey.KEY, this::getPreset);

        getSection("preset").getKeys(false)
                .forEach(key -> this.cache.computeIfAbsent(StringKey.of(key), this::getPreset));
    }

    public void save(){
        cache.forEach((key, value) -> this.main.getConfig().set(key.path(), value));
        this.main.saveConfig();
    }

    private ConfigurationSection getSection(String path){
        return this.main.getConfig().isConfigurationSection(path) ? Objects.requireNonNull(main.getConfig().getConfigurationSection(path)) : main.getConfig().createSection(path);
    };

    private GamePreset getPreset(Key k) {
        if (this.main.getConfig().contains(k.path()))
            return this.main.getConfig().getObject(k.path(), GamePreset.class);
        return new GamePreset();
    }

    public Resourcepack resourcepack() {
        return new Resourcepack(getSection("resourcepack"));
    }

    public @NotNull GamePreset current() {
        return this.cache.get(UniqueKey.KEY);
    }

    public void current(@NotNull GamePreset newPreset) {
        this.cache.put(UniqueKey.KEY, newPreset);
    }

    public @NotNull List<@NotNull GamePreset> presets() {
        return this.cache.entrySet()
                       .stream()
                       .filter(entry -> entry.getKey() instanceof StringKey)
                       .map(Map.Entry::getValue)
                       .collect(Collectors.toUnmodifiableList());
    }

    public @NotNull GamePreset preset(String key) {
        return this.cache.computeIfAbsent(StringKey.of(key), this::getPreset);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Resourcepack {
        private @NotNull ConfigurationSection section;

        public String url() {
            return this.section.getString("url", "https://raw.githubusercontent.com/Valgrifer/LoupGarou/master/resourcepack/loup_garou.zip");
        }

        public boolean useResourcePackHosting() {
            return this.section.getBoolean("useResourcePackHosting", false);
        }

        public boolean generateResourcePack() {
            return this.section.getBoolean("generateResourcePack", false);
        }

        public String path() {
            return this.section.getString("path", "./resourcepack/loup_garou.zip");
        }
    }

    private interface Key {
        String path();
    }

    private static class StringKey implements Key {
        static StringKey of(String key) {
            return new StringKey(key);
        }

        private final String key;
        StringKey(String key) {
            this.key = key;
        }

        public int hashCode() {
            return 17 + key.hashCode();
        }

        @Override
        public String path() {
            return "preset." + key;
        }
    }

    private static class UniqueKey implements Key {
        static final UniqueKey KEY = new UniqueKey();
        public int hashCode() {
            return 10;
        }

        @Override
        public String path() {
            return "current";
        }
    }
}
