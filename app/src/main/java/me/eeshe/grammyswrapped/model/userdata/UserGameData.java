package me.eeshe.grammyswrapped.model.userdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.User;

public class UserGameData extends UserData {
  // Game Name, Time Played in Milliseconds
  private final Map<String, Long> playedGames;

  public UserGameData(User user) {
    super(user);

    this.playedGames = new HashMap<>();
  }

  public void addPlayedGame(String gameName, long playedTimeMillis) {
    long accumulatedPlayedTimeMillis = playedGames.getOrDefault(gameName, 0L);
    accumulatedPlayedTimeMillis += playedTimeMillis;

    playedGames.put(gameName, accumulatedPlayedTimeMillis);
  }

  public Map<String, Long> getPlayedGames() {
    return playedGames;
  }

  /**
   * Returns a List with only the names of the games the user has played,
   * ordered alphabetically.
   */
  public List<String> getPlayedGameList() {
    List<String> playedGames = new ArrayList<>(this.playedGames.keySet());
    playedGames.sort(Comparator.naturalOrder());

    return playedGames;
  }
}
