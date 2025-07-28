package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.events.LGCustomItemChangeEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.roles.Role;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.WHITE;

@SuppressWarnings({ "unused" })
public class LGCardItems {
    private static final Material BASE = Material.PAPER;

    @Getter
    private static final List<String> variantMappings = new ArrayList<>();
    private static final JSONParser parser = new JSONParser();
    private static MainLg main;

    public static void registerCardTexture(String id, InputStream texture) {
        ResourcePack.addFile("textures/item/card/roles/" + id + ".png", texture);
    }

    @SneakyThrows
    public static void registerResources(MainLg main) {
        LGCardItems.main = main;
        registerVariants(new CardModifier(), -1);
        Constraint.getValues()
            .forEach(constraint -> ResourcePack.addFile("textures/item/card/overlays/" + constraint.getName() + ".png",
                main.getResource("overlays/" + constraint.getName() + ".png"), true));

        ResourcePack.addFile("textures/item/card/empty.png", main.getResource("empty.png"), true);

        ResourcePack.addFile("models/item/models/role.json", main.getResource("models/role.json"), true);

        JSONObject uniqueCard = new JSONObject();
        uniqueCard.put("parent", "minecraft:item/models/role");
        JSONObject textures;
        uniqueCard.put("textures", textures = new JSONObject());

        JSONObject finalTextures = textures;
        for (Class<? extends Role> role : main.getRoles()) {
            List<String> description = new ArrayList<>();
            String[] words = Role.getDescription(role).split(" ");
            StringBuilder line = new StringBuilder();
            int wordCounter = 0;
            for (String word : words) {
                if (wordCounter > 0) line.append(" ");
                line.append(word);
                wordCounter++;

                if (wordCounter >= 6 || line.toString().endsWith(".")) {
                    description.add(line.toString());
                    line = new StringBuilder(WHITE.toString());
                    wordCounter = 0;
                }
            }

            ItemBuilder item = ItemBuilder.make(BASE).setDisplayName(Role.getName(role)).setLore(description.toArray(new String[0]));

            for (String variant : variantMappings) {
                String roleId = Role.getId(role);
                finalTextures.clear();
                finalTextures.put("role", String.format("minecraft:item/card/roles/%s", roleId));

                CardModifier.fromKey(variant).constraintsMap.forEach(
                    (slot, constraint) -> finalTextures.put(slot.getName(), String.format("minecraft:item/card/overlays/%s", constraint.getName())));

                String path = String.format("models/item/%s/%s.json", roleId, variant);
                ResourcePack.addFile(path, VariousUtils.jsonToStream(uniqueCard), true);

                ItemBuilder card = ResourcePack.addItem(item.setCustomId(String.format("card_%s_%s", roleId, variant)), path);

                if (variant.equals("simple")) Role.setCard(role, card);
            }
        }
    }

    private static void registerVariants(CardModifier cm, int bl) {
        for (int i = 0; i < Slot.values().length; i++) {
            if (i == bl) continue;

            Slot slot = Slot.values()[i];
            List<Constraint> constraints = Constraint.values.stream()
                .filter(constraint -> constraint.getSlot().equals(slot))
                .collect(Collectors.toList());

            for (int index = -1; index < constraints.size(); index++) {
                Constraint constraint = index >= 0 ? constraints.get(index) : null;

                if (constraint != null) cm.add(constraint);
                else cm.remove(slot);

                if (variantMappings.contains(cm.toKey()) ||
                    (constraint != null && cm.constraintsMap.values().stream().anyMatch(constraint::isIncompatible))) continue;

                variantMappings.add(cm.toKey());
                registerVariants(cm.clone(), i);
            }
        }
    }

    public static ItemStack getItem(Role role) {
        ItemBuilder builder = ItemBuilder.make(BASE).setDisplayName(ChatColor.RED + "Pas de Role");
        if (role != null)
            builder.setDisplayName(role.getName()).setCustomModelData(main.getRoles().indexOf(role.getClass()) * variantMappings.size());

        return builder.build();
    }

