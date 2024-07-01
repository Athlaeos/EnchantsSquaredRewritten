package me.athlaeos.enchantssquared.enchantments.on_fishing;

import org.bukkit.event.player.PlayerFishEvent;

public interface TriggerOnFishingEnchantment {
    /**
     * Trigger on an PlayerFishEvent. Enchantments of this type trigger when aa player possessing the
     * enchanted item uses the fishing rod
     * @param e the event
     */
    void onFish(PlayerFishEvent e, int level);
}
