package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;

import java.util.Map;

public class Levels1IfPresent extends LevelService {
    /**
     * This LevelService does not collect enchantment levels based on equipment.
     * This is useful in enchantments where the enchantment must be executed individually per equipment piece,
     * within the enchantment logic itself. Will always return an enchantment level of 1 if even 1 equipment
     * piece has the enchantment.
     */
    public Levels1IfPresent(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        if (getIfCompatible(equipment.getHelmet(), equipment.getHelmetEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                getIfCompatible(equipment.getChestplate(), equipment.getChestplateEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                getIfCompatible(equipment.getLeggings(), equipment.getLeggingsEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                getIfCompatible(equipment.getBoots(), equipment.getBootsEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                getIfCompatible(equipment.getMainHand(), equipment.getMainHandEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                getIfCompatible(equipment.getOffHand(), equipment.getOffHandEnchantments().getOrDefault(customEnchant, 0)) > 0 ||
                equipment.getMiscEquipmentEnchantments().keySet().stream().anyMatch(i -> {
                    Map<CustomEnchant, Integer> enchants = equipment.getMiscEquipmentEnchantments().get(i);
                    return getIfCompatible(i, enchants.getOrDefault(customEnchant, 0)) > 0;
                }))
            return 1;
        return 0;
    }
}
