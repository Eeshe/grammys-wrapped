package me.eeshe.grammyswrapped.repository;

import java.util.List;

import me.eeshe.grammyswrapped.model.ElectricityStatusEmbed;

public interface ElectricityStatusEmbedRepository extends Repository {

    void save(ElectricityStatusEmbed electricityStatusEmbed);

    List<ElectricityStatusEmbed> findAll();

    ElectricityStatusEmbed getByMessageId(String messageId);

    void delete(String messageId);
}