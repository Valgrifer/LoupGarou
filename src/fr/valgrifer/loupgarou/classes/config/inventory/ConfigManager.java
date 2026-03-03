package fr.valgrifer.loupgarou.classes.config.inventory;

import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGWinType;
import fr.valgrifer.loupgarou.classes.config.ConfigValue;
import fr.valgrifer.loupgarou.classes.config.GamePreset;
import fr.valgrifer.loupgarou.classes.config.LgComposition;
import fr.valgrifer.loupgarou.classes.config.LgConfig;
import fr.valgrifer.loupgarou.classes.config.key.Composition;
import fr.valgrifer.loupgarou.classes.config.key.PresetName;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.inventory.LGInventoryHolder;
import fr.valgrifer.loupgarou.inventory.MenuPreset;
import fr.valgrifer.loupgarou.inventory.PaginationMapPreset;
import fr.valgrifer.loupgarou.listeners.JoinListener;
import fr.valgrifer.loupgarou.roles.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

@SuppressWarnings({ "unused" })
public class ConfigManager extends LGInventoryHolder {
    private static ConfigManager mainConfigManager = null;

    private final LgConfig lgConfig = MainLg.getInstance().getLgConfig();

    private void requestReload() {
        this.getCache().set("edited", true);
    }

    private void backAndCheckReload(LGInventoryHolder holder, InventoryClickEvent event) {
        lgConfig.current().remove(PresetName.KEY);
        this.getCache().remove("pageIndex");
        this.loadPreset("default");

        if (!MainLg.getInstance().getCurrentGame().isStarted() && this.getCache().get("edited", false)) {
            MainLg.makeNewGame();
            Bukkit.getOnlinePlayers().forEach(JoinListener::removePlayer);
            Bukkit.getOnlinePlayers().forEach(p -> JoinListener.addPlayer(p, false));
        }

        this.getCache().remove("edited");
    }

