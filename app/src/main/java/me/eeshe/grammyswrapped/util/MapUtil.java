package me.eeshe.grammyswrapped.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapUtil {

  public static <K extends Comparable<K>, V> void sortByKey(Map<K, V> map, boolean reverse) {
    if (map == null || map.isEmpty()) {
      return;
    }

    List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
    Comparator<? super Map.Entry<K, V>> comparator = Map.Entry.comparingByKey();
    if (reverse) {
      comparator = comparator.reversed();
    }
    entryList.sort(comparator);
    map.clear();
    for (Map.Entry<K, V> entry : entryList) {
      map.put(entry.getKey(), entry.getValue());
    }
  }

  public static <K, V extends Comparable<V>> void sortByValue(Map<K, V> map, boolean reverse) {
    if (map == null || map.isEmpty()) {
      return;
    }
    List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
    Comparator<? super Map.Entry<K, V>> comparator = Map.Entry.comparingByValue();
    if (reverse) {
      comparator = comparator.reversed();
    }
    entryList.sort(comparator);
    map.clear();
    for (Map.Entry<K, V> entry : entryList) {
      map.put(entry.getKey(), entry.getValue());
    }
  }
}
