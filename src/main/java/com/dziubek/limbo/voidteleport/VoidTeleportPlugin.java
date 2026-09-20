package com.dziubek.limbo.voidteleport;

import com.loohp.limbo.file.FileConfiguration;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.plugins.LimboPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gdy gracz spadnie poniżej progu "voida" (domyślnie Y=-64, konfigurowalne w config.yml),
 * zostaje teleportowany na spawn świata z krótką animacją "wynurzania się" (unosi się z dołu do
 * spawnu z easingiem) zamiast dalej spadać.
 */
public class VoidTeleportPlugin extends LimboPlugin {

    private static final String CONFIG_KEY_VOID_Y = "void-y";
    private static final int DEFAULT_VOID_Y = -64;
    private static final double RISE_HEIGHT = 6.0;
    private static final long RISE_TICKS = 30L;

    private int voidY = DEFAULT_VOID_Y;
    private final Set<UUID> returning = new HashSet<>();

    @Override
    public void onEnable() {
        loadConfig();
        getServer().getEventsManager().registerEvents(this, new VoidListener(this));
        getServer().getConsole().sendMessage("[LimboVoidTeleport] Włączono - próg voida: Y=" + voidY);
    }

    public int getVoidY() {
        return voidY;
    }

    public boolean isReturning(Player player) {
        return returning.contains(player.getUniqueId());
    }

    /**
     * Krótka animacja "wynurzania się" z voida - gracz ląduje kawałek poniżej spawnu i unosi się
     * do niego z easingiem (ease-out cubic), z tytułem i dźwiękiem na start/koniec.
     */
    public void playReturnAnimation(Player player, Location spawn) {
        UUID uuid = player.getUniqueId();
        returning.add(uuid);

        Location start = spawn.clone();
        start.setY(spawn.getY() - RISE_HEIGHT);
        player.teleport(start);
        player.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.PLAYER, 0.8f, 0.7f));
        player.setTitleSubTitle("§5§lOTCHŁAŃ", "§7Wracasz na spawn...", 2, 10, 5);

        animateRise(player, start, spawn.clone(), 0L);
    }

    private void animateRise(Player player, Location start, Location target, long tick) {
        double t = Math.min(1.0, tick / (double) RISE_TICKS);
        double eased = 1 - Math.pow(1 - t, 3);
        Location at = target.clone();
        at.setY(start.getY() + (target.getY() - start.getY()) * eased);
        player.teleport(at);

        if (t >= 1.0) {
            returning.remove(player.getUniqueId());
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.levelup"), Sound.Source.PLAYER, 0.6f, 1.4f));
            player.setTitleSubTitle("§a§lURATOWANY!", "", 2, 15, 10);
            return;
        }
        getServer().getScheduler().runTaskLater(this, () -> animateRise(player, start, target, tick + 1), 1L);
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
