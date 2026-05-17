package com.rakku212.rememberme;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.nio.file.Files;

@Plugin(
        id = "rememberme",
        name = "Rememberme",
        version = BuildConstants.VERSION
)
public class Rememberme {

    private final ProxyServer server;
    private final Path dataDirectory;
    @Inject
    private org.slf4j.Logger logger;

    @Inject
    public Rememberme(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.error("Could not create data directory", e);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(registeredServer -> {
            String serverName = registeredServer.getServerInfo().getName();
            UUID uuid = player.getUniqueId();
            Path playerFile = dataDirectory.resolve(uuid + ".txt");

            try {
                Files.writeString(playerFile, serverName);
            } catch (IOException e) {
                logger.error("Failed to save player data", e);
            }
        });
    }


    @Subscribe
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Path playerFile = dataDirectory.resolve(uuid + ".txt");

        try {
            if (!Files.exists(playerFile)) {
                return;
            }
            String lastServerName = Files.readString(playerFile).trim();
            if (lastServerName.isEmpty() || lastServerName.equals("lobby")) {
                return;
            }
            server.getServer(lastServerName).ifPresent(event::setInitialServer);
        } catch (IOException e) {
            logger.error("Failed to fetch player data", e);
        }
    }
}
