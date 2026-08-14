package me.eeshe.grammyswrapped.repository;

import me.eeshe.grammyswrapped.model.UserElectricityStatus;

public interface UserElectricityStatusRepository extends Repository {

    void save(UserElectricityStatus userElectricityStatus);

    UserElectricityStatus getById(String userId);
}
