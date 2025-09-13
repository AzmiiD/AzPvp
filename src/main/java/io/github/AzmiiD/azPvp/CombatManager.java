package io.github.AzmiiD.azPvp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final AzPvp plugin;
    private final Map<UUID, Long> combatTimers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> combatRunnables = new HashMap<>(); // Track per player runnable
    private int cooldownSeconds;

    public CombatManager(AzPvp plugin) {
        this.plugin = plugin;
        this.cooldownSeconds = plugin.getConfig().getInt("combat-cooldown", 5);
    }

    public void enterCombat(Player player) {
        UUID uuid = player.getUniqueId();
        long newExpiry = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        combatTimers.put(uuid, newExpiry);

        // Cancel existing runnable if any
        BukkitRunnable existing = combatRunnables.get(uuid);
        if (existing != null) {
            existing.cancel();
        }

        // Start new runnable for countdown
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isInCombat(player)) {
                    sendActionBar(player, plugin.getMessageWithoutPrefix("messages.exit-combat"));
                    combatRunnables.remove(uuid);
                    cancel();
                    return;
                }

                // Calculate remaining seconds
                long remainingMillis = combatTimers.get(uuid) - System.currentTimeMillis();
                int remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);
                String message = plugin.getMessageWithoutPrefix("messages.in-combat").replace("{remaining}", String.valueOf(remainingSeconds));
                sendActionBar(player, message);
            }
        };
        runnable.runTaskTimer(plugin, 0L, 20L); // Start immediately, update every second
        combatRunnables.put(uuid, runnable);
    }

    public boolean isInCombat(Player player) {
        Long expiry = combatTimers.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            combatTimers.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void updateCooldown(int seconds) {
        this.cooldownSeconds = seconds;
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}