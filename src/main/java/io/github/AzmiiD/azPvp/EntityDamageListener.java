package io.github.AzmiiD.azPvp;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {
    private final AzPvp plugin;

    public EntityDamageListener(AzPvp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        // Extract attacker if any
        Player attacker = getAttacker(damager);

        // Handle PVP only if victim is player and attacker is player
        if (attacker != null && victim instanceof Player) {
            Player target = (Player) victim;

            if (!plugin.isPvPEnabled(attacker.getUniqueId()) || !plugin.isPvPEnabled(target.getUniqueId())) {
                event.setCancelled(true);
                // Anti-spam message
                long currentTime = System.currentTimeMillis();
                Long lastTime = plugin.getLastPvpMessageTime().get(attacker.getUniqueId());
                if (lastTime == null || currentTime - lastTime > 5000) {
                    attacker.sendMessage(plugin.getPrefixedMessage("messages.pvp-disabled-attack"));
                    plugin.getLastPvpMessageTime().put(attacker.getUniqueId(), currentTime);
                }
                return;
            }

            // Enter combat for both in PVP
            plugin.getCombatManager().enterCombat(attacker);
            plugin.getCombatManager().enterCombat(target);
            return;
        }

        // If not PVP, check for hostile mob involvement
        Player playerInvolved = attacker != null ? attacker : (victim instanceof Player ? (Player) victim : null);
        boolean isHostile = isHostileMob(victim) || isHostileMob(damager);

        if (isHostile && playerInvolved != null) {
            plugin.getCombatManager().enterCombat(playerInvolved);
        } else {
        }

        // Explicit: Do not cancel for non-PVP
    }

    private Player getAttacker(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        } else if (damager instanceof Trident) {
            Trident trident = (Trident) damager;
            if (trident.getShooter() instanceof Player) {
                return (Player) trident.getShooter();
            }
        }
        return null;
    }

    private boolean isHostileMob(Entity entity) {
        String type = entity.getType().toString().toLowerCase();
        return type.contains("zombie") || type.contains("skeleton") || type.contains("creeper") ||
                type.contains("spider") || type.contains("enderman") || type.contains("piglin") ||
                type.contains("blaze") || type.contains("ghast") || type.contains("wither") ||
                type.contains("dragon");
    }
}