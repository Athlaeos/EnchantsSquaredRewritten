package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_block_break.TriggerOnBlockBreakEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;

public class BlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent e){
        if (!e.isCancelled()){
            Player breaker = e.getPlayer();

            if (EnchantsSquared.isWorldGuardAllowed(breaker, e.getBlock().getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(breaker); // EntityUtils.getEntityEquipment(breaker);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnBlockBreakEnchantment)){
                    ((TriggerOnBlockBreakEnchantment) enchantment).onBlockBreak(e, enchantment.getLevelService(false, breaker).getLevel(equipment));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDropItem(BlockDropItemEvent e){
        if (!e.isCancelled()){
            Player breaker = e.getPlayer();

            if (EnchantsSquared.isWorldGuardAllowed(breaker, e.getBlock().getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(breaker); // EntityUtils.getEntityEquipment(breaker);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnBlockBreakEnchantment)){
                    ((TriggerOnBlockBreakEnchantment) enchantment).onBlockDropItem(e, enchantment.getLevelService(false, breaker).getLevel(equipment));
                }
            }
        }
    }
}
