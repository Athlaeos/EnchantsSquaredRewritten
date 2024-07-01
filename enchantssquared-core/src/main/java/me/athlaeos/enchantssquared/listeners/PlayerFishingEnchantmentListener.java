package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_fishing.TriggerOnFishingEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFishingEnchantmentListener implements Listener {
    @EventHandler
    public void onFish(PlayerFishEvent e){
        Player fisher = e.getPlayer();

        if (EnchantsSquared.isWorldGuardAllowed(fisher, e.getHook().getLocation(), "es-deny-all")){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(fisher);

            boolean offHand = fisher.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD && fisher.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD;

            for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnFishingEnchantment)){
                ((TriggerOnFishingEnchantment) enchantment).onFish(e, enchantment.getLevelService(offHand, fisher).getLevel(equipment));
            }
        }
    }
}
