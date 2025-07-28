package fr.valgrifer.loupgarou.classes.chat;

import fr.valgrifer.loupgarou.classes.Interlocutor;
import fr.valgrifer.loupgarou.classes.LGChatType;
import fr.valgrifer.loupgarou.classes.LGGame;
import lombok.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class LGChat implements Interlocutor
{
    private final List<Interlocutor> viewers = new ArrayList<>();
    @Getter
    private final LGGame game;
    @Getter
    private final LGChatType type;
    @Getter
    private LGChat chat = null;
    @Getter
    @Setter
    private boolean muet = false;

    public LGChat(LGGame game, LGChatType type)
    {
        this.game = game;
        this.type = type;

        this.game.registerChat(this);
    }

    public List<Interlocutor> getViewers()
    {
        return Collections.unmodifiableList(this.viewers);
    }

    public void sendMessage(@Nonnull Interlocutor sender, @Nonnull String message)
    {
        this.sendMessage(this.makeContext(sender), message);
    }

    public void sendMessage(@Nonnull InterlocutorContext context, @Nonnull String message)
    {
        String msg = this.receive(context, message);
        for (Interlocutor interlocutor : this.viewers)
            if (interlocutor instanceof LGChat)
            {
                if (context.getChat().equals(interlocutor))
                    continue;
                ((LGChat) interlocutor).sendMessage(context.toBuilder().chat(this).build(), message);
            }
            else if (msg != null)
                interlocutor.sendMessage(msg);

        if (chat != null && !this.muet && context.getChat().equals(this))
            this.chat.sendMessage(context, message);
    }

    public void join(@Nonnull Interlocutor interlocutor)
    {
        if (this.viewers.contains(interlocutor))
            return;

        this.viewers.add(interlocutor);
    }

    public void leave(@Nonnull Interlocutor interlocutor)
    {
        if (!this.viewers.contains(interlocutor))
            return;

        this.viewers.remove(interlocutor);
    }

    public void joinChat(@Nonnull LGChat chat)
    {
        this.joinChat(chat, false);
    }

    public void joinChat(@Nonnull LGChat chat, boolean muted)
    {
        if (this.equals(chat)) return;
        this.chat = chat;
        this.chat.join(this);
        this.muet = muted;
    }

    public void leaveChat()
    {
        if (chat != null)
            chat.leave(this);
        this.muet = true;
    }

    public void onChat(@Nonnull String message)
    {
        this.sendMessage(this, message);
    }

    public void sendMessage(@Nonnull String message)
    {
        for (Interlocutor player : this.viewers)
            player.sendMessage(message);
    }

    public InterlocutorContext makeContext(@Nonnull Interlocutor sender)
    {
        return new InterlocutorContext(this, sender);
    }

    @Override
    public String getName()
    {
        return "[" + this.getType().getName() + "]";
    }

    public abstract @Nullable String receive(@Nonnull InterlocutorContext context, @Nonnull String message);

    @Getter
    @Builder(builderClassName = "Builder")
    public static class InterlocutorContext
    {
        private final @Nonnull LGChat chat;
        private final @Nonnull Interlocutor interlocutor;

        public InterlocutorContext.Builder toBuilder()
        {
            return new InterlocutorContext.Builder()
                           .chat(this.chat)
                           .interlocutor(this.interlocutor);
        }
    }
}
