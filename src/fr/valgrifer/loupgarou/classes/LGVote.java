package fr.valgrifer.loupgarou.classes;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import fr.valgrifer.loupgarou.MainLg;
import fr.valgrifer.loupgarou.classes.LGPlayer.LGChooseCallback;
import fr.valgrifer.loupgarou.events.LGVoteLeaderChange;
import fr.valgrifer.loupgarou.events.LGVoteStartEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import fr.valgrifer.loupgarou.utils.NMSUtils;
import fr.valgrifer.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.*;

public class LGVote implements Listener
{
    private static final ArmorStand eas = NMSUtils.getInstance().newArmorStand();
    private static final LGPlayer blank = new LGPlayer("Vote Blanc");
    public static final ItemBuilder itemBlankVote = ItemBuilder.make(Material.PAPER)
                                                            .setCustomId("blank_vote")
                                                            .setDisplayName(RESET + GOLD + "Vote Blanc");
    private static final int itemBlankSlot = 4;


    @Getter
    private final LGVoteCause cause;
    private final int initialTimeout, littleTimeout;
    private final LGGame game;
    private final LGGame.TextGenerator generator;
    @Getter
    private final Map<LGPlayer, List<LGPlayer>> votes = new HashMap<>();
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final boolean hideViewersMessage, randomIfEqual, blankVote;
    private final WrappedDataWatcher.WrappedDataWatcherObject invisible = new WrappedDataWatcher.WrappedDataWatcherObject(
            0,
            WrappedDataWatcher.Registry.get(Byte.class)
    );
    private final WrappedDataWatcher.WrappedDataWatcherObject noGravity = new WrappedDataWatcher.WrappedDataWatcherObject(
            5,
            WrappedDataWatcher.Registry.get(Boolean.class)
    );
    @Setter
    @Getter
    private LGPlayer choosen = null;
    private int timeout;
    private Runnable callback;
    @Getter
    private List<LGPlayer> participants, viewers;
    private int votesSize = 0;
    private LGPlayer mayor;
    private List<LGPlayer> latestTop = new ArrayList<>(), blacklisted = new ArrayList<>();
    @Getter
    private boolean mayorVote;
    private boolean ended;

    public static LGVote.Builder builder(LGGame game, LGVoteCause cause)
    {
        return new LGVote.Builder(game, cause);
    }

    private LGVote(
            @Nonnull LGGame game,
            @Nonnull LGVoteCause cause,
            int timeout,
            int littleTimeout,
            boolean hideViewersMessage,
            boolean randomIfEqual,
            boolean blankVote,
            @Nonnull LGGame.TextGenerator generator)
    {
        this.cause = cause;
        this.littleTimeout = littleTimeout;
        this.initialTimeout = timeout;
        this.timeout = timeout;
        this.game = game;
        this.generator = generator;
        this.hideViewersMessage = hideViewersMessage;
        this.blankVote = blankVote;
        this.randomIfEqual = randomIfEqual;


        Bukkit.getPluginManager().callEvent(new LGVoteStartEvent(game, this, cause));
    }

