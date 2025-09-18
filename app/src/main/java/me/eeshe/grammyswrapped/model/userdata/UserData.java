package me.eeshe.grammyswrapped.model.userdata;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.User;

public class UserData {
  private final User user;

  public UserData(User user) {
    this.user = user;
  }

  public static Map<String, ? extends UserData> sortByUsername(Map<String, ? extends UserData> userDataMap) {
    return userDataMap.entrySet().stream()
        .sorted(Comparator.comparing(entry -> ((UserData) entry.getValue()).getUser().getName()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldValue, newValue) -> oldValue,
            LinkedHashMap::new));
  }

  public User getUser() {
    return user;
  }
}
