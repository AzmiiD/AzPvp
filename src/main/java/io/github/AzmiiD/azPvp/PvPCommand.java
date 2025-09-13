package io.github.AzmiiD.azPvp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PvPCommand implements CommandExecutor, TabCompleter {
    private final AzPvp plugin;

    public PvPCommand(AzPvp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command ini hanya untuk player!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("pvp")) {
            if (args.length == 0) {
                player.sendMessage(plugin.getPrefixedMessage("messages.pvp-usage"));
                return true;
            }

            String arg = args[0].toLowerCase();
            if (arg.equals("on")) {
                plugin.setPvPEnabled(player.getUniqueId(), true);
                player.sendMessage(plugin.getPrefixedMessage("messages.pvp-enabled"));
            } else if (arg.equals("off")) {
                plugin.setPvPEnabled(player.getUniqueId(), false);
                player.sendMessage(plugin.getPrefixedMessage("messages.pvp-disabled"));
            } else {
                player.sendMessage(plugin.getPrefixedMessage("messages.pvp-usage"));
            }
            return true;
        } else if (label.equalsIgnoreCase("azpvp")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("azpvp.reload")) {
                    plugin.reloadPluginConfig();
                    player.sendMessage(plugin.getPrefixedMessage("messages.reload-success"));
                } else {
                    player.sendMessage(plugin.getPrefixedMessage("messages.no-permission"));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("pvp")) {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], List.of("on", "off"), completions);
            }
        } else if (command.getName().equalsIgnoreCase("azpvp")) {
            if (args.length == 1 && sender.hasPermission("azpvp.reload")) {
                StringUtil.copyPartialMatches(args[0], List.of("reload"), completions);
            }
        }
        Collections.sort(completions);
        return completions;
    }
}