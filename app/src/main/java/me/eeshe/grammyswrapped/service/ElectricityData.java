package me.eeshe.grammyswrapped.service;

import java.util.HashMap;
import java.util.Map;

import me.eeshe.grammyswrapped.model.userdata.UserElectricityData;

public class ElectricityData {
    private final Map<String, UserElectricityData> userElectricityData;

    public ElectricityData() {
        this.userElectricityData = new HashMap<>();
    }

    public ElectricityData(Map<String, UserElectricityData> userElectricityData) {
        this.userElectricityData = userElectricityData;
    }

    public Map<String, UserElectricityData> getUserElectricityData() {
        return userElectricityData;
    }
}
