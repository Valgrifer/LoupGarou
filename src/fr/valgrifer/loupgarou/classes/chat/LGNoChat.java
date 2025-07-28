package fr.valgrifer.loupgarou.classes.chat;

import fr.valgrifer.loupgarou.classes.LGChatType;
import fr.valgrifer.loupgarou.classes.LGGame;
import lombok.Getter;

import javax.annotation.Nonnull;

public class LGNoChat extends LGChat {
    public LGNoChat(LGGame game) {
        super(game, LGChatType.NOCHAT);
    }

    @Override
    public void onChat(@Nonnull String message) { }

    @Override
    public void sendMessage(@Nonnull String message) { }

    @Override
    public void sendMessage(@Nonnull InterlocutorContext context, @Nonnull String message) { }

    @Override
    public String getName()
    { return ""; }

    @Override
    public String receive(@Nonnull InterlocutorContext context, @Nonnull String message)
    { return ""; }
}
