package fr.valgrifer.loupgarou;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.events.LGSkinLoadEvent;
import fr.valgrifer.loupgarou.events.LGUpdatePrefixEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bukkit.ChatColor.WHITE;

public class ProtocolLibHook {
    public static void hook(MainLg plugin) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_TIME) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerUpdateTime time = new WrapperPlayServerUpdateTime(event.getPacket());
                LGPlayer lgp = LGPlayer.get(event.getPlayer());
                if (lgp.getGame() != null && lgp.getGame().getTime() != time.getTimeOfDay()) event.setCancelled(true);
            }
        });
        //Éviter que les gens s'entendent quand ils se sélectionnent et qu'ils sont trop proche
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerNamedSoundEffect sound = new WrapperPlayServerNamedSoundEffect(event.getPacket());
                if (sound.getSoundEffect() == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE) event.setCancelled(true);
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                LGPlayer player = LGPlayer.get(event.getPlayer());
                WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo(event.getPacket());
                ArrayList<PlayerInfoData> datas = new ArrayList<>();
                for (PlayerInfoData data : info.getData()) {
                    LGPlayer lgp = LGPlayer.get(Bukkit.getPlayer(data.getProfile().getUUID()));
                    if (player.getGame() != null && player.getGame() == lgp.getGame()) {
                        LGUpdatePrefixEvent evt2 = new LGUpdatePrefixEvent(player.getGame(), lgp, player, "", WHITE);
                        WrappedChatComponent displayName = data.getDisplayName();
                        Bukkit.getPluginManager().callEvent(evt2);
                        if (evt2.getPrefix().length() > 0) {
                            try {
                                if (displayName != null) {
                                    JSONObject obj = (JSONObject) new JSONParser().parse(displayName.getJson());
                                    displayName = WrappedChatComponent.fromText(evt2.getPrefix() + evt2.getColorName() + obj.get("text"));
                                }
                                else
                                    displayName = WrappedChatComponent.fromText(evt2.getPrefix() + evt2.getColorName() + data.getProfile().getName());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        LGSkinLoadEvent evt = new LGSkinLoadEvent(lgp.getGame(), lgp, player, data.getProfile());
                        Bukkit.getPluginManager().callEvent(evt);
                        datas.add(new PlayerInfoData(evt.getProfile(), data.getLatency(), data.getGameMode(), displayName));
                    }
                    else datas.add(data);
                }
                info.setData(datas);
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_HEALTH) {
            @Override
            public void onPacketSending(PacketEvent event) {
                LGPlayer player = LGPlayer.get(event.getPlayer());
                if (player.getGame() != null && player.getGame().isStarted()) {
                    WrapperPlayServerUpdateHealth health = new WrapperPlayServerUpdateHealth(event.getPacket());
                    health.setFood(6);
                }
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SCOREBOARD_TEAM) {
            @Override
            public void onPacketSending(PacketEvent event) {
                LGPlayer player = LGPlayer.get(event.getPlayer());
                WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam(event.getPacket());
                Player other = Bukkit.getPlayer(team.getName());
                if (other == null) return;
                LGPlayer lgp = LGPlayer.get(other);
                if (player.getGame() != null && player.getGame() == lgp.getGame()) {
                    LGUpdatePrefixEvent evt2 = new LGUpdatePrefixEvent(player.getGame(), lgp, player, "", WHITE);
                    Bukkit.getPluginManager().callEvent(evt2);
                    team.setColor(evt2.getColorName());
                    if (evt2.getPrefix().length() > 0) team.setPrefix(WrappedChatComponent.fromText(evt2.getPrefix()));
                    else team.setPrefix(WrappedChatComponent.fromText(evt2.getColorName().toString()));
                }
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                LGPlayer player = LGPlayer.get(event.getPlayer());
                if (player.getGame() != null) {
                    WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment(event.getPacket());

                    if (equip.getEntityID() == player.getPlayer().getEntityId()) return;

                    Arrays.asList(ItemSlot.values()).forEach(itemSlot -> equip.setItem(itemSlot, new ItemStack(Material.AIR)));
                }
            }
        });
    }
}