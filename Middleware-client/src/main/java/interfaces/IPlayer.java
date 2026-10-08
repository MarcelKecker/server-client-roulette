package main.java.interfaces;

import java.util.UUID;

public interface IPlayer {
    void hear(String message);
    String getName();
    UUID getID();
}
