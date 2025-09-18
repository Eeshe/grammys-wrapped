package me.eeshe.grammyswrapped.model.userdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.eeshe.grammyswrapped.model.ListenedArtist;
import net.dv8tion.jda.api.entities.User;

public class UserMusicData extends UserData {
  private final Map<String, ListenedArtist> listenedArtists;

  public UserMusicData(User user) {
    super(user);
    this.listenedArtists = new HashMap<>();
  }

  public Map<String, ListenedArtist> getListenedArtists() {
    return listenedArtists;
  }

  public List<ListenedArtist> getListenedArtistsList() {
    List<ListenedArtist> listenedArtists = new ArrayList<>(this.listenedArtists.values());
    listenedArtists.sort(Comparator.comparing(artist -> artist.getName()));

    return listenedArtists;
  }

  public void addListenedSong(String songName, String artistName) {
    ListenedArtist listenedArtist = listenedArtists.getOrDefault(artistName,
        new ListenedArtist(artistName));
    listenedArtist.addListenedSong(songName);

    listenedArtists.put(artistName, listenedArtist);
  }
}