    public static ItemStack getItem(LGPlayer player, Constraint... constraints) {
        ItemBuilder builder = ItemBuilder.make(BASE).setDisplayName(ChatColor.RED + "Pas de Role");
        if (player.getRole() == null) return builder.build();

        CardModifier cm = new CardModifier(constraints);
        LGCustomItemChangeEvent event = new LGCustomItemChangeEvent(player.getGame(), player, cm);
        Bukkit.getPluginManager().callEvent(event);

        return ResourcePack.getItem(event.getConstraints().getID(player.getRole().getClass())).build();
    }

    public static void updateItem(LGPlayer lgp, Constraint... constraints) {
        lgp.getPlayer().getInventory().setItemInOffHand(getItem(lgp, constraints));
        lgp.getPlayer().updateInventory();
    }

    @RequiredArgsConstructor
    public enum Slot {
        TOP_LEFT("top_left"), TOP_RIGHT("top_right"), BOTTOM_LEFT("bottom_left"), BOTTOM_RIGHT("bottom_right"), DEAD("rip");
        @Getter
        private final String name;
    }

    public static class CardModifier {
        private final Map<Slot, Constraint> constraintsMap = new HashMap<>();

        CardModifier(Constraint... constraints) {
            Arrays.stream(constraints).forEach(constraint -> constraintsMap.put(constraint.getSlot(), constraint));
        }

        private static CardModifier fromKey(String keys) {
            return new CardModifier(Arrays.stream(keys.replaceAll("simple", "").split("_"))
                .map(key -> key.isEmpty() ? null : Constraint.getConstraints(key))
                .filter(Objects::nonNull)
                .toArray(Constraint[]::new));
        }

        public String toKey() {
            return constraintsMap.values().stream().anyMatch(Objects::nonNull) ? constraintsMap.keySet()
                .stream()
                .map(constraintsMap::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(c -> Constraint.getValues().indexOf(c)))
                .map(Constraint::getName)
                .collect(Collectors.joining("_")) : "simple";
        }

        private String getID(Class<? extends Role> clazz) {
            return String.format("card_%s_%s", Role.getId(clazz), toKey());
        }

        public boolean has(Constraint constraint) {
            return constraint != null && constraintsMap.get(constraint.getSlot()).equals(constraint);
        }

        public Constraint get(Constraint constraint) {
            return constraintsMap.getOrDefault(constraint.getSlot(), null);
        }

        public Constraint get(Slot slot) {
            return constraintsMap.getOrDefault(slot, null);
        }

        public void add(Constraint constraint) {
            if (constraint != null && constraintsMap.values().stream().noneMatch(constraint::isIncompatible))
                constraintsMap.put(constraint.getSlot(), constraint);
        }

        public void remove(Slot slot) {
            constraintsMap.remove(slot);
        }

        public void remove(Constraint constraint) {
            constraintsMap.remove(constraint.getSlot());
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        public CardModifier clone() {
            CardModifier cm = new CardModifier();
            constraintsMap.keySet().stream().map(constraintsMap::get).filter(Objects::nonNull).forEach(cm::add);
            return cm;
        }
    }

    public static class Constraint {
        @Getter
        private static final ArrayList<Constraint> values = new ArrayList<>();
        public static final Constraint DEAD = register("dead", Slot.DEAD);
        public static final Constraint MAYOR = register("mayor", Slot.TOP_LEFT, DEAD);
        public static final Constraint INFECTED = register("infected", Slot.TOP_RIGHT);
        public static final Constraint VAMPIRED = register("vampired", Slot.BOTTOM_RIGHT);
        @Getter
        private final String name;
        @Getter
        private final Slot slot;
        @Getter
        private final List<Constraint> incompatibles;
        private Constraint(String name, Slot slot, Constraint... incompatibles) {
            this.name = name.replaceAll("\\W", "");
            this.slot = slot;
            this.incompatibles = Arrays.asList(incompatibles);

            values.add(this);
        }

        public static Constraint register(String name, Slot slot, Constraint... incompatibles) {
            Constraint cause;
            if ((cause = getConstraints(name)) != null) return cause;
            return new Constraint(name, slot, incompatibles);
        }

        public static Constraint getConstraints(String name) {
            for (Constraint cause : getValues())
                if (cause.getName().equalsIgnoreCase(name)) return cause;
            return null;
        }

        public boolean isIncompatible(Constraint constraint) {
            return constraint != null && (this.incompatibles.contains(constraint) || constraint.incompatibles.contains(this));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{name='" + name + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Constraint that = (Constraint) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
