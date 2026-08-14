package me.eeshe.grammyswrapped.service.impl;

import me.eeshe.grammyswrapped.service.ElectricityStatusService;
import net.dv8tion.jda.api.JDA;

public class ElectricityStatusServiceImpl implements ElectricityStatusService {
    private final JDA bot;

    public ElectricityStatusServiceImpl(JDA bot) {
        this.bot = bot;
    }

    @Override
    public void postElectricityStatusEmbed(String guildId, String channelId) {

    }

}
