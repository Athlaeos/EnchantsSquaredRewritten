package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class LevelsFromOffHandAndEquipment extends LevelService {
    public LevelsFromOffHandAndEquipment(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        int level = 0;
        if (compatible(equipment.getOffHand())) level += equipment.getOffHandEnchantments().getOrDefault(customEnchant, 0);
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