    @SuppressWarnings("SpellCheckingInspection")
    public ConfigManager() {
        super(6, BLACK + "Lg Config");

        LGGameOptions gameOptions = new LGGameOptions(this) {
            @Override
            protected void preset() {
                setSlot(
                        0,
                        getHolder().getMaxLine() - 1,
                        new MenuPreset.Slot(baseBackButton().setDisplayName(RED + "Retour")),
                        ConfigManager.this::backAndCheckReload
                );

                super.preset();
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, OptionItem<?> cv) {
                super.itemAction(holder, event, cv);
                requestReload();
            }
        };

        setDefaultPreset(new MenuPreset(this) {
            private LgComposition getComposition() {
                return lgConfig.current().get(Composition.KEY);
            }

            @Override
            protected void preset() {
                setSlot(
                        4, 0, new Slot(ItemBuilder.make(Material.PAPER).setCustomId("info_name")) {
                            @Override
                            public ItemBuilder getItem(LGInventoryHolder holder) {
                                if (!lgConfig.current().has(PresetName.KEY))
                                    return lockSlot.getDefaultItem();
                                return getDefaultItem().setDisplayName(GOLD + lgConfig.current().get(PresetName.KEY));
                            }
                        }
                );

                setSlot(
                        4, 1, new Slot(ItemBuilder.make(Material.CHEST).setCustomId("ac_compo").setDisplayName(Composition.KEY.displayName(getComposition()))) {
                            @Override
                            public ItemBuilder getItem(LGInventoryHolder holder) {
                                return getDefaultItem().setLore(Composition.KEY.lore(getComposition()));
                            }
                        }, (holder, event) -> loadPreset("editCompo")
                );

                setSlot(
                        4, 2, new Slot(ItemBuilder.make(Material.COMMAND_BLOCK)
                                               .setCustomId("ac_gameoptions")
                                               .setDisplayName(GREEN + "Options")) {
                            @Override
                            public ItemBuilder getItem(LGInventoryHolder holder) {
                                return getDefaultItem().setLore(gameOptions.getObjects().stream().map(LGGameOptions.OptionItem::name).toArray(String[]::new));
                            }
                        },
                        (holder, event) -> loadPreset("editOptions")
                );

                setSlot(
                        0, 0, new Slot(ItemBuilder.make(Material.DARK_OAK_DOOR)
                                               .setCustomId("ac_joinall")
                                               .setDisplayName(GREEN + "/lg joinall")
                                               .setLore(GRAY + "A faire après chaque édition de composition")), (holder, event) -> {
                            Bukkit.getOnlinePlayers().forEach(JoinListener::removePlayer);
                            Bukkit.getOnlinePlayers().forEach(p -> JoinListener.addPlayer(p, false));
                        }
                );


                setSlot(
                        7, 2,
                        new Slot(ItemBuilder.make(Material.ENDER_CHEST).setCustomId("ac_openpreset").setDisplayName(GREEN + "Preset")),
                        (holder, event) -> loadPreset("loadGamePreset")
                );


                setSlot(
                        1, 2, new Slot(ItemBuilder.make(Material.CLOCK).setCustomId("ac_skiptoday").setDisplayName(GREEN + "Skip au prochain jour")),
                        (holder, event) -> {
                            if (MainLg.getInstance().getCurrentGame() == null || !MainLg.getInstance().getCurrentGame().isStarted()) return;
                            MainLg.getInstance()
                                    .getCurrentGame()
                                    .broadcastMessage(DARK_GREEN + BOLD + "Le passage à la prochaine journée a été forcé !", true);
                            MainLg.getInstance().getCurrentGame().cancelWait();
                            for (LGPlayer lgp : MainLg.getInstance().getCurrentGame().getInGame())
                                lgp.stopChoosing();
                            MainLg.getInstance().getCurrentGame().endNight();
                        }
                );

                setSlot(
                        1, 3,
                        new Slot(ItemBuilder.make(Material.CLOCK).setCustomId("ac_skiptonight").setDisplayName(GREEN + "Skip à la prochaine nuit")),
                        (holder, event) -> {
                            if (MainLg.getInstance().getCurrentGame() == null || !MainLg.getInstance().getCurrentGame().isStarted()) return;
                            MainLg.getInstance()
                                    .getCurrentGame()
                                    .broadcastMessage(DARK_GREEN + BOLD + "Le passage à la prochaine nuit a été forcé !", true);
                            for (LGPlayer lgp : MainLg.getInstance().getCurrentGame().getInGame())
                                lgp.stopChoosing();
                            MainLg.getInstance().getCurrentGame().cancelWait();
                            MainLg.getInstance().getCurrentGame().nextNight();
                        }
                );


                setSlot(
                        4, 4, new Slot(
                                ItemBuilder.make(Material.LIME_CONCRETE).setCustomId("ac_gamestartstop").setDisplayName(DARK_GREEN + "Démarrer la Game")) {
                            @Override
                            public ItemBuilder getItem(LGInventoryHolder holder) {
                                ItemBuilder builder = getDefaultItem();
                                if (MainLg.getInstance().getCurrentGame().isStarted()) {
                                    builder.setType(Material.RED_CONCRETE).setDisplayName(DARK_RED + "Arrêter la Game");
                                }
                                return builder;
                            }
                        }, (holder, event) -> {
                            if (MainLg.getInstance().getCurrentGame().isStarted()) {
                                LGGame game = MainLg.getInstance().getCurrentGame();
                                game.cancelWait();
                                game.endGame(LGWinType.EQUAL);
                                game.broadcastMessage("§cLa partie a été arrêtée de force !", true);
                            }
                            else {
                                event.getWhoClicked().sendMessage("§aVous avez démarré une nouvelle partie !");
                                MainLg.getInstance().getCurrentGame().updateStart();
                                try {
                                    getInventory().getViewers().forEach(HumanEntity::closeInventory);
                                } catch (Exception ignored) { }
                            }
                        }
                );
            }

            @Override
            public boolean autoUpdate() {
                return true;
            }
        });
        savePreset(
                "editCompo", new LGRolePreset(this) {
                    @Override
                    protected MenuPreset.Slot makeInfoButtonIcon() {
                        return new MenuPreset.Slot(ItemBuilder.make(Material.BOOK)) {
                            @Override
                            public ItemBuilder getItem(LGInventoryHolder h) {
                                int roleAmount = lgConfig.current().get(Composition.KEY).size();
                                return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                               .setLore(
                                                       AQUA + BOLD + "Click Gauche " + RESET + ":" + GRAY + " Ajoute le rôle",
                                                       AQUA + BOLD + "Click Droit   " + RESET + ":" + GRAY + " Retire le rôle", " ",
                                                       RESET + GRAY + "Il y a " + GOLD + roleAmount + GRAY + " rôle" + (roleAmount > 1 ? "s" : "")
                                               );
                            }
                        };
                    }

                    @Override
                    protected ItemBuilder mapList(Class<? extends Role> clazz) {
                        int         count = lgConfig.current().get(Composition.KEY).get(clazz);
                        ItemBuilder def   = super.mapList(clazz);
                        if (count == 0) return def.setType(Material.RED_CONCRETE);
                        return def.setDisplayName(GREEN + count + " " + def.getDisplayName()).setAmount(count);
                    }

                    @Override
                    protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, Class<? extends Role> clazz) {
                        LgComposition compo  = lgConfig.current().get(Composition.KEY);

                        if (event.isLeftClick()) compo.increase(clazz);
                        if (event.isRightClick()) compo.decrease(clazz);

                        lgConfig.save();
                        requestReload();
                        holder.reloadPreset();
                    }

                    @Override
                    protected void preset() {
                        setSlot(
                                0,
                                getHolder().getMaxLine() - 1,
                                new MenuPreset.Slot(baseBackButton().setDisplayName(RED + "Retour")
                                                            .setLore(
                                                                    WHITE + "pour faire " + GRAY + UNDERLINE + "joinall" + WHITE + " en même temps que revenir à la page d'accueil"
                                                            )),
                                ConfigManager.this::backAndCheckReload
                        );

                        super.preset();
                    }
                }
        );

