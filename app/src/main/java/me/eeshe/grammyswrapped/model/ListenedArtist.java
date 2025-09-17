package me.eeshe.grammyswrapped.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ListenedArtist {
  private final UUID uuid;
  private final String name;
  private final Set<String> listenedSongs;

  public ListenedArtist(String name) {
    this.uuid = UUID.randomUUID();
    this.name = name;
    this.listenedSongs = new HashSet<>();
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public Set<String> getListenedSongs() {
    return listenedSongs;
  }

  public void addListenedSong(String songName) {
    listenedSongs.add(songName);
  }
}
