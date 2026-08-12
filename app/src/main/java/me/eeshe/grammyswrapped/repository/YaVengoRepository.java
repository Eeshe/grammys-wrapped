package me.eeshe.grammyswrapped.repository;

import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.api.entities.User;

public class YaVengoRepository {
    private final ConcurrentHashMap<String, User> yaVengoTargets = new ConcurrentHashMap<>();

    public void put(User user, User target) {
        yaVengoTargets.put(user.getId(), target);
    }

    public User get(User user) {
        return yaVengoTargets.get(user.getId());
    }
}
