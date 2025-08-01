package fr.valgrifer.loupgarou.classes;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class LGChatType {
    @Getter
    private static final ArrayList<LGChatType> values = new ArrayList<>();

    public static final LGChatType VILLAGE = new LGChatType("Village");
    public static final LGChatType SPEC = new LGChatType("Spectateur");
    public static final LGChatType NOCHAT = new LGChatType("NoChat");
    public static final LGChatType LOUP_GAROU = new LGChatType("Loup Garou");
    public static final LGChatType LITTLE_GIRL = new LGChatType("Petite Fille");
    public static final LGChatType LELOUCH = new LGChatType("Lelouch");
    public static final LGChatType LOVE = new LGChatType("LOVE");
    public static final LGChatType TWIN_GIRLS = new LGChatType("Jumelles");
    public static final LGChatType MEDIUM = new LGChatType("Médium");
    public static final LGChatType VAMPIRE = new LGChatType("Vampire");

    public static LGChatType register(String name) {
        LGChatType type;
        if ((type = getChatType(name)) != null) return type;
        return new LGChatType(name);
    }

    private static LGChatType getChatType(String name) {
        for (LGChatType type : getValues())
            if (type.getName().equalsIgnoreCase(name))
                return type;
        return null;
    }

    @Getter
    private final String name;

    public LGChatType(String name) {
        this.name = name;

        values.add(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LGChatType that = (LGChatType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
