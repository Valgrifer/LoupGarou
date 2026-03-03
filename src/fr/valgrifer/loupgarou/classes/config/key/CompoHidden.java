package fr.valgrifer.loupgarou.classes.config.key;

import fr.valgrifer.loupgarou.classes.config.ConfigValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.DARK_GREEN;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.DARK_RED;

public class CompoHidden extends ConfigValue.Single<Boolean> {
    public static final @NotNull CompoHidden KEY = new CompoHidden();

    @Override
    public String displayName(Boolean data) {
        return (data ? DARK_GREEN : DARK_RED) + "Composition caché";
    }

    @Override
    public List<String> lore(Boolean data) {
        return List.of(displayName(data));
    }

    @Override
    public @Nullable Class<Boolean> optionType() {
        return Boolean.TYPE;
    }

    @Override
    public @NotNull String key() {
        return "compoHidden";
    }

    @Override
    public @NotNull Boolean defaultValue() {
        return false;
    }
}
