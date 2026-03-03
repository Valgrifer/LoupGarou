package fr.valgrifer.loupgarou.classes.config.key;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.config.LgComposition;
import fr.valgrifer.loupgarou.classes.config.ConfigValue;
import fr.valgrifer.loupgarou.roles.Role;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GOLD;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GREEN;

public class Composition extends ConfigValue<Map<String, Integer>, LgComposition> {
    public static final @NotNull Composition KEY = new Composition();

    @Override
    public String displayName(LgComposition data) {
        return GOLD + "Composition";
    }

    @Override
    public List<String> lore(LgComposition data) {
        return data.getComposition()
                       .entrySet()
                       .stream()
                       .filter(entry -> entry.getValue() > 0)
                       .map(entry -> GREEN + entry.getValue() + " " + Role.getScoreBoardName(entry.getKey()))
                       .sorted()
                       .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @Nullable Class<LgComposition> optionType() {
        return null;
    }

    @Override
    public @NotNull String key() {
        return "composition";
    }

    @Override
    public @NotNull LgComposition defaultValue() {
        return new LgComposition();
    }

    @Override
    public @NotNull LgComposition serialize(@NotNull Map<String, Integer> value) {
        return new LgComposition(value.entrySet()
                                         .stream()
                                         .map(entry -> {
                                             Optional<Class<? extends Role>> role = MainLg.getInstance().getRoles().stream().filter(r -> Role.getId(r).equals(entry.getKey())).findFirst();
                                             return role.<AbstractMap.SimpleEntry<Class<? extends Role>, Integer>> map(aClass -> new AbstractMap.SimpleEntry<>(aClass, entry.getValue())).orElse(null);

                                         })
                                         .filter(Objects::nonNull)
                                         .collect(VariousUtils.toMap(HashMap::new)));
    }

    @Override
    public @NotNull Map<String, Integer> deserialize(@NotNull LgComposition value) {
        return value.getComposition()
                       .entrySet()
                       .stream()
                       .map(entry -> new AbstractMap.SimpleEntry<>(Role.getId(entry.getKey()), entry.getValue()))
                       .collect(VariousUtils.toMap(HashMap::new));
    }
}
