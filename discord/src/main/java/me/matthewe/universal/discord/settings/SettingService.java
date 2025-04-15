package me.matthewe.universal.discord.settings;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Log
@Service
public class SettingService {
    private Map<String, Object> settings = new HashMap<String, Object>();
    private SettingRepository settingRepository;

    @Autowired
    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
        loadAllSettings();
    }

    public Object getSetting(String key) {
        return settings.get(key);
    }

    public void setSetting(String key, Object value) {
        settings.put(key, value);
        settingRepository.save(new Setting(key, value));
        log.info("Saved setting: " + key);
    }

    private void loadAllSettings() {
        for (Setting setting : settingRepository.findAll()) {
            settings.put(setting.getKey(), setting.getValue());
            log.info("Loaded setting: " + setting.getKey());
        }
    }
}
