package com.dziubek.limbo.voidteleport;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.player.PlayerMoveEvent;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;

public class VoidListener implements Listener {

    private final VoidTeleportPlugin plugin;

    public VoidListener(VoidTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null || to.getY() > plugin.getVoidY()) {
            return;
        }
        Player player = event.getPlayer();
        Location spawn = Limbo.getInstance().getServerProperties().getWorldSpawn();
        player.teleport(spawn);
    }
}
