package io.github.AzmiiD.azRestart;

import org.bukkit.plugin.java.JavaPlugin;
import io.github.AzmiiD.azRestart.task.RestartTask;
import io.github.AzmiiD.azRestart.command.ReloadCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AzRestart extends JavaPlugin {

    private RestartTask restartTask;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize restart task
        restartTask = new RestartTask(this);

        // Register command
        getCommand("autorestart").setExecutor(new ReloadCommand(this));

        // Start the restart scheduler
        restartTask.startScheduler();

        getLogger().info("AutoRestart plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Cancel the restart task if it's running
        if (restartTask != null) {
            restartTask.stopScheduler();
        }

        getLogger().info("AutoRestart plugin disabled!");
    }

    /**
     * Reloads the plugin configuration and restarts the scheduler
     */
    public void reloadPluginConfig() {
        // Reload config from disk
        reloadConfig();

        // Stop current task
        if (restartTask != null) {
            restartTask.stopScheduler();
        }

        // Create new task with updated config
        restartTask = new RestartTask(this);
        restartTask.startScheduler();

        getLogger().info("AutoRestart configuration reloaded!");
    }

    /**
     * Gets the restart task instance
     * @return RestartTask instance
     */
    public RestartTask getRestartTask() {
        return restartTask;
    }
}
