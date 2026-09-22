package fachlogik;

import interfaces.IChatter;

import java.util.ArrayList;
import java.util.List;

public class Chatroom implements interfaces.IChatroom {
    List<IChatter> chatters = new ArrayList<>();
    @Override
    public void enter(IChatter chatter) {
        if (chatters.contains(chatter)) {
            throw new IllegalArgumentException("Chatter " + chatter.getName() + " already exists");
        }
        chatters.add(chatter);
        post(chatter, "entered");
    }

    @Override
    public void leave(IChatter chatter) {
        if (!chatters.contains(chatter)) {
            throw new IllegalArgumentException("Chatter " + chatter.getName() + " does not exist");
        }
        chatters.remove(chatter);
        post(chatter, "left");
    }

    @Override
    public void post(IChatter author, String message) {
        for (IChatter chatter : chatters) {
            if (chatter == author) {
                chatter.hear("I said: " + message);
            } else {
                chatter.hear(author.getName() + " said: " + message);
            }
        }
    }
}
