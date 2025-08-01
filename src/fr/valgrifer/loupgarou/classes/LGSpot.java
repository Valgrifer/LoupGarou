package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.MainLg;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class LGSpot {
    private final LGPlayer[] spots;

    public LGSpot() {
        this.spots = new LGPlayer[size()];
    }

    public static int size() {
        return Objects.requireNonNull(MainLg.getInstance().getConfig().getList("spawns")).size();
    }

    public void set(int index, LGPlayer player) {
        if (index < 0 || index >= this.spots.length)
            throw new IndexOutOfBoundsException();

        this.spots[index] = player;
    }

    public @Nullable LGPlayer get(int index) {
        return spots[index];
    }

    public int indexOf(LGPlayer player) {
        for (int i = 0; i < this.spots.length; i++)
            if (this.spots[i] != null && this.spots[i].equals(player))
                return i;
        return -1;
    }

    public LGPlayer previousOf(int spot) {
        return previousOf(spot, null);
    }
    public LGPlayer previousOf(LGPlayer player) {
        return previousOf(this.indexOf(player), null);
    }
    public LGPlayer previousOf(LGPlayer player, @Nullable Predicate<LGPlayer> predicate) {
        return previousOf(this.indexOf(player), predicate);
    }
    public LGPlayer previousOf(int spot, @Nullable Predicate<LGPlayer> predicate) {
        for (int i = spot + 1; ; i++) {
            if (i >= this.spots.length) i = 0;
            if (i == spot)// Fait un tour complet
                break;
            LGPlayer target = get(i);
            if (target != null && !target.isDead() && (predicate == null || predicate.test(target))) {
                return target;
            }
        }
        return null;
    }

    public LGPlayer nextOf(int spot) {
        return nextOf(spot, null);
    }
    public LGPlayer nextOf(LGPlayer player) {
        return nextOf(this.indexOf(player), null);
    }
    public LGPlayer nextOf(LGPlayer player, @Nullable Predicate<LGPlayer> predicate) {
        return nextOf(this.indexOf(player), predicate);
    }
    public LGPlayer nextOf(int spot, Predicate<LGPlayer> predicate) {
        for (int i = spot - 1; ; i--) {
            if (i < 0) i = this.spots.length - 1;
            if (i == spot)// Fait un tour complet
                break;
            LGPlayer target = get(i);
            if (target != null && !target.isDead() && (predicate == null || predicate.test(target))) {
                return target;
            }
        }
        return null;
    }
}
