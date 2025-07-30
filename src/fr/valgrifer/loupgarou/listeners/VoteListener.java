package fr.valgrifer.loupgarou.listeners;

import fr.valgrifer.loupgarou.classes.LGGame;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.classes.LGVote;
import fr.valgrifer.loupgarou.inventory.ItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

public class VoteListener implements Listener {

    @EventHandler
    public void onClick(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING)
            return;

        if (ItemBuilder.checkId(e.getPlayer().getInventory().getItemInMainHand(), LGVote.getItemBlankVote().getCustomId()))
            return;

        LGPlayer lgp = LGPlayer.get(e.getPlayer());

        if (!lgp.isCanChoose()) {
            e.setCancelled(true);
            return;
        }

        lgp.chooseAction();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        LGPlayer player = LGPlayer.get(event.getPlayer());

        LGGame game = player.getGame();

        if (game == null || !game.isStarted() || game.getVote() == null) return;

        if (!player.isCanChoose())
            return;

        if (!game.getVote().isAllowBlankVote()) return;

        if (!ItemBuilder.checkId(event.getItem(), LGVote.getItemBlankVote().getCustomId())) return;

        event.setCancelled(true);

        game.getVote().vote(player, LGVote.getBlank());
    }
}
