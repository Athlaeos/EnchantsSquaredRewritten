package me.athlaeos.enchantssquared.enchantments.on_potion_effect;

import org.bukkit.event.entity.EntityPotionEffectEvent;

public interface TriggerOnPotionEffectEnchantment {
    /**
     * Trigger on an EntityPotionEffectEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item gains or loses a potion effect
     * @param e the event
     */
    void onPotionEffect(EntityPotionEffectEvent e, int level);
}
