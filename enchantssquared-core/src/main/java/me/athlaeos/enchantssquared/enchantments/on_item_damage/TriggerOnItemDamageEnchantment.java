package me.athlaeos.enchantssquared.enchantments.on_item_damage;

import org.bukkit.event.player.PlayerItemDamageEvent;

public interface TriggerOnItemDamageEnchantment {
    /**
     * Trigger on an PlayerItemDamageEvent. Enchantments of this type trigger when a player possessing the
     * enchanted item causes it to take durability damage
     * @param e the event
     */
    void onItemDamage(PlayerItemDamageEvent e, int level);
}
