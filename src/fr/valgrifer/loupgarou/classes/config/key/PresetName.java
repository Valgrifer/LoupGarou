package fr.valgrifer.loupgarou.classes.config.key;

import fr.valgrifer.loupgarou.classes.config.ConfigValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PresetName extends ConfigValue.Single<String> {
    public static final @NotNull PresetName KEY = new PresetName();

    @Override
    public String displayName(String data) {
        return "";
    }

    @Override
    public List<String> lore(String data) {
        return List.of();
    }

    @Override
    public @Nullable Class<String> optionType() {
        return null;
    }

    @Override
    public @NotNull String key() {
        return "name";
    }

    @Override
    public @NotNull String defaultValue() {
        return "null";
    }
}
