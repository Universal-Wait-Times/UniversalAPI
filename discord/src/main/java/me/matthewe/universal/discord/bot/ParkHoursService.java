package me.matthewe.universal.discord.bot;

import jakarta.annotation.PostConstruct;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkHoursService {
    private JDAService service;
    private SettingService settingService;

    @Autowired
    public ParkHoursService(JDAService service, SettingService settingService) {
        this.service = service;
        this.settingService = settingService;
    }

    @PostConstruct
    public void start() throws Exception {
        handleParkHours();

    }

    private void handleParkHours() {


    }


}

