package io.github.AzmiiD.azRestart.command;

import io.github.AzmiiD.azRestart.AzRestart;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final AzRestart plugin;

    public ReloadCommand(AzRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if command is /autorestart
        if (!command.getName().equalsIgnoreCase("autorestart")) {
            return false;
        }

        // Check permissions
        if (!sender.hasPermission("autorestart.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Check if arguments are provided
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "AutoRestart Plugin v1.0");
            sender.sendMessage(ChatColor.YELLOW + "Usage: /autorestart reload");
            return true;
        }

        // Handle reload subcommand
        if (args[0].equalsIgnoreCase("reload")) {
            try {
                // Reload the plugin configuration
                plugin.reloadPluginConfig();

                sender.sendMessage(ChatColor.GREEN + "AutoRestart configuration reloaded successfully!");

                // Show current restart time
                if (plugin.getRestartTask() != null) {
                    sender.sendMessage(ChatColor.YELLOW + "Next restart scheduled at: " +
                            plugin.getRestartTask().getRestartTime());
                }

                return true;

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
                plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }

        // Handle status subcommand (bonus feature)
        if (args[0].equalsIgnoreCase("status")) {
            if (plugin.getRestartTask() != null) {
                sender.sendMessage(ChatColor.YELLOW + "AutoRestart Status:");
                sender.sendMessage(ChatColor.YELLOW + "Next restart time: " +
                        plugin.getRestartTask().getRestartTime());
                sender.sendMessage(ChatColor.YELLOW + "Restart in progress: " +
                        (plugin.getRestartTask().isRestartInProgress() ?
                                ChatColor.RED + "Yes" : ChatColor.GREEN + "No"));
            } else {
                sender.sendMessage(ChatColor.RED + "AutoRestart task is not initialized!");
            }
            return true;
        }

        // Unknown subcommand
        sender.sendMessage(ChatColor.RED + "Unknown subcommand! Use: /autorestart reload");
        return true;
    }
}