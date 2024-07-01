package me.athlaeos.enchantssquared.enchantments.on_block_break;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;

public interface TriggerOnBlockBreakEnchantment {
    /**
     * Trigger on an BlockBreakEvent. Enchantments of this type trigger when a player possessing the enchantment
     * breaks a block
     * @param e the event
     */
    void onBlockBreak(BlockBreakEvent e, int level);
    /**
     * Trigger on an BlockDropItemEvent. Enchantments of this type trigger when a player possessing the enchantment
     * breaks a block and it then drops an item
     * @param e the event
     */
    void onBlockDropItem(BlockDropItemEvent e, int level);
}
