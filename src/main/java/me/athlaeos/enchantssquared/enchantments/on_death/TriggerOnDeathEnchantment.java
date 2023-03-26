package me.athlaeos.enchantssquared.enchantments.on_death;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface TriggerOnDeathEnchantment {
    /**
     * Trigger on an EntityDeathEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item dies
     * @param e the event
     */
    void onEntityDeath(EntityDeathEvent e, int level);
    /**
     * Trigger on an EntityDeathEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item kills another entity
     * @param e the event
     */
    void onEntityKilled(EntityDeathEvent e, int level);
    /**
     * Trigger on an PlayerDeathEvent. Enchantments of this type trigger when a player possessing the
     * enchanted item dies
     * @param e the event
     */
    void onPlayerDeath(PlayerDeathEvent e, int level);
    /**
     * Trigger on an PlayerDeathEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item kills a player
     * @param e the event
     */
    void onPlayerKilled(PlayerDeathEvent e, int level);
    /**
     * Trigger on an PlayerRespawnEvent. Enchantments of this type trigger when a player possessing the
     * enchanted item respawns from death
     * @param e the event
     */
    void onPlayerRespawn(PlayerRespawnEvent e, int level);
}
