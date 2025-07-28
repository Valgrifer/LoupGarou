package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVoteCause;
import fr.valgrifer.loupgarou.events.LGVoteEndEvent;
import fr.valgrifer.loupgarou.events.LGVoteStartEvent;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static fr.valgrifer.loupgarou.utils.ChatColorQuick.GOLD;
import static fr.valgrifer.loupgarou.utils.ChatColorQuick.RESET;

public class VoteListener implements Listener {
    private static final LGPlayer blank = new LGPlayer("Vote Blanc");
    public static final ItemBuilder itemBlankVote = ItemBuilder.make(Material.PAPER)
                                                             .setCustomId("blank_vote")
                                                             .setDisplayName(RESET + GOLD + "Vote Blanc");

    @EventHandler
    public void onClick(PlayerAnimationEvent e) {
        if (e.getAnimationType() == PlayerAnimationType.ARM_SWING) LGPlayer.get(e.getPlayer()).chooseAction();

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteStart(LGVoteStartEvent event) {
        if (event.getCause() != LGVoteCause.VILLAGE)
            return;

        ItemStack blank = itemBlankVote.build();
        event.getGame().getAlive()
                .forEach(player -> {
                    player.getPlayer().getInventory().setItem(0, blank);
                    player.getPlayer().getInventory().setHeldItemSlot(4);
                });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        LGPlayer player = LGPlayer.get(event.getPlayer());

        LGGame game = player.getGame();

        if (game == null || !game.isStarted()) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        if (!ItemBuilder.checkId(event.getItem(), itemBlankVote.getCustomId())) return;

        if (game.getVote().getCause() != LGVoteCause.VILLAGE) return;

        game.getVote().vote(player, blank);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLGVoteEnd(LGVoteEndEvent event) {
        if (event.getCause() != LGVoteCause.VILLAGE) return;

        event.getGame().getAlive().forEach(player -> player.getPlayer().getInventory().setItem(0, null));

        if (event.getVote().getChoosen() != blank)
            return;

        event.getVote().setChoosen(null);
    }

}
