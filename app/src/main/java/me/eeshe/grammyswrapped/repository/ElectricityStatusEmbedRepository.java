package me.eeshe.grammyswrapped.repository;

import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;

public interface ElectricityStatusEmbedRepository extends Repository {

    ElectricityStatusEmbed getByMessageId(String messageId);

    void save(ElectricityStatusEmbed electricityStatusEmbed);
}
