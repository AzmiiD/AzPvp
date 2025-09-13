package io.github.AzmiiD.azPvp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPreProcessListener implements Listener {
    private final AzPvp plugin;

    public CommandPreProcessListener(AzPvp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("azpvp.bypass")) return;

        if (plugin.getCombatManager().isInCombat(player)) {
            String command = event.getMessage().toLowerCase().split(" ")[0].replace("/", "");
            for (String blocked : plugin.getConfig().getStringList("blocked-commands")) {
                if (command.equals(blocked.toLowerCase())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getPrefixedMessage("messages.command-blocked"));
                    break;
                }
            }
        }
    }
}