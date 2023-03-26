package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;

public class LevelsFromMainHandOnly extends LevelService {
    public LevelsFromMainHandOnly(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        if (!compatible(equipment.getMainHand())) return 0;
        return equipment.getMainHandEnchantments().getOrDefault(customEnchant, 0);
    }
}
