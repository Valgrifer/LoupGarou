package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.bukkit.Material;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ResourcePack {
    private static final Map<Material, Map<ItemBuilder, String>> customItems = new HashMap<>();
    private static final Map<String, InputStream> fileMap = new HashMap<>();
    private static ZipFile zip;
    private static ZipParameters zipParameters;
    private static boolean generated = false;

    @SuppressWarnings("UnusedReturnValue")
    public static ItemBuilder addItem(ItemBuilder item, InputStream texture) {
        return addItem(item, texture, null);
    }

    public static ItemBuilder addItem(ItemBuilder item, String model) {
        return addItem(item, null, model);
    }

    public static ItemBuilder addItem(ItemBuilder item, InputStream texture, String modelOri) {
        if (generated || item == null || item.getType() == Material.AIR || item.getCustomId() == null || getItem(item.getCustomId()) != null)
            return null;

        Map<ItemBuilder, String> map =  customItems.computeIfAbsent(item.getType(), type -> new HashMap<>());

        item.setCustomModelData(map.size() + 1);

        String texturePath = String.format("item/%s", item.getCustomId());
        String model = modelOri;
        if (model == null) model = texturePath;

        if (model.startsWith("models/")) model = model.substring("models/".length());
        if (model.endsWith(".json")) model = model.substring(0, model.length() - ".json".length());

        map.put(item.clone(), model);

        if (modelOri == null || !fileMap.containsKey(modelOri)) {
            JSONObject base = new JSONObject();
            base.put("parent", "minecraft:item/generated");
            JSONObject textures;
            base.put("textures", textures = new JSONObject());
            textures.put("layer0", "minecraft:" + texturePath);

            ResourcePack.addFile("models/" + model + ".json", VariousUtils.jsonToStream(base), true);
        }

        if (texture != null) ResourcePack.addFile("textures/" + texturePath + ".png", texture, true);

        return item;
    }

    public static ItemBuilder getItem(String id) {
        return getItem(id, null);
    }

    public static ItemBuilder getItem(String id, ItemBuilder def) {
        for (Map<ItemBuilder, String> builders : customItems.values())
            for (ItemBuilder builder : builders.keySet())
                if (builder.getCustomId().equals(id.toLowerCase())) return builder.clone();

        return def;
    }

    public static void addFile(String path, InputStream file) {
        addFile(path, file, false);
    }

    public static void addFile(String path, InputStream file, boolean force) {
        if (generated || (!force && fileMap.containsKey(path))) return;

        fileMap.put(path, file);
    }

    @SuppressWarnings({ "ResultOfMethodCallIgnored" })
    @SneakyThrows
    public static void generate(MainLg main, String path) {
        if (generated) return;
        generated = true;

        try {
            File resourcePackFile = new File(path);
            File resourcePackFolder = resourcePackFile.getParentFile();

            zip = new ZipFile(resourcePackFile);
            zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
            zipParameters.setCompressionLevel(CompressionLevel.MAXIMUM);

            MainLg.getInstance().getLogger().info("Resources Pack Generate");

            if (!resourcePackFolder.exists()) resourcePackFolder.mkdir();

            if (resourcePackFile.exists()) resourcePackFile.delete();

            Thread.sleep(500);

            JSONObject mcmeta = new JSONObject();
            JSONObject pack = new JSONObject();

//        4 dans les versions 1.13 à 1.14.4
//        5 dans les versions 1.15 à 1.16.1
//        6 dans les versions 1.16.2 à 1.16.5
//        7 dans les versions 1.17 et suivantes
            pack.put("pack_format", 6);
            pack.put("description", "§9Version : §72.0\n§6Minecraft : §71.16");

            mcmeta.put("pack", pack);
            InputStream stream = VariousUtils.jsonToStream(mcmeta);
            addStream("pack.mcmeta", stream);
            addStream("pack.png", main.getResource("pack.png"));

            fileMap.forEach((key, value) -> {
                try {
                    if (!key.startsWith("assets/")) addStream("assets/minecraft/" + key, value);
                    else addStream(key, value);
                } catch (Exception e) {
                    System.err.println(key);
                    e.printStackTrace();
                }
            });

            for (Material mat : customItems.keySet()) {
                String materialName = mat.name().toLowerCase();

                JSONObject base = new JSONObject();
                base.put("parent", "minecraft:item/generated");
                JSONObject textures;
                base.put("textures", textures = new JSONObject());
                textures.put("layer0", String.format("minecraft:item/%s", materialName));
                JSONArray overrides;
                base.put("overrides", overrides = new JSONArray());

                for (ItemBuilder item : customItems.get(mat).keySet()) {
                    String modelPath = customItems.get(mat).get(item);

                    JSONObject override = new JSONObject();
                    JSONObject predicate = new JSONObject();
                    predicate.put("custom_model_data", item.getCustomModelData());
                    override.put("predicate", predicate);
                    override.put("model", String.format("minecraft:%s", modelPath));
                    overrides.add(override);
                }

                overrides.sort(Comparator.comparing(o -> (int) ((JSONObject) ((JSONObject) o).get("predicate")).get("custom_model_data")));

                addStream(String.format("assets/minecraft/models/item/%s.json", materialName), VariousUtils.jsonToStream(base));
            }

            fileMap.clear();
            zip.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addStream(String filename, InputStream stream) {
        try {
            zipParameters.setFileNameInZip(filename);
            zip.addStream(stream, zipParameters);
            stream.close();
        } catch (IOException e) {
            System.out.println(filename);
            e.printStackTrace();
        }
    }
}
