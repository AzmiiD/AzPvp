package io.github.AzmiiD.azPvp;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AzPvp extends JavaPlugin {

    private CombatManager combatManager;
    private Map<UUID, Boolean> pvpStatus = new HashMap<>(); // Default PVP off
    private Map<UUID, Long> lastPvpMessageTime = new HashMap<>(); // Anti-spam PVP message
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        combatManager = new CombatManager(this);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CommandPreProcessListener(this), this);
//        Bukkit.getPluginManager().registerEvents(new ProjectileListener(this), this);

        // Register commands with tab completer
        PvPCommand pvPCommand = new PvPCommand(this);
        getCommand("pvp").setExecutor(pvPCommand);
        getCommand("pvp").setTabCompleter(pvPCommand);
        getCommand("azpvp").setExecutor(pvPCommand);
        getCommand("azpvp").setTabCompleter(pvPCommand);

        getLogger().info("AZPvP plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AZPvP plugin disabled!");
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public boolean isPvPEnabled(UUID playerUUID) {
        return pvpStatus.getOrDefault(playerUUID, false);
    }

    public void setPvPEnabled(UUID playerUUID, boolean enabled) {
        pvpStatus.put(playerUUID, enabled);
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        combatManager.updateCooldown(config.getInt("combat-cooldown", 5));
        getLogger().info("Config reloaded!");
    }

    // Helper for prefixed message (for chat)
    public String getPrefixedMessage(String key) {
        String prefix = config.getString("prefix", "&6[AZPvP] ");
        String message = config.getString(key, "Message not found.");
        message = message.replace("{cooldown}", String.valueOf(config.getInt("combat-cooldown", 5)));
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    // Helper for message without prefix (for action bar)
    public String getMessageWithoutPrefix(String key) {
        String message = config.getString(key, "Message not found.");
        message = message.replace("{cooldown}", String.valueOf(config.getInt("combat-cooldown", 5)));
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    public Map<UUID, Long> getLastPvpMessageTime() {
        return lastPvpMessageTime;
    }
}
