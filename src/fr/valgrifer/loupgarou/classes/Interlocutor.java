package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.classes.chat.LGChat;

import javax.annotation.Nonnull;

public interface Interlocutor
{
    void joinChat(@Nonnull LGChat chat);
    void joinChat(@Nonnull LGChat chat, boolean muted);

    void leaveChat();

    /**
     * Send message by Conversable
     */
    void onChat(@Nonnull String message);

    /**
     * Send message to Conversable
     */
    void sendMessage(@Nonnull String message);

    String getName();
}
