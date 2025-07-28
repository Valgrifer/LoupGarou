package fr.valgrifer.loupgarou.scoreboard;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.valgrifer.loupgarou.classes.LGPlayer;
import fr.valgrifer.loupgarou.utils.RandomString;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboard {
    @Getter
    private final String name = RandomString.generate(16);
    @Getter
    private final String displayName;
    private final List<CustomScoreboardEntry> entries = new ArrayList<>();
    @Getter
    private final LGPlayer player;
    @Getter
    private boolean shown;

    public CustomScoreboard(String displayName, LGPlayer player) {
        this.displayName = displayName;
        this.player = player;

        for (int i = 15; i >= 0; i--)
            entries.add(new CustomScoreboardEntry(i, this));
    }

    public CustomScoreboardEntry getLine(int index) {
        return entries.get(index);
    }

    public void clear() {
        entries.forEach(CustomScoreboardEntry::delete);
    }

    public void setLines(String... lines) {
        int max = Math.min(lines.length, 16);
        int i = 0;
        for (; i < max; i++)
            getLine(i).setDisplayName(lines[i]);
        for (; i < 15; i++)
            getLine(i).delete();
    }

    public void show() {
        WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective();
        objective.setMode(0);
        objective.setName(name);
        objective.setDisplayName(WrappedChatComponent.fromText(displayName));
        objective.sendPacket(player.getPlayer());
        WrapperPlayServerScoreboardDisplayObjective display = new WrapperPlayServerScoreboardDisplayObjective();
        display.setPosition(1);
        display.setScoreName(name);
        display.sendPacket(player.getPlayer());
        shown = true;

        for (CustomScoreboardEntry entry : entries)
            entry.show();
    }

    public void hide() {
        WrapperPlayServerScoreboardObjective remove = new WrapperPlayServerScoreboardObjective();
        remove.setMode(1);
        remove.setName(name);
        remove.sendPacket(player.getPlayer());

        for (CustomScoreboardEntry entry : entries)
            entry.hide();

        shown = false;
    }
}
