package com.dziubek.limbo.voidteleport;

import com.loohp.limbo.file.FileConfiguration;
import com.loohp.limbo.plugins.LimboPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Gdy gracz spadnie poniżej progu "voida" (domyślnie Y=-64, konfigurowalne w config.yml),
 * zostaje teleportowany na spawn świata zamiast dalej spadać.
 */
public class VoidTeleportPlugin extends LimboPlugin {

    private static final String CONFIG_KEY_VOID_Y = "void-y";
    private static final int DEFAULT_VOID_Y = -64;

    private int voidY = DEFAULT_VOID_Y;

    @Override
    public void onEnable() {
        loadConfig();
        getServer().getEventsManager().registerEvents(this, new VoidListener(this));
        getServer().getConsole().sendMessage("[LimboVoidTeleport] Włączono - próg voida: Y=" + voidY);
    }

    public int getVoidY() {
        return voidY;
    }

    private void loadConfig() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File configFile = new File(dataFolder, "config.yml");
        try {
            FileConfiguration config = new FileConfiguration(configFile);
            Integer configured = config.get(CONFIG_KEY_VOID_Y, Integer.class);
            if (configured == null) {
                config.set(CONFIG_KEY_VOID_Y, DEFAULT_VOID_Y);
                config.saveConfig(configFile);
                voidY = DEFAULT_VOID_Y;
            } else {
                voidY = configured;
            }
        } catch (IOException e) {
            getServer().getConsole().sendMessage("[LimboVoidTeleport] Nie udało się wczytać config.yml: " + e.getMessage());
        }
    }
}
