package fr.valgrifer.loupgarou;

import com.comphenix.protocol.ProtocolLibrary;
import fr.valgrifer.loupgarou.classes.*;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.listeners.*;
import fr.valgrifer.loupgarou.roles.*;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class MainLg extends JavaPlugin {
    @Getter
    private static MainLg instance;
    @Getter
    private static int maxPlayers = 0;
    private final List<Class<? extends Role>> blacklistRoleSpec = new ArrayList<>();
    @Getter
    private List<Class<? extends Role>> roles = new ArrayList<>();
    @Getter
    private List<Class<? extends Role>> notSelectableRoles = new ArrayList<>();
    @Getter
    @Setter
    private LGGame currentGame;//Because for now, only one game will be playable on one server (flemme)

    public static void makeNewGame() {
        LGGame game = new LGGame(MainLg.getMaxPlayers());
        MainLg.getInstance().setCurrentGame(game);
    }

    @Override
    public void onLoad() {
        instance = this;
        loadRoles();
        if (!new File(getDataFolder(), "config.yml").exists()) {//Créer la config
            saveDefaultConfig();
            FileConfiguration config = getConfig();
            config.set("spawns", new ArrayList<>());
            for (Class<? extends Role> role : roles)//Nombre de participants pour chaque rôle
                config.set("role." + Role.getId(role), 0);
            saveConfig();
        }

        registerResources();
    }

    @Override
    public void onEnable() {
        roles.sort(Comparator.comparing(Role::getName));
        roles = Collections.unmodifiableList(roles);
        notSelectableRoles = Collections.unmodifiableList(notSelectableRoles);

        loadMaxPlayers();

        LGCardItems.registerResources(this);

        if (getConfig().getBoolean("resourcepack.generateResourcePack", false))
            ResourcePack.generate(this, getConfig().getString("resourcepack.path", "./resourcepack/loup_garou.zip"));

        getLogger().info("ResourcePack Url Used: " + VariousUtils.resourcePackAddress(this));

        makeNewGame();

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new CancelListener(), this);
        pm.registerEvents(new VoteListener(), this);
        pm.registerEvents(new ChatListener(), this);
        pm.registerEvents(new LoupGarouListener(), this);
        pm.registerEvents(new LoveListener(), this);

        for (Player player : Bukkit.getOnlinePlayers())
            JoinListener.addPlayer(player, false);

        if (pm.getPlugin("ProtocolLib") != null) ProtocolLibHook.hook(this);
        else pm.disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    private Location centerLocation;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lg")) {
            if (!sender.hasPermission("loupgarou.admin")) {
                sender.sendMessage(DARK_RED + "Erreur: Vous n'avez pas la permission...");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Erreur: Vous n'êtes pas un joueur");
                return true;
            }
            if (args.length >= 1 && args[0].equalsIgnoreCase("center")) {
                Player player = (Player) sender;
                this.centerLocation = player.getLocation();
                sender.sendMessage(GREEN + "Le centre a bien été pris en compte !");
                return true;
            }
            if (args.length >= 1 && args[0].equalsIgnoreCase("addspawn")) {
                if (this.centerLocation == null) {
                    sender.sendMessage(RED + "Le centre n'a pas étais définie !");
                    return true;
                }

                Player player = (Player) sender;
                Location loc = player.getLocation();

                List<Object> list = (List<Object>) getConfig().getList("spawns", new ArrayList<>());
                list.add(
                    Arrays.asList((double) loc.getBlockX(), loc.getY(), (double) loc.getBlockZ(), VariousUtils.getAngle(this.centerLocation, loc), 15D));
                getConfig().set("spawns", list);
                saveConfig();

                sender.sendMessage(GREEN + "La position a bien été ajoutée !");
                return true;
            }
            if (args.length >= 1 && args[0].equalsIgnoreCase("debug")) {
                ((Player) sender).openInventory(DebugCard.getMainDebugCard().getInventory());
                return true;
            }
            ((Player) sender).openInventory(ConfigManager.getMainConfigManager().getInventory());
            return true;
        }
        else if (command.getName().equalsIgnoreCase("spec")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(DARK_RED + "Erreur: " + RED + "Vous n'êtes pas un joueur");
                return true;
            }
            LGPlayer lgp = LGPlayer.get((Player) sender);

            if (lgp.getGame() == null) return true;

            @SuppressWarnings("ConstantValue") boolean canOpen = (!lgp.getGame().isStarted() || lgp.getGame().isStarted() && lgp.isDead()) &&
                lgp.getGame().getRoles().stream().noneMatch(rl -> blacklistRoleSpec.contains(rl.getClass()));

            if (canOpen) ((Player) sender).openInventory(SpecManager.getMainSpecManager().getInventory());
            else lgp.sendMessage(DARK_RED + "Erreur: " + RED + "La Commande vous à étais bloqué");
            return true;
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("lg") && sender.hasPermission("loupgarou.admin") && args.length == 1)
            return getStartingList(args[0], "addSpawn", "center");
        return new ArrayList<>(0);
    }

    private List<String> getStartingList(String startsWith, String... list) {
        startsWith = startsWith.toLowerCase();
        List<String> returnlist = new ArrayList<>();
        if (startsWith.isEmpty()) return Arrays.asList(list);
        for (String s : list)
            if (s.toLowerCase().startsWith(startsWith)) returnlist.add(s);
        return returnlist;
    }

    public void loadMaxPlayers() {
        int players = 0;
        for (Class<? extends Role> role : roles)
            players += getConfig().getInt("role." + Role.getId(role));
        maxPlayers = players;
    }

    public void addRole(Class<? extends Role> clazz, InputStream image) {
        addRole(clazz, image, true);
    }

    public void addRole(Class<? extends Role> clazz, InputStream image, boolean selectable) {
        String id = Role.getId(clazz);
        LGCardItems.registerCardTexture(id, image);
        this.roles.add(clazz);
        if (!selectable) this.notSelectableRoles.add(clazz);
    }

    public void addBlackListSpecRole(Class<? extends Role> clazz) {
        if (!this.blacklistRoleSpec.contains(clazz)) this.blacklistRoleSpec.add(clazz);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadRoles() {
        try {
            addRole(RWereWolf.class, getResource("roles/werewolf.png"));
            addRole(RWolfGrimmer.class, getResource("roles/werewolfgrimmer.png"));
            addRole(RBlackWerewolf.class, getResource("roles/blackwerewolf.png"));
            addRole(RGardien.class, getResource("roles/gardien.png"));
            addRole(RWitch.class, getResource("roles/witch.png"));
            addRole(RClairvoyant.class, getResource("roles/clairvoyant.png"));
            addRole(RWolfClairvoyant.class, getResource("roles/werewolfclairvoyant.png"));
            addRole(RHunter.class, getResource("roles/hunter.png"));
            addRole(RVillager.class, getResource("roles/villager.png"));
            addRole(RVillagerVillager.class, getResource("roles/villager.png"));
            addRole(RMedium.class, getResource("roles/medium.png"));
            addRole(RDictator.class, getResource("roles/dictator.png"));
            addRole(RCupid.class, getResource("roles/cupid.png"));
            addRole(RLittleGirl.class, getResource("roles/littlegirl.png"));
            addRole(RRedRidingHood.class, getResource("roles/redridinghood.png"));
            addRole(RWhiteWerewolf.class, getResource("roles/whitewerewolf.png"));
            addRole(RJester.class, getResource("roles/jester.png"));
            addRole(RAngel.class, getResource("roles/angel.png"));
            addRole(RAngelV2.class, getResource("roles/angelv2.png"));
            addRole(RAngelV2Guardian.class, getResource("roles/guardianangel.png"), false);
            addRole(RAngelV2Fallen.class, getResource("roles/fallenangel.png"), false);
            addRole(RSurvivor.class, getResource("roles/survivor.png"));
            addRole(RAssassin.class, getResource("roles/assassin.png"));
            addRole(RBigBadWolf.class, getResource("roles/bigbadwolf.png"));
            addRole(RRaven.class, getResource("roles/raven.png"));
            addRole(RDetective.class, getResource("roles/detective.png"));
            addRole(RDogWolf.class, getResource("roles/dogwolf.png"));
            addRole(RDogWolfWW.class, getResource("roles/dogwolfww.png"), false);
            addRole(RPirate.class, getResource("roles/pirate.png"));
            addRole(RPyromaniac.class, getResource("roles/pyromaniac.png"));
            addRole(RPriestess.class, getResource("roles/priestess.png"));
//            addRole(RPriest.class, getResource("roles/priest.png"));
            addRole(RReaper.class, getResource("roles/reaper.png"));
            addRole(RChildWild.class, getResource("roles/childwild.png"));
            addRole(RChildWildWW.class, getResource("roles/childwildww.png"), false);
            addRole(RBearShowman.class, getResource("roles/bearshowman.png"));
            addRole(RVampire.class, getResource("roles/vampire.png"));
            addRole(RVampireHunter.class, getResource("roles/vampirehunter.png"));
            addRole(RPsychopath.class, getResource("roles/kira.png"));
            addRole(RWolfFelt.class, getResource("roles/feltwerewolf.png"));
            addRole(RFox.class, getResource("roles/fox.png"));

            addBlackListSpecRole(RMedium.class);
            addBlackListSpecRole(RPriest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getResource(@NonNull String path) {
        return this.getClassLoader().getResourceAsStream("assets/" + path);
    }

    private void registerResources() {
        Arrays.asList("entity/steve.png", "entity/alex.png", "misc/pumpkinblur.png")
            .forEach(o -> ResourcePack.addFile("assets/minecraft/textures/" + o, getResource(o), true));

        Map<String, Material> newItem = new HashMap<>();
        newItem.put("ui_selector", Material.EMERALD);
        newItem.put("ui_validation", Material.GOLD_NUGGET);
        newItem.put("ui_cancel", Material.IRON_NUGGET);
        newItem.put("ui_potion_life", Material.PURPLE_DYE);
        newItem.put("ui_potion_death", Material.LIGHT_BLUE_DYE);
        newItem.put("ui_heart", Material.SUGAR);

        newItem.forEach((id, mat) -> ResourcePack.addItem(ItemBuilder.make(mat).setCustomId(id), getResource(String.format("item/%s.png", id))));

        JSONObject soundsJS = new JSONObject();

        for (LGSound sound : LGSound.getValues()) {
            if (!sound.isResourcePack()) continue;

            JSONObject soundJS = new JSONObject();
            soundJS.put("category", sound.getCategory().name().toLowerCase());
            JSONArray soundJSsounds = new JSONArray();
            soundJS.put("sounds", soundJSsounds);
            JSONObject soundJSsound = new JSONObject();
            soundJSsounds.add(soundJSsound);
            soundJSsound.put("name", sound.getId());
            soundJSsound.put("stream", true);

            soundsJS.put(sound.getName().toLowerCase(), soundJS);
        }

        ResourcePack.addFile("sounds.json", VariousUtils.jsonToStream(soundsJS), true);
    }
}