    public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback)
    {
        this.callback = callback;
        this.participants = participants;
        this.viewers = viewers;
        game.wait(timeout, this::end, generator);

        ItemStack blank = itemBlankVote.build();
        for (LGPlayer player : participants)
        {
            player.choose(getChooseCallback(player));

            player.getPlayer().getInventory().setItem(itemBlankSlot, blank);
            player.getPlayer().getInventory().setHeldItemSlot(0);
        }

        Bukkit.getPluginManager().registerEvents(this, MainLg.getInstance());
    }

    public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback, ArrayList<LGPlayer> blacklisted)
    {
        this.start(participants, viewers, callback);
        this.blacklisted = blacklisted;
    }

    public void start(List<LGPlayer> participants, List<LGPlayer> viewers, Runnable callback, LGPlayer mayor)
    {
        this.mayor = mayor;
        this.start(participants, viewers, callback);
    }

    private void end()
    {
        ended = true;
        for (LGPlayer lgp : viewers)
            showVoting(lgp, null);
        for (LGPlayer lgp : votes.keySet())
            updateVotes(lgp, true);

        List<LGPlayer> voted = votes.entrySet()
                                      .stream()
                                      .filter(entry -> votes.values().stream()
                                                               .noneMatch(list -> list.size() > entry.getValue().size()))
                                      .map(Entry::getKey)
                                       .filter(lgp -> lgp != blank)
                                       .collect(Collectors.toList());

        boolean equal = voted.size() > 1;

        choosen = voted.size() == 1 ? voted.stream().findFirst().orElse(null) : null;

        for (LGPlayer player : participants) {
            player.getCache().remove("vote");
            player.stopChoosing();
            player.getPlayer().getInventory().setItem(itemBlankSlot, null);
        }

        if (equal && mayor == null && randomIfEqual)
            choosen = voted.get(game.getRandom().nextInt(voted.size()));

        if (equal && mayor != null) {
            for (LGPlayer player : viewers)
                player.sendMessage(BLUE + "Égalité, le " + DARK_PURPLE + BOLD + "Capitaine" + BLUE + " va départager les votes.");
            mayor.sendMessage(GOLD + "Tu dois choisir qui va mourir.");

            for (int i = 0; i < voted.size(); i++)
            {
                LGPlayer lgp = voted.get(i);
                showArrow(mayor, lgp, -mayor.getPlayer().getEntityId() - i);
            }

            List<LGPlayer> blackListed = new ArrayList<>();
            for (LGPlayer player : participants)
                if (!voted.contains(player)) blackListed.add(player);
                else VariousUtils.setWarning(player.getPlayer(), true);
            mayorVote = true;
            game.wait(
                    30, () -> {
                        for (LGPlayer player : participants)
                            if (voted.contains(player))
                                VariousUtils.setWarning(player.getPlayer(), false);

                        for (int i = 0; i < voted.size(); i++)
                            showArrow(mayor, null, -mayor.getPlayer().getEntityId() - i);
                        //Choix au hasard d'un joueur si personne n'a été désigné
                        choosen = voted.get(game.getRandom().nextInt(voted.size()));
                        callback.run();
                    }, (player, secondsLeft) -> {
                        timeout = secondsLeft;
                        return
                                mayor == player ?
                                        GOLD + "Il te reste " + YELLOW + secondsLeft + " seconde" + (secondsLeft > 1 ? "s" : "") + GOLD + " pour délibérer" :
                                        GOLD + "Le " + DARK_PURPLE + BOLD + "Capitaine" + GOLD + " délibère (" + YELLOW + secondsLeft + " s" + GOLD + ")";
                    }
            );
            mayor.choose(c -> {
                if (c != null) {
                    if (blackListed.contains(c)) mayor.sendMessage(DARK_RED + ITALIC + "Ce joueur n'est pas concerné par le choix.");
                    else {
                        for (LGPlayer player : participants)
                            if (voted.contains(player)) VariousUtils.setWarning(player.getPlayer(), false);

                        for (int i = 0; i < voted.size(); i++)
                            showArrow(mayor, null, -mayor.getPlayer().getEntityId() - i);
                        game.cancelWait();
                        choosen = c;
                        callback.run();
                    }
                }
            });
        }
        else {
            game.cancelWait();
            callback.run();
        }
    }

    public LGChooseCallback getChooseCallback(LGPlayer who)
    {
        return choosen -> {
            if (choosen != null)
                vote(who, choosen);
        };
    }

    public void vote(LGPlayer voter, LGPlayer voted)
    {
        if (!participants.contains(voter) && !voter.isFakePlayer()) return;
        if (blacklisted.contains(voted))
        {
            voter.sendMessage(RED + "Vous ne pouvez pas voter pour " + GRAY + BOLD + voted.getName() + RED + ".");
            return;
        }
        if (voted == voter.getCache().get("vote")) voted = null;

        if (voted != null && voter.getPlayer() != null) votesSize++;
        if (voter.getCache().has("vote")) votesSize--;

        if (votesSize == participants.size() && game.getWaitTicks() > littleTimeout * 20)
        {
            votesSize = 999;
            game.wait(littleTimeout, initialTimeout, this::end, generator);
        }
        boolean changeVote = false;
        if (voter.getCache().has("vote"))
        {//On enlève l'ancien vote
            LGPlayer devoted = voter.getCache().get("vote");
            if (votes.containsKey(devoted))
            {
                List<LGPlayer> voters = votes.get(devoted);
                if (voters != null)
                {
                    voters.remove(voter);
                    if (voters.isEmpty()) votes.remove(devoted);
                }
            }
            voter.getCache().remove("vote");
            updateVotes(devoted);
            changeVote = true;
        }

        if (voted != null)
        {//S'il vient de voter, on ajoute le nouveau vote
            //voter.sendTitle("", GRAY+"Tu as voté pour "+GRAY+BOLD+voted.getName(), 40);
            if (votes.containsKey(voted)) votes.get(voted).add(voter);
            else votes.put(voted, new ArrayList<>(Collections.singletonList(voter)));
            voter.getCache().set("vote", voted);
            updateVotes(voted);
        }

        if (voter.getPlayer() != null)
        {
            showVoting(voter, voted);
            String message;
            if (voted == blank) {
                if (changeVote) {
                    message = GRAY + BOLD + voter.getName() + GOLD + " a changé son vote pour un vote blanc.";
                    voter.sendMessage(GOLD + "Tu as changé ton vote pour un vote blanc.");
                }
                else {
                    message = GRAY + BOLD + voter.getName() + GOLD + " a voté blanc.";
                    voter.sendMessage(GOLD + "Tu as voté blanc.");
                }
            }
            else if (voted != null) {
                if (changeVote) {
                    message = GRAY + BOLD + voter.getName() + GOLD + " a changé son vote pour " + GRAY + BOLD + voted.getName() + GOLD + ".";
                    voter.sendMessage(GOLD + "Tu as changé de vote pour " + GRAY + BOLD + voted.getName() + GOLD + ".");
                }
                else {
                    message = GRAY + BOLD + voter.getName() + GOLD + " a voté pour " + GRAY + BOLD + voted.getName() + GOLD + ".";
                    voter.sendMessage(GOLD + "Tu as voté pour " + GRAY + BOLD + voted.getName() + GOLD + ".");
                }
            }
            else
            {
                message = GRAY + BOLD + voter.getName() + GOLD + " a annulé son vote.";
                voter.sendMessage(GOLD + "Tu as annulé ton vote.");
            }

            if (!hideViewersMessage)
                for (LGPlayer player : viewers)
                    if (player != voter) player.sendMessage(message);
        }
    }

    public List<LGPlayer> getVotes(LGPlayer voted)
    {
        return votes.containsKey(voted) ? votes.get(voted) : new ArrayList<>(0);
    }

    private void updateVotes(LGPlayer voted)
    {
        updateVotes(voted, false);
    }

    private void updateVotes(LGPlayer voted, boolean kill)
    {
        Integer entityId = null;
        if (!voted.isFakePlayer())
        {
            entityId = Integer.MIN_VALUE + voted.getPlayer().getEntityId();
            WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
            destroy.setEntityIds(new int[]{ entityId });
            for (LGPlayer lgp : viewers)
                destroy.sendPacket(lgp.getPlayer());
        }

        if (!kill)
        {
            int max = 0;
            for (Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
                if (entry.getValue().size() > max) max = entry.getValue().size();
            List<LGPlayer> last = latestTop;
            latestTop = new ArrayList<>();
            for (Entry<LGPlayer, List<LGPlayer>> entry : votes.entrySet())
                if (entry.getValue().size() == max) latestTop.add(entry.getKey());
            Bukkit.getPluginManager().callEvent(new LGVoteLeaderChange(game, this, last, latestTop));
        }

        if (votes.containsKey(voted) && !kill && !voted.isFakePlayer() && entityId != null)
        {
            Location loc = voted.getPlayer().getLocation();

            WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
            spawn.setEntityID(entityId);
            spawn.setType(EntityType.DROPPED_ITEM);
            spawn.setX(loc.getX());
            spawn.setY(loc.getY() + 0.3);
            spawn.setZ(loc.getZ());

            int          votesNbr               = votes.get(voted).size();
            final int    numberOfParticipants   = participants.size();
            final double votePercentage         = ((double) votesNbr / numberOfParticipants) * 100;
            final String voteFormatedPercentage = String.format("%.0f%%", votePercentage);
            final String voteContent =
                    GOLD + BOLD + votesNbr + " / " + numberOfParticipants + YELLOW + " vote" + (votesNbr > 1 ? "s" : "") + " (" + GOLD + BOLD +
                            voteFormatedPercentage + YELLOW + ")";

            for (LGPlayer lgp : viewers)
                spawn.sendPacket(lgp.getPlayer());
            NMSUtils.getInstance().updateArmorStandNameFor(eas, entityId, voteContent, viewers);
        }
    }

    private void showVoting(LGPlayer to, LGPlayer ofWho)
    {
        int                            entityId = -to.getPlayer().getEntityId();
        WrapperPlayServerEntityDestroy destroy  = new WrapperPlayServerEntityDestroy();
        destroy.setEntityIds(new int[]{ entityId });
        destroy.sendPacket(to.getPlayer());
        if (ofWho != null && !ofWho.isFakePlayer())
        {
            WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
            spawn.setEntityID(entityId);
            spawn.setType(EntityType.DROPPED_ITEM);
            Location loc = ofWho.getPlayer().getLocation();
            spawn.setX(loc.getX());
            spawn.setY(loc.getY() + 1.3);
            spawn.setZ(loc.getZ());
            spawn.setHeadPitch(0);
            Location toLoc = to.getPlayer().getLocation();
            double   diffX = loc.getX() - toLoc.getX(), diffZ = loc.getZ() - toLoc.getZ();
            float    yaw   = 180 - ((float) Math.toDegrees(Math.atan2(diffX, diffZ)));

            spawn.setYaw(yaw);
            spawn.sendPacket(to.getPlayer());

            WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
            meta.setEntityID(entityId);
            meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte) 0x20), new WrappedWatchableObject(noGravity, true)));
            meta.sendPacket(to.getPlayer());

            WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
            look.setEntityID(entityId);
            look.setPitch(0);
            look.setYaw(yaw);
            look.sendPacket(to.getPlayer());

            new BukkitRunnable()
            {

                @Override
                public void run()
                {
                    WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
                    equip.setEntityID(entityId);
                    equip.setItem(ItemSlot.HEAD, new ItemStack(Material.EMERALD));
                    equip.sendPacket(to.getPlayer());
                }
            }.runTaskLater(MainLg.getInstance(), 2);
        }
    }

    private void showArrow(LGPlayer to, LGPlayer ofWho, int entityId)
    {
        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
        destroy.setEntityIds(new int[]{ entityId });
        destroy.sendPacket(to.getPlayer());
        if (ofWho != null && !ofWho.isFakePlayer())
        {
            WrapperPlayServerSpawnEntityLiving spawn = new WrapperPlayServerSpawnEntityLiving();
            spawn.setEntityID(entityId);
            spawn.setType(EntityType.DROPPED_ITEM);
            //spawn.setMetadata(new WrappedDataWatcher());
            Location loc = ofWho.getPlayer().getLocation();
            spawn.setX(loc.getX());
            spawn.setY(loc.getY() + 1.3);
            spawn.setZ(loc.getZ());
            spawn.setHeadPitch(0);
            Location toLoc = to.getPlayer().getLocation();
            double   diffX = loc.getX() - toLoc.getX(), diffZ = loc.getZ() - toLoc.getZ();
            float    yaw   = 180 - ((float) Math.toDegrees(Math.atan2(diffX, diffZ)));

            spawn.setYaw(yaw);
            spawn.sendPacket(to.getPlayer());

            WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
            meta.setEntityID(entityId);
            meta.setMetadata(Arrays.asList(new WrappedWatchableObject(invisible, (byte) 0x20), new WrappedWatchableObject(noGravity, true)));
            meta.sendPacket(to.getPlayer());

            WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
            look.setEntityID(entityId);
            look.setPitch(0);
            look.setYaw(yaw);
            look.sendPacket(to.getPlayer());

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
                    equip.setEntityID(entityId);
                    equip.setItem(ItemSlot.HEAD, new ItemStack(Material.EMERALD));
                    equip.sendPacket(to.getPlayer());
                }
            }.runTaskLater(MainLg.getInstance(), 2);
        }
    }

    public void remove(LGPlayer killed)
    {
        participants.remove(killed);
        if (!ended)
        {
            votes.remove(killed);
            latestTop.remove(killed);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        LGPlayer player = LGPlayer.get(event.getPlayer());

        LGGame game = player.getGame();

        if (game == null || !game.isStarted()) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if (game.getVote() != this || !this.blankVote) return;

        if (!ItemBuilder.checkId(event.getItem(), itemBlankVote.getCustomId())) return;

        game.getVote().vote(player, blank);
    }

    @ToString
    public static class Builder {
        private final LGGame game;
        private final LGVoteCause cause;
        private int timeout = 180;
        private int littleTimeout = 20;
        private boolean hideViewersMessage = false;
        private boolean randomIfEqual = false;
        private boolean blankVote = false;
        private @Nonnull LGGame.TextGenerator generator = defaultGenerator();

        public Builder(@Nonnull LGGame game, @Nonnull LGVoteCause cause)
        {
            this.game = game;
            this.cause = cause;
        }

        private LGGame.TextGenerator defaultGenerator()
        {
            return (lgp, secondsLeft) -> RED + BOLD + "Tu ne peux pas jouer";
        }

        public Builder timeout(int timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public int timeout()
        {
            return timeout;
        }

        public Builder littleTimeout(int littleTimeout)
        {
            this.littleTimeout = littleTimeout;
            return this;
        }

        public int littleTimeout()
        {
            return littleTimeout;
        }

        public Builder hideViewersMessage(boolean hideViewersMessage)
        {
            this.hideViewersMessage = hideViewersMessage;
            return this;
        }

        public boolean hideViewersMessage()
        {
            return hideViewersMessage;
        }

        public Builder randomIfEqual(boolean randomIfEqual)
        {
            this.randomIfEqual = randomIfEqual;
            return this;
        }

        public boolean randomIfEqual()
        {
            return randomIfEqual;
        }

        public Builder allowBlankVote(boolean blankVote)
        {
            this.blankVote = blankVote;
            return this;
        }

        public boolean allowBlankVote()
        {
            return blankVote;
        }

        public Builder generator(LGGame.TextGenerator generator)
        {
            if (generator != null) this.generator = generator;
            else this.generator = defaultGenerator();
            return this;
        }

        public LGGame.TextGenerator generator()
        {
            return generator;
        }

        public LGVote build()
        {
            return new LGVote(game, cause, timeout, littleTimeout, hideViewersMessage, randomIfEqual, blankVote, generator);
        }
    }
}
