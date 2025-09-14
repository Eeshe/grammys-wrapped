package me.eeshe.grammyswrapped.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.User;

public class UserGameData {
  private final String userId;
  private final String username;
  // Game Name, Time Played in Milliseconds
  private final Map<String, Long> playedGames;

  public UserGameData(User user) {
    this.userId = user.getId();
    this.username = user.getName();
    this.playedGames = new HashMap<>();
  }

  public void addPlayedGame(String gameName, long playedTimeMillis) {
    long accumulatedPlayedTimeMillis = playedGames.getOrDefault(gameName, 0L);
    accumulatedPlayedTimeMillis += playedTimeMillis;

    playedGames.put(gameName, accumulatedPlayedTimeMillis);
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
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
