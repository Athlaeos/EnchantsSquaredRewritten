package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class LevelsFromMainHandAndEquipment extends LevelService {
    public LevelsFromMainHandAndEquipment(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        int level = 0;
        if (compatible(equipment.getMainHand())) level += equipment.getMainHandEnchantments().getOrDefault(customEnchant, 0);
        if (compatible(equipment.getHelmet())) level += equipment.getHelmetEnchantments().getOrDefault(customEnchant, 0);
        if (compatible(equipment.getChestplate())) level += equipment.getChestplateEnchantments().getOrDefault(customEnchant, 0);
        if (compatible(equipment.getLeggings())) level += equipment.getLeggingsEnchantments().getOrDefault(customEnchant, 0);
        if (compatible(equipment.getBoots())) level += equipment.getBootsEnchantments().getOrDefault(customEnchant, 0);
        for (ItemStack i : equipment.getMiscEquipment()){
            if (compatible(i)) level += equipment.getMiscEquipmentEnchantments().getOrDefault(i, new HashMap<>()).getOrDefault(customEnchant, 0);
        }
        return level;
    }
}