        savePreset("editOptions", gameOptions);

        savePreset("loadGamePreset", new PaginationMapPreset<GamePreset>(this) {
            @Override
            protected Slot makeInfoButtonIcon() {
                return new MenuPreset.Slot(ItemBuilder.make(Material.BOOK)) {
                    @Override
                    public ItemBuilder getItem(LGInventoryHolder h) {
                        return getDefaultItem().setDisplayName(GRAY + "Page " + GOLD + (getPageIndex() + 1))
                                       .setLore(AQUA + BOLD + "Click " + RESET + ":" + GRAY + " Charge le preset");
                    }
                };
            }

            @Override
            protected void preset() {
                setSlot(
                        0,
                        getHolder().getMaxLine() - 1,
                        new MenuPreset.Slot(baseBackButton().setDisplayName(RED + "Retour")),
                        (holder, event) -> holder.loadPreset("default")
                );
            }

            @Override
            protected ItemBuilder mapList(GamePreset obj) {
                List<String> lore = new ArrayList<>();

                ConfigValue.getValues()
                        .stream()
                        .filter(cv -> cv != PresetName.KEY)
                        .filter(cv -> Objects.isNull(cv.optionType()))
                        .forEach(cvo -> {
                            if(!lore.isEmpty())
                                lore.add("");

                            ConfigValue<?, Object> cv = (ConfigValue<?, Object>) cvo;

                            lore.add(GOLD + cv.displayName(obj.get(cv)) + GRAY + " :");
                            lore.addAll(cv.lore(obj.get(cv)));
                        });

                if(!lore.isEmpty())
                    lore.add("");

                lore.add(GOLD + "Options" + GRAY + " :");
                lore.addAll(
                        ConfigValue.getGameOptions()
                                .stream()
                                .map(cv -> ((ConfigValue<?, Object>) cv).displayName(obj.get((ConfigValue<?, Object>) cv)))
                                .collect(Collectors.toUnmodifiableList()));

                return ItemBuilder.make(Material.CHEST)
                               .setCustomId("ac_preset_" + PresetName.KEY.id(obj.get(PresetName.KEY)))
                               .setDisplayName(GOLD + BOLD + obj.get(PresetName.KEY))
                               .setLore(lore);
            }

            @Override
            protected void itemAction(LGInventoryHolder holder, InventoryClickEvent event, GamePreset obj) {
                event.getWhoClicked().sendMessage(GREEN + "Chargement du preset " + GOLD + BOLD + obj.get(PresetName.KEY));

                lgConfig.current(obj.clone());
                lgConfig.save();

                holder.getCache().remove("pageIndex");
                holder.loadPreset("default");

                MainLg.makeNewGame();
                Bukkit.getOnlinePlayers().forEach(JoinListener::removePlayer);
                Bukkit.getOnlinePlayers().forEach(p -> JoinListener.addPlayer(p, false));
            }

            @Override
            public List<GamePreset> getObjects() {
                return lgConfig.presets();
            }
        });
    }

    public static ConfigManager getMainConfigManager() {
        return mainConfigManager == null ? (mainConfigManager = new ConfigManager()) : mainConfigManager;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("loupgarou.admin")) {
            event.getWhoClicked().sendMessage("§4Erreur: Vous n'avez pas la permission...");
            return;
        }
        super.onClick(event);
    }
}
