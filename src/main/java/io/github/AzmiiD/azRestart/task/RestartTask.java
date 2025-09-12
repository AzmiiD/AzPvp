package io.github.AzmiiD.azRestart.task;

import io.github.AzmiiD.azRestart.AzRestart;
import io.github.AzmiiD.azRestart.util.WebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RestartTask {

    private final AzRestart plugin;
    private BukkitTask schedulerTask;
    private BukkitTask countdownTask;
    private LocalTime restartTime;
    private String prefix;
    private List<String> warningMessages;
    private String webhookUrl;
    private boolean restartInProgress = false;

    public RestartTask(AzRestart plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads configuration values from config.yml
     */
    private void loadConfig() {
        // Parse restart time
        String timeString = plugin.getConfig().getString("restart-time", "06:00");
        this.restartTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));

        // Load other config values
        this.prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("prefix", "&c[AutoRestart] "));
        this.warningMessages = plugin.getConfig().getStringList("warning-messages");
        this.webhookUrl = plugin.getConfig().getString("webhook-url", "");

        plugin.getLogger().info("Configured restart time: " + timeString);
    }

    /**
     * Starts the main scheduler that checks for restart time
     */
    public void startScheduler() {
        // Check every 30 seconds for restart time
        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!restartInProgress) {
                    checkRestartTime();
                }
            }
        }.runTaskTimer(plugin, 0L, 600L); // 600 ticks = 30 seconds

        plugin.getLogger().info("Restart scheduler started!");
    }

    /**
     * Stops all running schedulers
     */
    public void stopScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        restartInProgress = false;
    }

    /**
     * Checks if current time matches restart time and initiates countdown
     */
    private void checkRestartTime() {
        LocalTime now = LocalTime.now();
        LocalTime restartWindow = restartTime.minusMinutes(5); // 5 minutes before restart

        // Check if we're within the 5-minute window before restart time
        if (now.isAfter(restartWindow) && now.isBefore(restartTime.plusMinutes(1)) && !restartInProgress) {
            plugin.getLogger().info("Restart time reached! Starting countdown...");
            startRestartCountdown();
        }
    }

    /**
     * Starts the restart countdown sequence
     */
    private void startRestartCountdown() {
        restartInProgress = true;

        countdownTask = new BukkitRunnable() {
            private int timeLeft = 300; // 5 minutes in seconds

            @Override
            public void run() {
                // Check for warning message times
                if (timeLeft == 300) { // 5 minutes
                    sendWarningMessage(0);
                } else if (timeLeft == 60) { // 1 minute
                    sendWarningMessage(1);
                } else if (timeLeft == 10) { // 10 seconds
                    sendWarningMessage(2);
                } else if (timeLeft <= 0) {
                    // Restart the server
                    restartServer();
                    cancel();
                    return;
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    /**
     * Sends a warning message to players and webhook
     * @param messageIndex Index of the warning message in config
     */
    private void sendWarningMessage(int messageIndex) {
        if (messageIndex >= 0 && messageIndex < warningMessages.size()) {
            String message = prefix + warningMessages.get(messageIndex);

            // Broadcast to all players
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

            // Send to Discord webhook
            if (!webhookUrl.isEmpty()) {
                WebhookUtil.sendWebhookMessage(webhookUrl,
                        ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
            }

            plugin.getLogger().info("Sent restart warning: " +
                    ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }

    /**
     * Performs the server restart
     */
    private void restartServer() {
        String restartMessage = prefix + "Server is restarting now!";

        // Final message to players and webhook
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', restartMessage));

        if (!webhookUrl.isEmpty()) {
            WebhookUtil.sendWebhookMessage(webhookUrl,
                    ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', restartMessage)));
        }

        plugin.getLogger().info("Restarting server...");

        // Wait a moment for messages to be sent, then restart
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(plugin, 40L); // Wait 2 seconds (40 ticks)
    }

    /**
     * Gets the configured restart time
     * @return LocalTime restart time
     */
    public LocalTime getRestartTime() {
        return restartTime;
    }

    /**
     * Checks if restart is currently in progress
     * @return true if restart countdown is active
     */
    public boolean isRestartInProgress() {
        return restartInProgress;
    }
}