package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.inventory.ItemStack;

public abstract class LevelService {
    protected CustomEnchant customEnchant;
    public LevelService(CustomEnchant customEnchant){
        this.customEnchant = customEnchant;
    }
    /**
     * Responsible for gathering the enchantment level on a per-enchant basis
     * @param equipment the entity equipment to gather the levels from
     * @return the (combined) level of the enchantment on the equipment. Or 0 if none were found.
     */
    public abstract int getLevel(EntityEquipment equipment);

    public boolean compatible(ItemStack i){
        return !ItemUtils.isAirOrNull(i) && customEnchant.isFunctionallyCompatible(i.getType());
    }

    public int getIfCompatible(ItemStack i, int get){
        return compatible(i) ? get : 0;
    }
}
