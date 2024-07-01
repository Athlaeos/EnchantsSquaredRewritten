package me.athlaeos.enchantssquared.enchantments.on_interact;

import org.bukkit.event.player.PlayerInteractEvent;

public interface TriggerOnInteractEnchantment {
    /**
     * Trigger on an PlayerInteractEvent. Enchantments of this type trigger when a player possessing the
     * enchanted item interacts with anything or using anything
     * @param e the event
     */
    void onInteract(PlayerInteractEvent e, int level);
}
